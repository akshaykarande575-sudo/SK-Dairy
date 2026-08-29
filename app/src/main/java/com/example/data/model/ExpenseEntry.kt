package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ExpenseCategory {
  CATTLE_FEED,      // पशूखाद्य व पेंड (Cattle Feed & Pend)
  FODDER,           // हिरवा / सुका चारा (Green/Dry Fodder)
  MEDICAL_VET,      // डॉक्टर व औषधोपचार (Vet & Medicine)
  AI_BREEDING,      // कृत्रिम रेतन खर्च (AI / Insemination Fee)
  LABOR,            // मजुरी / कामगार खर्च (Labor / Milking charges)
  ELECTRICITY_MAINT,// वीज, पाणी व देखभाल (Electricity & Farm repair)
  OTHER             // इतर खर्च (Other misc)
}

@Entity(tableName = "expenses")
data class ExpenseEntry(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val date: Long,
  val category: ExpenseCategory,
  val amount: Double,
  val description: String = "",
  val createdBy: String = "Akshay (Admin)",
  val createdAt: Long = System.currentTimeMillis()
)
