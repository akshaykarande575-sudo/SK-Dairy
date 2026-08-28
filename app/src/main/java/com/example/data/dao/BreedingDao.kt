package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BreedingRecord
import com.example.data.model.BreedingStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BreedingDao {
  @Query("SELECT * FROM breeding_records ORDER BY aiDate DESC")
  fun getAllBreedingRecords(): Flow<List<BreedingRecord>>

  @Query("SELECT * FROM breeding_records WHERE cowId = :cowId ORDER BY aiDate DESC")
  fun getRecordsForCow(cowId: Long): Flow<List<BreedingRecord>>

  @Query("SELECT * FROM breeding_records WHERE id = :id LIMIT 1")
  suspend fun getRecordById(id: Long): BreedingRecord?

  @Query("SELECT * FROM breeding_records WHERE status IN ('AI_DONE', 'PREGNANCY_CONFIRMED') ORDER BY expectedDeliveryDate ASC")
  fun getActivePregnancies(): Flow<List<BreedingRecord>>

  @Query("SELECT * FROM breeding_records WHERE status = 'DELIVERED' ORDER BY actualDeliveryDate DESC")
  fun getDeliveredRecords(): Flow<List<BreedingRecord>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRecord(record: BreedingRecord): Long

  @Update
  suspend fun updateRecord(record: BreedingRecord)

  @Delete
  suspend fun deleteRecord(record: BreedingRecord)

  @Query("DELETE FROM breeding_records WHERE id = :id")
  suspend fun deleteRecordById(id: Long)
}
