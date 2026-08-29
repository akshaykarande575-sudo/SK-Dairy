package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.BreedingDao
import com.example.data.dao.CowDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.MilkDao
import com.example.data.model.BreedingRecord
import com.example.data.model.BreedingStatus
import com.example.data.model.Cow
import com.example.data.model.CowStatus
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntry
import com.example.data.model.MilkEntry
import com.example.data.model.MilkSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
  entities = [Cow::class, BreedingRecord::class, MilkEntry::class, ExpenseEntry::class],
  version = 2,
  exportSchema = false
)
abstract class DairyDatabase : RoomDatabase() {
  abstract fun cowDao(): CowDao
  abstract fun breedingDao(): BreedingDao
  abstract fun milkDao(): MilkDao
  abstract fun expenseDao(): ExpenseDao

  companion object {
    @Volatile
    private var INSTANCE: DairyDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): DairyDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          DairyDatabase::class.java,
          "dairy_farm_database"
        )
          .fallbackToDestructiveMigration()
          .addCallback(DairyDatabaseCallback(scope))
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DairyDatabaseCallback(
      private val scope: CoroutineScope
    ) : Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            populateInitialData(database)
          }
        }
      }

      suspend fun populateInitialData(database: DairyDatabase) {
        val cowDao = database.cowDao()
        val breedingDao = database.breedingDao()
        val milkDao = database.milkDao()
        val expenseDao = database.expenseDao()

        val cal = Calendar.getInstance()
        val today = cal.timeInMillis

        // 1. Initial Cows
        val cow1Id = cowDao.insertCow(
          Cow(
            tagNumber = "MH-101",
            name = "लक्ष्मी (Lakshmi)",
            breed = "HF Cross (एचएफ)",
            status = CowStatus.PREGNANT,
            dailyAvgMilk = 18.5,
            notes = "दुध देण्याची क्षमता उत्तम आहे. उच्च प्रतीची एचएफ संकरित गाय."
          )
        )

        val cow2Id = cowDao.insertCow(
          Cow(
            tagNumber = "MH-102",
            name = "कपिला (Kapila)",
            breed = "Gir (गीर गाय)",
            status = CowStatus.MILKING,
            dailyAvgMilk = 12.0,
            notes = "A2 दूध, फॅट जास्त (४.५ फॅट). अतिशय शांत स्वभावाची."
          )
        )

        val cow3Id = cowDao.insertCow(
          Cow(
            tagNumber = "MH-103",
            name = "गंगा (Ganga)",
            breed = "Jersey (जर्सी)",
            status = CowStatus.AI_DONE,
            dailyAvgMilk = 15.0,
            notes = "नुकतेच कृत्रिम रेतन (AI) केले आहे."
          )
        )

        val cow4Id = cowDao.insertCow(
          Cow(
            tagNumber = "MH-104",
            name = "गौरी (Gauri)",
            breed = "HF (होल्स्टीन)",
            status = CowStatus.DRY,
            dailyAvgMilk = 0.0,
            notes = "प्रसूती जवळ आल्याने आटवली आहे (Dry Period)."
          )
        )

        // 2. Breeding & Pregnancy Records:
        // A) Delivery in 4-5 days (Triggers 5-day delivery alert!)
        val calvingSoonDate = today + (4L * 24 * 60 * 60 * 1000) // 4 days from now
        val aiDate1 = calvingSoonDate - (282L * 24 * 60 * 60 * 1000)
        breedingDao.insertRecord(
          BreedingRecord(
            cowId = cow1Id,
            cowTagOrName = "लक्ष्मी (MH-101)",
            aiDate = aiDate1,
            bullSemenDetails = "HF Sexed Semen - ABS Bull #4088",
            inseminatorName = "डॉ. पाटील (पशुवैद्यकीय)",
            pregnancyCheckDate = aiDate1 + (60L * 24 * 60 * 60 * 1000),
            isPregnancyConfirmed = true,
            expectedDeliveryDate = calvingSoonDate,
            status = BreedingStatus.PREGNANCY_CONFIRMED,
            notes = "प्रसूती ५ दिवसांत अपेक्षित! गोठा स्वच्छ ठेवा व कॅल्शियम तयार ठेवा."
          )
        )

        // B) AI Done 50 days ago (approaching 60-day pregnancy check)
        val aiDate3 = today - (50L * 24 * 60 * 60 * 1000)
        breedingDao.insertRecord(
          BreedingRecord(
            cowId = cow3Id,
            cowTagOrName = "गंगा (MH-103)",
            aiDate = aiDate3,
            bullSemenDetails = "Jersey Pure Bull #204",
            inseminatorName = "डॉ. जाधव",
            pregnancyCheckDate = aiDate3 + (60L * 24 * 60 * 60 * 1000),
            isPregnancyConfirmed = false,
            expectedDeliveryDate = aiDate3 + (282L * 24 * 60 * 60 * 1000),
            status = BreedingStatus.AI_DONE,
            notes = "१० दिवसांत ६० दिवस पूर्ण होतील, गर्भ तपासणी करावी."
          )
        )

        // C) Delivered ~90 days ago (Triggers 3-Month Post-Delivery notification!)
        val delivered90DaysAgo = today - (91L * 24 * 60 * 60 * 1000)
        val aiDate2 = delivered90DaysAgo - (280L * 24 * 60 * 60 * 1000)
        breedingDao.insertRecord(
          BreedingRecord(
            cowId = cow2Id,
            cowTagOrName = "कपिला (MH-102)",
            aiDate = aiDate2,
            bullSemenDetails = "Girraj Gir Semen",
            inseminatorName = "डॉ. कदम",
            pregnancyCheckDate = aiDate2 + (60L * 24 * 60 * 60 * 1000),
            isPregnancyConfirmed = true,
            expectedDeliveryDate = delivered90DaysAgo,
            actualDeliveryDate = delivered90DaysAgo,
            calfGender = "कालवड (मादी) / Female Calf",
            status = BreedingStatus.DELIVERED,
            notes = "प्रसूती होऊन ३ महिने झाले. आता माज तपासा व नवीन रेतन नियोजन करा."
          )
        )

        // 3. Milk entries are not pre-seeded so the milk log starts completely clean for the user.

        // 4. Initial Month Expenses (Feed, Vet, Misc)
        expenseDao.insertExpense(
          ExpenseEntry(
            date = today - (10L * 24 * 60 * 60 * 1000),
            category = ExpenseCategory.CATTLE_FEED,
            amount = 8400.0,
            description = "सरकी पेंड व सुग्रास पशुखाद्य (४ पोती)",
            createdAt = today - (10L * 24 * 60 * 60 * 1000)
          )
        )
        expenseDao.insertExpense(
          ExpenseEntry(
            date = today - (6L * 24 * 60 * 60 * 1000),
            category = ExpenseCategory.FODDER,
            amount = 4500.0,
            description = "हिरवा मका चारा १ गाडी",
            createdAt = today - (6L * 24 * 60 * 60 * 1000)
          )
        )
        expenseDao.insertExpense(
          ExpenseEntry(
            date = today - (3L * 24 * 60 * 60 * 1000),
            category = ExpenseCategory.MEDICAL_VET,
            amount = 1200.0,
            description = "लसीकरण व मिनरल मिक्स्चर पाकीट",
            createdAt = today - (3L * 24 * 60 * 60 * 1000)
          )
        )
        expenseDao.insertExpense(
          ExpenseEntry(
            date = today - (1L * 24 * 60 * 60 * 1000),
            category = ExpenseCategory.AI_BREEDING,
            amount = 600.0,
            description = "कृत्रिम रेतन (AI) डॉक्टर फी व सिमेन स्ट्रॉ",
            createdAt = today - (1L * 24 * 60 * 60 * 1000)
          )
        )
      }
    }
  }
}
