package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MilkSession {
  MORNING,  // सकाळ
  EVENING   // संध्याकाळ
}

@Entity(tableName = "milk_entries")
data class MilkEntry(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val date: Long, // Start of day epoch millis
  val cowId: Long? = null, // Optional if tracking per cow or bulk
  val cowName: String = "", // e.g., "सर्व गाई (एकूण)" or "MH-101 (लक्ष्मी)"
  val session: MilkSession,
  val liters: Double,
  val fat: Double = 0.0,
  val snf: Double = 0.0,
  val ratePerLiter: Double = 0.0,
  val totalAmount: Double = (liters * ratePerLiter),
  val dairyCenterName: String = "",
  val createdBy: String = "Akshay (Admin)",
  val createdAt: Long = System.currentTimeMillis()
)
