package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DairyRepository
import com.example.data.model.BreedingRecord
import com.example.data.model.Cow
import com.example.data.model.CowStatus
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntry
import com.example.data.model.FarmAlert
import com.example.data.model.MilkEntry
import com.example.data.model.MilkSession
import com.example.util.AppLanguage
import com.example.util.DateUtils
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

import com.example.data.CloudSyncService
import com.example.data.model.FarmMember
import com.example.data.model.FarmProfile
import com.example.data.model.MemberRole

data class MonthSummary(
  val year: Int,
  val month: Int,
  val monthName: String,
  val totalLiters: Double,
  val morningLiters: Double,
  val eveningLiters: Double,
  val avgFat: Double,
  val avgRate: Double,
  val totalRevenue: Double,
  val totalExpenses: Double,
  val netProfit: Double,
  val expenseBreakdown: Map<ExpenseCategory, Double>,
  val entriesCount: Int
)

class DairyViewModel(
  private val repository: DairyRepository,
  context: Context
) : ViewModel() {

  private val cloudSyncService = CloudSyncService(context, repository, viewModelScope)

  val language = MutableStateFlow(AppLanguage.MARATHI)
  val selectedMonthOffset = MutableStateFlow(0) // 0 = current month, -1 = last month, etc.
  val defaultBaseRate = MutableStateFlow<String>("") // Optional user-configured fixed base rate (e.g. ₹37)

  val farmProfile: StateFlow<FarmProfile> = cloudSyncService.farmProfile
  val syncStatusText: StateFlow<String> = cloudSyncService.syncStatusText

  val cows: StateFlow<List<Cow>> = combine(
    cloudSyncService.realtimeCows,
    repository.allCows
  ) { cloudList, roomList ->
    if (cloudList.isNotEmpty()) cloudList else roomList
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val breedingRecords: StateFlow<List<BreedingRecord>> = repository.allBreedingRecords
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val milkEntries: StateFlow<List<MilkEntry>> = combine(
    cloudSyncService.realtimeMilkEntries,
    repository.allMilkEntries
  ) { cloudList, roomList ->
    if (cloudList.isNotEmpty()) cloudList else roomList
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val expenses: StateFlow<List<ExpenseEntry>> = repository.allExpenses
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val farmAlerts: StateFlow<List<FarmAlert>> = repository.farmAlerts
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Monthly summary computed reactively
  val monthlySummary: StateFlow<MonthSummary> = combine(
    milkEntries,
    expenses,
    selectedMonthOffset
  ) { milkList, expenseList, offset ->
    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH, offset)
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)
    val monthName = DateUtils.formatMonthYear(cal.timeInMillis)

    val startEpoch = DateUtils.getStartOfMonth(year, month)
    val endEpoch = DateUtils.getEndOfMonth(year, month)

    val monthMilk = milkList.filter { it.date in startEpoch..endEpoch }
    val monthExpenses = expenseList.filter { it.date in startEpoch..endEpoch }

    val totalLiters = monthMilk.sumOf { it.liters }
    val morningLiters = monthMilk.filter { it.session == MilkSession.MORNING }.sumOf { it.liters }
    val eveningLiters = monthMilk.filter { it.session == MilkSession.EVENING }.sumOf { it.liters }

    val totalRevenue = monthMilk.sumOf { it.totalAmount }
    val avgFat = if (monthMilk.isNotEmpty()) monthMilk.map { it.fat }.filter { it > 0 }.let { if (it.isNotEmpty()) it.average() else 0.0 } else 0.0
    val avgRate = if (totalLiters > 0) totalRevenue / totalLiters else 0.0

    val totalExp = monthExpenses.sumOf { it.amount }
    val netProfit = totalRevenue - totalExp

    val expenseMap = mutableMapOf<ExpenseCategory, Double>()
    for (cat in ExpenseCategory.values()) {
      expenseMap[cat] = monthExpenses.filter { it.category == cat }.sumOf { it.amount }
    }

    MonthSummary(
      year = year,
      month = month,
      monthName = monthName,
      totalLiters = totalLiters,
      morningLiters = morningLiters,
      eveningLiters = eveningLiters,
      avgFat = avgFat,
      avgRate = avgRate,
      totalRevenue = totalRevenue,
      totalExpenses = totalExp,
      netProfit = netProfit,
      expenseBreakdown = expenseMap,
      entriesCount = monthMilk.size
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    MonthSummary(
      year = Calendar.getInstance().get(Calendar.YEAR),
      month = Calendar.getInstance().get(Calendar.MONTH),
      monthName = "",
      totalLiters = 0.0,
      morningLiters = 0.0,
      eveningLiters = 0.0,
      avgFat = 0.0,
      avgRate = 0.0,
      totalRevenue = 0.0,
      totalExpenses = 0.0,
      netProfit = 0.0,
      expenseBreakdown = emptyMap(),
      entriesCount = 0
    )
  )

  fun toggleLanguage() {
    language.value = if (language.value == AppLanguage.MARATHI) AppLanguage.ENGLISH else AppLanguage.MARATHI
  }

  fun setLanguage(lang: AppLanguage) {
    language.value = lang
  }

  fun selectMonthOffset(offset: Int) {
    selectedMonthOffset.value = offset
  }

  fun setDefaultBaseRate(rate: String) {
    defaultBaseRate.value = rate.trim()
  }

  // --- Cow Actions ---
  fun addCow(tagNumber: String, name: String, breed: String, status: CowStatus, avgMilk: Double, notes: String) {
    viewModelScope.launch {
      val cow = Cow(
        tagNumber = tagNumber.trim(),
        name = name.trim(),
        breed = breed.trim(),
        status = status,
        dailyAvgMilk = avgMilk,
        notes = notes.trim()
      )
      val insertedId = repository.insertCow(cow)
      cloudSyncService.syncCowToCloud(cow.copy(id = insertedId))
    }
  }

  fun deleteCow(cow: Cow) {
    viewModelScope.launch {
      repository.deleteCow(cow)
      cloudSyncService.deleteCowFromCloud(cow)
    }
  }

  // --- Breeding / AI Actions ---
  fun addBreedingRecord(
    cowId: Long,
    cowTagOrName: String,
    aiDate: Long,
    bullSemenDetails: String,
    inseminatorName: String,
    notes: String
  ) {
    viewModelScope.launch {
      repository.insertBreedingRecord(
        cowId = cowId,
        cowTagOrName = cowTagOrName,
        aiDate = aiDate,
        bullSemenDetails = bullSemenDetails,
        inseminatorName = inseminatorName,
        notes = notes
      )
    }
  }

  fun confirmPregnancy(recordId: Long, isConfirmed: Boolean) {
    viewModelScope.launch {
      repository.confirmPregnancy(recordId, isConfirmed)
    }
  }

  fun recordDelivery(recordId: Long, actualDeliveryDate: Long, calfGender: String, notes: String) {
    viewModelScope.launch {
      repository.recordDelivery(recordId, actualDeliveryDate, calfGender, notes)
    }
  }

  fun deleteBreedingRecord(record: BreedingRecord) {
    viewModelScope.launch {
      repository.deleteBreedingRecord(record)
    }
  }

  // --- Milk Actions ---
  fun addMilkEntry(
    date: Long,
    cowId: Long?,
    cowName: String,
    session: MilkSession,
    liters: Double,
    fat: Double,
    snf: Double,
    ratePerLiter: Double,
    dairyCenter: String
  ) {
    val currentUser = farmProfile.value.currentUserName
    val entry = MilkEntry(
      date = date,
      cowId = cowId,
      cowName = cowName.ifBlank { "गोठा एकूण दूध (All Herd)" },
      session = session,
      liters = liters,
      fat = fat,
      snf = snf,
      ratePerLiter = ratePerLiter,
      totalAmount = liters * ratePerLiter,
      dairyCenterName = dairyCenter,
      createdBy = currentUser
    )
    viewModelScope.launch {
      val insertedId = repository.insertMilkEntry(entry)
      cloudSyncService.syncMilkEntryToCloud(entry.copy(id = insertedId))
    }
  }

  fun deleteMilkEntry(entry: MilkEntry) {
    viewModelScope.launch {
      repository.deleteMilkEntry(entry)
      cloudSyncService.deleteMilkEntryFromCloud(entry)
    }
  }

  fun clearAllMilkEntries() {
    viewModelScope.launch {
      repository.clearAllMilkEntries()
    }
  }

  // --- Multi-User & Team Management ---
  fun inviteMember(name: String, contact: String, role: MemberRole) {
    cloudSyncService.inviteMember(name, contact, role)
  }

  fun removeMember(memberId: String) {
    cloudSyncService.removeMember(memberId)
  }

  fun updateMemberRole(memberId: String, newRole: MemberRole) {
    cloudSyncService.updateMemberRole(memberId, newRole)
  }

  fun switchActiveUser(member: FarmMember) {
    cloudSyncService.switchActiveUser(member)
  }

  fun joinFarmCode(code: String) {
    cloudSyncService.joinOrSetFarmCode(code)
  }

  // --- Farm Onboarding & Setup Actions ---
  fun createNewFarm(farmName: String, ownerName: String, ownerContact: String = "") {
    cloudSyncService.createNewFarm(farmName, ownerName, ownerContact)
  }

  fun joinExistingFarm(
    farmCode: String,
    userName: String,
    userContact: String = "",
    role: MemberRole = MemberRole.EDITOR,
    onComplete: (Boolean, String) -> Unit = { _, _ -> }
  ) {
    cloudSyncService.joinExistingFarm(farmCode, userName, userContact, role, onComplete)
  }

  fun switchFarmOrLogout() {
    cloudSyncService.switchFarmOrLogout()
  }

  // --- Expense Actions ---
  fun addExpense(
    date: Long,
    category: ExpenseCategory,
    amount: Double,
    description: String
  ) {
    viewModelScope.launch {
      repository.insertExpense(
        ExpenseEntry(
          date = date,
          category = category,
          amount = amount,
          description = description
        )
      )
    }
  }

  fun deleteExpense(expense: ExpenseEntry) {
    viewModelScope.launch {
      repository.deleteExpense(expense)
    }
  }

  // Check and push notifications for urgent alerts
  fun sendUrgentAlertsNotification(context: Context) {
    val currentAlerts = farmAlerts.value
    val isMarathi = language.value == AppLanguage.MARATHI
    currentAlerts.filter { it.type == com.example.data.model.AlertType.DELIVERY_IN_5_DAYS || it.type == com.example.data.model.AlertType.THREE_MONTHS_POST_DELIVERY }
      .take(2)
      .forEach { alert ->
        NotificationHelper.showNotification(context, alert, isMarathi)
      }
  }
}

class DairyViewModelFactory(
  private val repository: DairyRepository,
  private val context: Context
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(DairyViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return DairyViewModel(repository, context) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
