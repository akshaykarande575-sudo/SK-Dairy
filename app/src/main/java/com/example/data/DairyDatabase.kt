package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.BreedingDao
import com.example.data.dao.CowDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.MilkDao
import com.example.data.model.BreedingRecord
import com.example.data.model.Cow
import com.example.data.model.ExpenseEntry
import com.example.data.model.MilkEntry
import kotlinx.coroutines.CoroutineScope

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
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
