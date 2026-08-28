package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CowStatus {
  MILKING,   // दुभती (In milk)
  PREGNANT,  // गाभण (Confirmed pregnant)
  AI_DONE,   // रेतन झालेली (Inseminated, awaiting check)
  DRY,       // आटलेली (Dry cow)
  HEIFER     // कालवड (Young female)
}

@Entity(tableName = "cows")
data class Cow(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val tagNumber: String,
  val name: String,
  val breed: String, // HF, Jersey, Gir, Sahiwal, Khillar, Buffalo, etc.
  val status: CowStatus = CowStatus.MILKING,
  val dailyAvgMilk: Double = 0.0,
  val notes: String = "",
  val createdAt: Long = System.currentTimeMillis()
)
