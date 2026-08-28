package com.example.data.model

enum class AlertType {
  DELIVERY_IN_5_DAYS,          // प्रसूतीच्या ५ दिवस आधी सूचना
  PREGNANCY_CHECK_DUE,         // ६० दिवसांनंतर गर्भ तपासणी सूचना
  THREE_MONTHS_POST_DELIVERY,  // प्रसूतीनंतर ३ महिन्यांनी रेतन/माज नियोजन सूचना
  DELIVERY_TODAY_OR_OVERDUE    // प्रसूती अपेक्षित आज किंवा झाली असेल तर नोंद करा
}

enum class AlertPriority {
  HIGH,
  MEDIUM,
  INFO
}

data class FarmAlert(
  val id: String,
  val type: AlertType,
  val priority: AlertPriority,
  val titleMr: String,
  val titleEn: String,
  val messageMr: String,
  val messageEn: String,
  val cowId: Long,
  val cowName: String,
  val dueDateEpoch: Long,
  val daysRemaining: Int,
  val breedingRecordId: Long
)
