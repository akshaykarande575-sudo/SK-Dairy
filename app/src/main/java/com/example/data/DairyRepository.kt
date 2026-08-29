package com.example.data

import com.example.data.dao.BreedingDao
import com.example.data.dao.CowDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.MilkDao
import com.example.data.model.AlertPriority
import com.example.data.model.AlertType
import com.example.data.model.BreedingRecord
import com.example.data.model.BreedingStatus
import com.example.data.model.Cow
import com.example.data.model.CowStatus
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntry
import com.example.data.model.FarmAlert
import com.example.data.model.MilkEntry
import com.example.data.model.MilkSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DairyRepository(
  private val cowDao: CowDao,
  private val breedingDao: BreedingDao,
  private val milkDao: MilkDao,
  private val expenseDao: ExpenseDao
) {
  // Cows
  val allCows: Flow<List<Cow>> = cowDao.getAllCows()
  val cowCount: Flow<Int> = cowDao.getCowCount()

  suspend fun insertCow(cow: Cow): Long = cowDao.insertCow(cow)
  suspend fun updateCow(cow: Cow) = cowDao.updateCow(cow)
  suspend fun deleteCow(cow: Cow) = cowDao.deleteCow(cow)
  suspend fun getCowById(id: Long): Cow? = cowDao.getCowById(id)

  // Breeding & AI
  val allBreedingRecords: Flow<List<BreedingRecord>> = breedingDao.getAllBreedingRecords()
  val activePregnancies: Flow<List<BreedingRecord>> = breedingDao.getActivePregnancies()
  val deliveredRecords: Flow<List<BreedingRecord>> = breedingDao.getDeliveredRecords()

  suspend fun insertBreedingRecord(
    cowId: Long,
    cowTagOrName: String,
    aiDate: Long,
    bullSemenDetails: String,
    inseminatorName: String,
    notes: String
  ): Long {
    // Bovine gestation period is typically 280-283 days (~9 months 9 days)
    val gestationDays = 282L
    val pregnancyCheckDays = 60L

    val expectedDelivery = aiDate + TimeUnit.DAYS.toMillis(gestationDays)
    val pregCheckDate = aiDate + TimeUnit.DAYS.toMillis(pregnancyCheckDays)

    val record = BreedingRecord(
      cowId = cowId,
      cowTagOrName = cowTagOrName,
      aiDate = aiDate,
      bullSemenDetails = bullSemenDetails,
      inseminatorName = inseminatorName,
      pregnancyCheckDate = pregCheckDate,
      isPregnancyConfirmed = false,
      expectedDeliveryDate = expectedDelivery,
      status = BreedingStatus.AI_DONE,
      notes = notes
    )
    val id = breedingDao.insertRecord(record)

    // Update cow status
    cowDao.getCowById(cowId)?.let { cow ->
      cowDao.updateCow(cow.copy(status = CowStatus.AI_DONE))
    }
    return id
  }

  suspend fun confirmPregnancy(recordId: Long, isConfirmed: Boolean) {
    breedingDao.getRecordById(recordId)?.let { record ->
      val newStatus = if (isConfirmed) BreedingStatus.PREGNANCY_CONFIRMED else BreedingStatus.FAILED
      breedingDao.updateRecord(
        record.copy(
          isPregnancyConfirmed = isConfirmed,
          status = newStatus
        )
      )
      // Update cow status
      cowDao.getCowById(record.cowId)?.let { cow ->
        val cowStatus = if (isConfirmed) CowStatus.PREGNANT else CowStatus.MILKING
        cowDao.updateCow(cow.copy(status = cowStatus))
      }
    }
  }

  suspend fun recordDelivery(
    recordId: Long,
    actualDeliveryDate: Long,
    calfGender: String,
    notes: String
  ) {
    breedingDao.getRecordById(recordId)?.let { record ->
      breedingDao.updateRecord(
        record.copy(
          actualDeliveryDate = actualDeliveryDate,
          calfGender = calfGender,
          status = BreedingStatus.DELIVERED,
          notes = if (notes.isNotBlank()) "${record.notes} | $notes" else record.notes
        )
      )
      // Update cow to MILKING
      cowDao.getCowById(record.cowId)?.let { cow ->
        cowDao.updateCow(cow.copy(status = CowStatus.MILKING))
      }
    }
  }

  suspend fun deleteBreedingRecord(record: BreedingRecord) {
    breedingDao.deleteRecord(record)
  }

  // Milk Entries
  val allMilkEntries: Flow<List<MilkEntry>> = milkDao.getAllMilkEntries()

  suspend fun insertMilkEntry(entry: MilkEntry): Long = milkDao.insertMilkEntry(entry)
  suspend fun updateMilkEntry(entry: MilkEntry) = milkDao.updateMilkEntry(entry)
  suspend fun deleteMilkEntry(entry: MilkEntry) = milkDao.deleteMilkEntry(entry)
  suspend fun clearAllMilkEntries() = milkDao.deleteAllMilkEntries()

  // Expenses
  val allExpenses: Flow<List<ExpenseEntry>> = expenseDao.getAllExpenses()

  suspend fun insertExpense(expense: ExpenseEntry): Long = expenseDao.insertExpense(expense)
  suspend fun updateExpense(expense: ExpenseEntry) = expenseDao.updateExpense(expense)
  suspend fun deleteExpense(expense: ExpenseEntry) = expenseDao.deleteExpense(expense)

  // Smart Alerts Engine Flow (Computed from Breeding records & current time)
  val farmAlerts: Flow<List<FarmAlert>> = combine(allBreedingRecords) { recordsArray ->
    val records = recordsArray[0]
    val now = System.currentTimeMillis()
    val oneDayMillis = TimeUnit.DAYS.toMillis(1)
    val alerts = mutableListOf<FarmAlert>()

    for (record in records) {
      // 1. Delivery approaching in 5 days or less / Today / Overdue
      if (record.status == BreedingStatus.PREGNANCY_CONFIRMED || record.status == BreedingStatus.AI_DONE) {
        val diffMillis = record.expectedDeliveryDate - now
        val daysUntilDelivery = (diffMillis / oneDayMillis).toInt()

        if (daysUntilDelivery in 0..5) {
          // Exactly the user's requirement: "Pregnancy chya adi 5 days notify jhal pahije mla"
          alerts.add(
            FarmAlert(
              id = "delivery_soon_${record.id}",
              type = AlertType.DELIVERY_IN_5_DAYS,
              priority = AlertPriority.HIGH,
              titleMr = "🚨 प्रसूतीची वेळ जवळ आली आहे! ($daysUntilDelivery दिवस बाकी)",
              titleEn = "🚨 Calving Alert! ($daysUntilDelivery days remaining)",
              messageMr = "गाय: ${record.cowTagOrName} ची अपेक्षित प्रसूती तारीख जवळ आहे. गोठा स्वच्छ ठेवा, सुका चारा व कॅल्शियमची व्यवस्था करा.",
              messageEn = "Cow: ${record.cowTagOrName} is due for delivery soon. Prepare clean calving pen and fresh water.",
              cowId = record.cowId,
              cowName = record.cowTagOrName,
              dueDateEpoch = record.expectedDeliveryDate,
              daysRemaining = daysUntilDelivery,
              breedingRecordId = record.id
            )
          )
        } else if (daysUntilDelivery < 0 && record.status != BreedingStatus.DELIVERED) {
          // Overdue or delivery happened
          alerts.add(
            FarmAlert(
              id = "delivery_overdue_${record.id}",
              type = AlertType.DELIVERY_TODAY_OR_OVERDUE,
              priority = AlertPriority.HIGH,
              titleMr = "⚠️ प्रसूती तारीख झाली आहे (नोंद करा)",
              titleEn = "⚠️ Expected Delivery Passed (Mark Calved)",
              messageMr = "गाय: ${record.cowTagOrName} ची प्रसूती झाली असल्यास 'प्रसूती नोंद' बटनावर क्लिक करून नोंद करा.",
              messageEn = "Cow: ${record.cowTagOrName} delivery date has arrived. Record actual delivery if calved.",
              cowId = record.cowId,
              cowName = record.cowTagOrName,
              dueDateEpoch = record.expectedDeliveryDate,
              daysRemaining = daysUntilDelivery,
              breedingRecordId = record.id
            )
          )
        }

        // 2. Pregnancy Check Alert (~60 days after AI)
        if (!record.isPregnancyConfirmed && record.status == BreedingStatus.AI_DONE) {
          val daysSinceAI = ((now - record.aiDate) / oneDayMillis).toInt()
          if (daysSinceAI >= 55 && daysSinceAI <= 90) {
            val daysUntil60 = 60 - daysSinceAI
            val titleMr = if (daysUntil60 <= 0) "🔍 गर्भ तपासणी (PD) वेळ झाली आहे" else "🔍 गर्भ तपासणी ($daysUntil60 दिवसांत)"
            alerts.add(
              FarmAlert(
                id = "preg_check_${record.id}",
                type = AlertType.PREGNANCY_CHECK_DUE,
                priority = AlertPriority.MEDIUM,
                titleMr = titleMr,
                titleEn = "🔍 Pregnancy Diagnosis (PD) Due",
                messageMr = "गाय: ${record.cowTagOrName} ला रेतन करून $daysSinceAI दिवस झाले आहेत. डॉक्टरांकडून गर्भधारणा तपासणी (PD) करून घ्या.",
                messageEn = "Cow: ${record.cowTagOrName} was inseminated $daysSinceAI days ago. Perform pregnancy diagnosis.",
                cowId = record.cowId,
                cowName = record.cowTagOrName,
                dueDateEpoch = record.pregnancyCheckDate,
                daysRemaining = daysUntil60,
                breedingRecordId = record.id
              )
            )
          }
        }
      }

      // 3. User's requirement: "Delevery nantr 3 month ne notification pan dya"
      // 3 Months Post-Delivery Notification (approx 90 days after delivery)
      if (record.status == BreedingStatus.DELIVERED && record.actualDeliveryDate != null) {
        val daysSinceDelivery = ((now - record.actualDeliveryDate) / oneDayMillis).toInt()
        // If between 80 and 130 days post-calving
        if (daysSinceDelivery in 80..130) {
          val remainingTo90 = 90 - daysSinceDelivery
          alerts.add(
            FarmAlert(
              id = "post_delivery_3mo_${record.id}",
              type = AlertType.THREE_MONTHS_POST_DELIVERY,
              priority = AlertPriority.MEDIUM,
              titleMr = "⏰ प्रसूतीनंतर ३ महिने पूर्ण - नवीन रेतन / माज नियोजन",
              titleEn = "⏰ 3 Months Post-Calving - Heat & AI Check",
              messageMr = "गाय: ${record.cowTagOrName} ची प्रसूती होऊन $daysSinceDelivery दिवस (३ महिने) झाले आहेत. गाईचा माज ओळखून वेळेत नवीन कृत्रिम रेतन (AI) चे नियोजन करा.",
              messageEn = "Cow: ${record.cowTagOrName} calved $daysSinceDelivery days ago (3 months). Check for heat cycle and plan next AI.",
              cowId = record.cowId,
              cowName = record.cowTagOrName,
              dueDateEpoch = record.actualDeliveryDate + TimeUnit.DAYS.toMillis(90),
              daysRemaining = remainingTo90,
              breedingRecordId = record.id
            )
          )
        }
      }
    }

    alerts.sortedBy { it.priority }
  }
}
