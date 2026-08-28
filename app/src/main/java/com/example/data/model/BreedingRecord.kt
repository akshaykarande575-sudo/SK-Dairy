package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class BreedingStatus {
  AI_DONE,               // रेतन झाले (Awaiting pregnancy check)
  PREGNANCY_CONFIRMED,   // गर्भधारणा निश्चित (Confirmed pregnant)
  DELIVERED,             // प्रसूती झाली (Calved)
  FAILED                 // उलटली / रेतन अयशस्वी (Failed / Repeat heat)
}

@Entity(
  tableName = "breeding_records",
  foreignKeys = [
    ForeignKey(
      entity = Cow::class,
      parentColumns = ["id"],
      childColumns = ["cowId"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index("cowId")]
)
data class BreedingRecord(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val cowId: Long,
  val cowTagOrName: String,
  val aiDate: Long, // AI Insemination date in epoch millis
  val bullSemenDetails: String = "", // e.g. "HF Pedigree 5543", "Gir Pure"
  val inseminatorName: String = "", // Doctor / AI technician name
  val pregnancyCheckDate: Long, // AI date + 60 days standard
  val isPregnancyConfirmed: Boolean = false,
  val expectedDeliveryDate: Long, // AI date + 282 days standard gestation for cows
  val actualDeliveryDate: Long? = null,
  val calfGender: String? = null, // "वासरू (नर) / Male", "कालवड (मादी) / Female"
  val status: BreedingStatus = BreedingStatus.AI_DONE,
  val notes: String = "",
  val createdAt: Long = System.currentTimeMillis()
)
