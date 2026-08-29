package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Cow
import kotlinx.coroutines.flow.Flow

@Dao
interface CowDao {
  @Query("SELECT * FROM cows ORDER BY name ASC")
  fun getAllCows(): Flow<List<Cow>>

  @Query("SELECT * FROM cows WHERE id = :id LIMIT 1")
  suspend fun getCowById(id: Long): Cow?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCow(cow: Cow): Long

  @Update
  suspend fun updateCow(cow: Cow)

  @Delete
  suspend fun deleteCow(cow: Cow)

  @Query("DELETE FROM cows WHERE id = :id")
  suspend fun deleteCowById(id: Long)

  @Query("DELETE FROM cows")
  suspend fun deleteAllCows()

  @Query("SELECT COUNT(*) FROM cows")
  fun getCowCount(): Flow<Int>
}
