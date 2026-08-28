package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MilkEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MilkDao {
  @Query("SELECT * FROM milk_entries ORDER BY date DESC, createdAt DESC")
  fun getAllMilkEntries(): Flow<List<MilkEntry>>

  @Query("SELECT * FROM milk_entries WHERE date >= :startEpoch AND date <= :endEpoch ORDER BY date DESC, session ASC")
  fun getMilkEntriesForPeriod(startEpoch: Long, endEpoch: Long): Flow<List<MilkEntry>>

  @Query("SELECT * FROM milk_entries WHERE cowId = :cowId ORDER BY date DESC")
  fun getMilkEntriesForCow(cowId: Long): Flow<List<MilkEntry>>

  @Query("SELECT * FROM milk_entries WHERE id = :id LIMIT 1")
  suspend fun getMilkEntryById(id: Long): MilkEntry?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMilkEntry(entry: MilkEntry): Long

  @Update
  suspend fun updateMilkEntry(entry: MilkEntry)

  @Delete
  suspend fun deleteMilkEntry(entry: MilkEntry)

  @Query("DELETE FROM milk_entries WHERE id = :id")
  suspend fun deleteMilkEntryById(id: Long)
}
