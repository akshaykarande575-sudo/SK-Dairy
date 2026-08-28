package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BreedingRecord
import com.example.data.model.BreedingStatus
import com.example.data.model.Cow
import com.example.data.model.FarmAlert
import com.example.ui.dialogs.AddBreedingDialog
import com.example.ui.dialogs.RecordDeliveryDialog
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.DateUtils
import java.util.concurrent.TimeUnit

@Composable
fun BreedingScreen(
  breedingRecords: List<BreedingRecord>,
  cows: List<Cow>,
  alerts: List<FarmAlert>,
  lang: AppLanguage,
  onAddBreeding: (cowId: Long, cowName: String, aiDate: Long, bullDetails: String, doctorName: String, notes: String) -> Unit,
  onConfirmPregnancy: (recordId: Long, isConfirmed: Boolean) -> Unit,
  onRecordDelivery: (recordId: Long, deliveryDate: Long, calfGender: String, notes: String) -> Unit,
  onDeleteRecord: (BreedingRecord) -> Unit
) {
  var selectedTab by remember { mutableStateOf(0) }
  var showAddAIDialog by remember { mutableStateOf(false) }
  var recordToDeliver by remember { mutableStateOf<BreedingRecord?>(null) }

  val isMr = lang == AppLanguage.MARATHI

  val activeRecords = breedingRecords.filter { it.status == BreedingStatus.AI_DONE || it.status == BreedingStatus.PREGNANCY_CONFIRMED }
  val deliveredRecords = breedingRecords.filter { it.status == BreedingStatus.DELIVERED }

  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("breeding_screen_list"),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Top Urgent Alerts Banner (5-Day Calving & 3-Month Post Delivery)
      if (alerts.isNotEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFB74D))),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  Icons.Default.NotificationsActive,
                  contentDescription = null,
                  tint = Color(0xFFE65100),
                  modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isMr) "🔔 तातडीच्या सूचना व अलर्ट (${alerts.size})" else "🔔 Critical Farm Alerts (${alerts.size})",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFBF360C)
                )
              }
              Spacer(modifier = Modifier.height(8.dp))
              alerts.forEach { alert ->
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(Color.White.copy(alpha = 0.8f), shape = RoundedCornerShape(8.dp))
                    .padding(10.dp)
                ) {
                  Text(
                    text = if (isMr) alert.titleMr else alert.titleEn,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD84315)
                  )
                  Text(
                    text = if (isMr) alert.messageMr else alert.messageEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF37474F),
                    modifier = Modifier.padding(top = 2.dp)
                  )
                }
              }
            }
          }
        }
      }

      // Tab switcher: Active Insemination / Calved History
      item {
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = {
              Text(
                text = "${if (isMr) "सक्रिय रेतन व गाभण" else "Active AI & Pregnant"} (${activeRecords.size})",
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
              )
            }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = {
              Text(
                text = "${if (isMr) "प्रसूती इतिहास (Calved)" else "Calving History"} (${deliveredRecords.size})",
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
              )
            }
          )
        }
      }

      val currentList = if (selectedTab == 0) activeRecords else deliveredRecords

      if (currentList.isEmpty()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                Icons.Default.Science,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = if (isMr) "कोणतीही रेतन नोंद आढळली नाही" else "No AI/Breeding records",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = if (isMr) "गाईला कृत्रिम रेतन (AI) केल्यावर खालील '+' बटनावर क्लिक करून नोंद करा." else "Tap '+' button below to add AI record for a cow.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp)
              )
            }
          }
        }
      } else {
        items(currentList, key = { it.id }) { record ->
          BreedingRecordCard(
            record = record,
            lang = lang,
            onConfirmPregnancy = { isConfirmed -> onConfirmPregnancy(record.id, isConfirmed) },
            onRecordDelivery = { recordToDeliver = record },
            onDelete = { onDeleteRecord(record) }
          )
        }
      }
    }

    // Add AI FAB
    FloatingActionButton(
      onClick = { showAddAIDialog = true },
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp)
        .testTag("add_ai_record_fab")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add AI Record")
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isMr) "AI रेतन नोंद" else "Add AI Record",
          fontWeight = FontWeight.Bold
        )
      }
    }
  }

  if (showAddAIDialog) {
    AddBreedingDialog(
      cows = cows,
      lang = lang,
      onDismiss = { showAddAIDialog = false },
      onSave = { cowId, cowName, aiDate, bullDetails, doctorName, notes ->
        onAddBreeding(cowId, cowName, aiDate, bullDetails, doctorName, notes)
        showAddAIDialog = false
      }
    )
  }

  recordToDeliver?.let { rec ->
    RecordDeliveryDialog(
      record = rec,
      lang = lang,
      onDismiss = { recordToDeliver = null },
      onSave = { deliveryDate, calfGender, notes ->
        onRecordDelivery(rec.id, deliveryDate, calfGender, notes)
        recordToDeliver = null
      }
    )
  }
}

@Composable
fun BreedingRecordCard(
  record: BreedingRecord,
  lang: AppLanguage,
  onConfirmPregnancy: (Boolean) -> Unit,
  onRecordDelivery: () -> Unit,
  onDelete: () -> Unit
) {
  val isMr = lang == AppLanguage.MARATHI
  val now = System.currentTimeMillis()
  val diffDelivery = record.expectedDeliveryDate - now
  val daysToDelivery = (diffDelivery / TimeUnit.DAYS.toMillis(1)).toInt()

  val daysSinceAI = ((now - record.aiDate) / TimeUnit.DAYS.toMillis(1)).toInt()

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header: Cow Name & Status Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "🐄 ${record.cowTagOrName}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "${if (isMr) "रेतन होऊन दिवस:" else "Days since AI:"} $daysSinceAI ${if (isMr) "दिवस" else "days"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
          )
        }

        // Status Badge
        Surface(
          shape = CircleShape,
          color = when (record.status) {
            BreedingStatus.PREGNANCY_CONFIRMED -> Color(0xFFC8E6C9)
            BreedingStatus.DELIVERED -> Color(0xFFBBDEFB)
            BreedingStatus.AI_DONE -> Color(0xFFFFE0B2)
            BreedingStatus.FAILED -> Color(0xFFFFCDD2)
          }
        ) {
          Text(
            text = when (record.status) {
              BreedingStatus.PREGNANCY_CONFIRMED -> if (isMr) "✓ गाभण निश्चित" else "Pregnant"
              BreedingStatus.DELIVERED -> if (isMr) "👶 प्रसूती झाली" else "Calved"
              BreedingStatus.AI_DONE -> if (isMr) "⏳ रेतन झाले (तपासणी बाकी)" else "Awaiting Check"
              BreedingStatus.FAILED -> if (isMr) "✗ उलटली" else "Failed"
            },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = when (record.status) {
              BreedingStatus.PREGNANCY_CONFIRMED -> Color(0xFF1B5E20)
              BreedingStatus.DELIVERED -> Color(0xFF0D47A1)
              BreedingStatus.AI_DONE -> Color(0xFFE65100)
              BreedingStatus.FAILED -> Color(0xFFB71C1C)
            },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // AI Date & Doctor Details
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
          .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(text = if (isMr) "कृत्रिम रेतन दिनांक (AI):" else "AI Date:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
          Text(text = DateUtils.formatDate(record.aiDate), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        if (record.bullSemenDetails.isNotBlank()) {
          Column {
            Text(text = if (isMr) "सिमेन / वळू जात:" else "Bull/Semen:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(text = record.bullSemenDetails, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Expected Delivery / Delivery Date Card
      if (record.status != BreedingStatus.DELIVERED) {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (daysToDelivery in 0..5) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = if (isMr) "अपेक्षित प्रसूती तारीख (Delivery Date):" else "Expected Delivery Date:",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Medium
                )
                Text(
                  text = DateUtils.formatDate(record.expectedDeliveryDate),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (daysToDelivery in 0..5) Color(0xFFC62828) else MaterialTheme.colorScheme.primary
                )
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (daysToDelivery in 0..5) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
              ) {
                Text(
                  text = if (daysToDelivery > 0) "$daysToDelivery ${if (isMr) "दिवस बाकी" else "days left"}" else (if (isMr) "प्रसूतीची वेळ झाली" else "Due now"),
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            if (daysToDelivery in 0..5) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = if (isMr) "🚨 प्रसूती ५ दिवसांच्या आत अपेक्षित आहे! गोठ्याची स्वच्छता व कॅल्शियमची सोय ठेवा." else "🚨 Calving due in 5 days! Prepare clean pen and fresh water.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFC62828),
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      } else {
        // Already Delivered View
        record.actualDeliveryDate?.let { delDate ->
          val daysSinceDelivery = ((now - delDate) / TimeUnit.DAYS.toMillis(1)).toInt()
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text(
                    text = if (isMr) "प्रत्यक्ष प्रसूती तारीख:" else "Actual Calved Date:",
                    style = MaterialTheme.typography.labelSmall
                  )
                  Text(
                    text = DateUtils.formatDate(delDate),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                  )
                }
                record.calfGender?.let { g ->
                  Text(
                    text = "👶 $g",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                  )
                }
              }

              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "${if (isMr) "प्रसूती होऊन दिवस:" else "Days since delivery:"} $daysSinceDelivery ${if (isMr) "दिवस" else "days"}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF2E7D32)
              )

              if (daysSinceDelivery >= 80) {
                Text(
                  text = if (isMr) "⏰ ३ महिने पूर्ण झाले आहेत. आता पुढील माज तपासा व नवीन रेतन करा." else "⏰ 3 Months complete. Check for next heat & plan insemination.",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFE65100),
                  modifier = Modifier.padding(top = 4.dp)
                )
              }
            }
          }
        }
      }

      // Action Buttons Row
      Spacer(modifier = Modifier.height(12.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (record.status == BreedingStatus.AI_DONE) {
          Button(
            onClick = { onConfirmPregnancy(true) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("confirm_preg_${record.id}")
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = if (isMr) "गाभण निश्चित करा (PD)" else "Confirm Pregnant")
          }
        } else if (record.status == BreedingStatus.PREGNANCY_CONFIRMED) {
          Button(
            onClick = onRecordDelivery,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("record_calved_${record.id}")
          ) {
            Icon(Icons.Default.ChildCare, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = if (isMr) "प्रसूती झाली (नोंद करा)" else "Record Calving")
          }
        } else {
          Spacer(modifier = Modifier.width(1.dp))
        }

        IconButton(
          onClick = onDelete,
          modifier = Modifier.testTag("delete_breeding_${record.id}")
        ) {
          Icon(Icons.Default.DeleteOutline, contentDescription = "Delete record", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
        }
      }
    }
  }
}
