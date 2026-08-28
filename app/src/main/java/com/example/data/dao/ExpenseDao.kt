package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ExpenseEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
  @Query("SELECT * FROM expenses ORDER BY date DESC, createdAt DESC")
  fun getAllExpenses(): Flow<List<ExpenseEntry>>

  @Query("SELECT * FROM expenses WHERE date >= :startEpoch AND date <= :endEpoch ORDER BY date DESC")
  fun getExpensesForPeriod(startEpoch: Long, endEpoch: Long): Flow<List<ExpenseEntry>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpense(expense: ExpenseEntry): Long

  @Update
  suspend fun updateExpense(expense: ExpenseEntry)

  @Delete
  suspend fun deleteExpense(expense: ExpenseEntry)

  @Query("DELETE FROM expenses WHERE id = :id")
  suspend fun deleteExpenseById(id: Long)
}
