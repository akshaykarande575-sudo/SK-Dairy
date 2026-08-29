package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
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
import com.example.data.model.Cow
import com.example.data.model.MemberRole
import com.example.data.model.MilkEntry
import com.example.data.model.MilkSession
import com.example.ui.MonthSummary
import com.example.ui.dialogs.AddMilkDialog
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.DateUtils

import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import com.example.ui.dialogs.TenDayBillingDialog
import com.example.util.WhatsAppShareHelper

@Composable
fun MilkScreen(
  milkEntries: List<MilkEntry>,
  cows: List<Cow>,
  monthSummary: MonthSummary,
  lang: AppLanguage,
  currentUserRole: MemberRole = MemberRole.ADMIN,
  currentUserName: String = "Akshay (Admin)",
  farmName: String = "SK Dairy",
  farmCode: String = "",
  defaultRate: String = "",
  onSetDefaultRate: (String) -> Unit = {},
  onClearAllMilk: () -> Unit = {},
  onAddMilk: (date: Long, cowId: Long?, cowName: String, session: MilkSession, liters: Double, fat: Double, snf: Double, rate: Double, dairyName: String) -> Unit,
  onDeleteMilk: (MilkEntry) -> Unit
) {
  val context = LocalContext.current
  var showAddDialog by remember { mutableStateOf(false) }
  var showRateSettingDialog by remember { mutableStateOf(false) }
  var showClearConfirmDialog by remember { mutableStateOf(false) }
  var showTenDayBillingDialog by remember { mutableStateOf(false) }
  var tempRateInput by remember { mutableStateOf(defaultRate) }

  val isMr = lang == AppLanguage.MARATHI
  val canEdit = currentUserRole == MemberRole.ADMIN || currentUserRole == MemberRole.EDITOR

  if (showRateSettingDialog) {
    AlertDialog(
      onDismissRequest = { showRateSettingDialog = false },
      icon = { Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
      title = {
        Text(
          text = if (isMr) "ठरविक दूध दर सेटिंग (Default Rate)" else "Default Milk Rate Setting",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = if (isMr) "महिनाभरासाठी किंवा नेहमीसाठी ठरविक दर सेट करा (उदा. ₹37). हा दर नवीन नोंद करताना आपोआप भरेल, पण आवश्यकतेनुसार बदलता येईल."
            else "Set a default fixed rate per liter (e.g. ₹37). It will automatically populate in new entries while remaining fully editable.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          OutlinedTextField(
            value = tempRateInput,
            onValueChange = { tempRateInput = it },
            label = { Text(if (isMr) "दर प्रति लिटर (₹)" else "Rate per Liter (₹)") },
            placeholder = { Text("उदा. 37.0") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("default_rate_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onSetDefaultRate(tempRateInput)
            showRateSettingDialog = false
          },
          modifier = Modifier.testTag("save_default_rate_button")
        ) {
          Text(if (isMr) "दर सेव्ह करा" else "Save Rate")
        }
      },
      dismissButton = {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          if (tempRateInput.isNotBlank() || defaultRate.isNotBlank()) {
            TextButton(
              onClick = {
                tempRateInput = ""
                onSetDefaultRate("")
                showRateSettingDialog = false
              }
            ) {
              Text(if (isMr) "काढून टाका" else "Clear")
            }
          }
          OutlinedButton(onClick = { showRateSettingDialog = false }) {
            Text(AppStrings.cancel(lang))
          }
        }
      }
    )
  }

  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
      title = {
        Text(
          text = if (isMr) "सर्व दूध नोंदी पुसायच्या आहेत का?" else "Clear All Milk Records?",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Text(
          text = if (isMr) "यामुळे डेटाबेसमधील सर्व दूध नोंदी कायमच्या हटवल्या जातील आणि नवीन कोरी नोंद सुरू करता येईल."
          else "This will remove all milk records from the database so you can start completely clean."
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onClearAllMilk()
            showClearConfirmDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("confirm_clear_all_button")
        ) {
          Text(if (isMr) "हो, सर्व पुसा" else "Yes, Clear All")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showClearConfirmDialog = false }) {
          Text(AppStrings.cancel(lang))
        }
      }
    )
  }

  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("milk_screen_list"),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Realtime Cloud Synced Live Banner
      item {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFE8F5E9),
          border = BorderStroke(1.dp, Color(0xFFA5D6A7)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("cloud_sync_banner")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .background(Color(0xFF2E7D32), CircleShape)
              )
              Icon(
                Icons.Default.CloudDone,
                contentDescription = "Cloud Synced",
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(18.dp)
              )
              Text(
                text = if (isMr) "Cloud Synced (क्लाऊड सिंक सुरू आहे)" else "Cloud Synced (क्लाऊड सिंक सुरू आहे)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
              )
            }
            if (farmCode.isNotBlank()) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFC8E6C9)
              ) {
                Text(
                  text = farmCode,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF1B5E20),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
        }
      }

      // Month Overview Card
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${monthSummary.monthName} ${if (isMr) "दूध सारांश" else "Milk Summary"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.padding(4.dp)
              ) {
                Text(
                  text = "${monthSummary.entriesCount} ${if (isMr) "नोंदी" else "entries"}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimary,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              // Total Milk
              Column {
                Text(
                  text = if (isMr) "एकूण दूध (Total)" else "Total Milk",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                  text = DateUtils.formatLiters(monthSummary.totalLiters),
                  style = MaterialTheme.typography.headlineMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }

              // Total Revenue
              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = if (isMr) "दूध उत्पन्न (Revenue)" else "Milk Income",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                  text = DateUtils.formatCurrency(monthSummary.totalRevenue),
                  style = MaterialTheme.typography.headlineMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-metrics row (Morning vs Evening vs Fat)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                  shape = RoundedCornerShape(12.dp)
                )
                .padding(10.dp),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${if (isMr) "सकाळ:" else "Morn:"} ${DateUtils.formatLiters(monthSummary.morningLiters)}",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Medium
                )
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WbTwilight, contentDescription = null, tint = Color(0xFF5C6BC0), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${if (isMr) "संध्याकाळ:" else "Eve:"} ${DateUtils.formatLiters(monthSummary.eveningLiters)}",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Medium
                )
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Opacity, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${if (isMr) "फॅट:" else "Fat:"} ${String.format("%.1f", monthSummary.avgFat)}%",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Medium
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: 10-Day Bill & WhatsApp Share
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // 10-Day Billing Button
              Button(
                onClick = { showTenDayBillingDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("open_10day_bill_button")
              ) {
                Icon(
                  Icons.Default.ReceiptLong,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (isMr) "१०-दिवसीय बिल" else "10-Day Bill",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              // WhatsApp Share Monthly Report Button
              Button(
                onClick = {
                  val text = WhatsAppShareHelper.generateMonthlySummaryWhatsAppText(
                    farmName = farmName,
                    monthSummary = monthSummary
                  )
                  WhatsAppShareHelper.shareViaWhatsApp(context, text)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .weight(1.2f)
                  .testTag("share_whatsapp_milk_summary_button")
              ) {
                Icon(
                  Icons.Default.Share,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (isMr) "व्हाट्सअॅप अहवाल" else "WhatsApp Report",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }
          }
        }
      }

      // Quick Controls: Base Rate Setting & Clear Records Bar
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            onClick = {
              tempRateInput = defaultRate
              showRateSettingDialog = true
            },
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.testTag("rate_setting_button")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                Icons.Default.Tune,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (defaultRate.isNotBlank())
                  (if (isMr) "ठरविक दर: ₹$defaultRate/L" else "Base Rate: ₹$defaultRate/L")
                else
                  (if (isMr) "⚙️ ठरविक दर सेट करा (Base Rate)" else "⚙️ Set Default Base Rate"),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          if (milkEntries.isNotEmpty()) {
            TextButton(
              onClick = { showClearConfirmDialog = true },
              modifier = Modifier.testTag("clear_all_milk_button")
            ) {
              Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isMr) "नोंदी पुसा" else "Clear All",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error
              )
            }
          }
        }
      }

      // Section Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isMr) "दैनिक दूध नोंदी (Daily Logs)" else "Daily Milk Logs",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${milkEntries.size} ${if (isMr) "नोंदी उपलब्ध" else "logs"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
          )
        }
      }

      // Empty State
      if (milkEntries.isEmpty()) {
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
                Icons.Default.Opacity,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = if (isMr) "कोणतीही दूध नोंद उपलब्ध नाही" else "No milk records found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = if (isMr) "खालील '+' बटनावर क्लिक करून आजची सकाळ किंवा संध्याकाळची नोंद करा." else "Tap '+' button below to record morning or evening milk collection.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp)
              )
            }
          }
        }
      } else {
        items(milkEntries, key = { it.id }) { entry ->
          MilkEntryCard(
            entry = entry,
            lang = lang,
            canDelete = canEdit,
            onDelete = { onDeleteMilk(entry) }
          )
        }
      }
    }

    // Floating Action Button
    FloatingActionButton(
      onClick = {
        if (canEdit) {
          showAddDialog = true
        } else {
          Toast.makeText(
            context,
            if (isMr) "वाचक (Viewer) मोडमध्ये नोंद करण्याची परवानगी नाही. ॲडमिनशी संपर्क साधा." else "Viewers cannot record milk. Contact farm admin.",
            Toast.LENGTH_LONG
          ).show()
        }
      },
      containerColor = if (canEdit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
      contentColor = if (canEdit) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp)
        .testTag("add_milk_fab")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(if (canEdit) Icons.Default.Add else Icons.Default.Lock, contentDescription = "Add Milk Log")
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (canEdit) (if (isMr) "दूध नोंद" else "Record Milk") else (if (isMr) "फक्त वाचक" else "View Only"),
          fontWeight = FontWeight.Bold
        )
      }
    }
  }

  if (showAddDialog) {
    AddMilkDialog(
      cows = cows,
      lang = lang,
      defaultRate = defaultRate,
      onDismiss = { showAddDialog = false },
      onSave = { date, cowId, cowName, session, liters, fat, snf, rate, dairyName ->
        onAddMilk(date, cowId, cowName, session, liters, fat, snf, rate, dairyName)
        showAddDialog = false
      }
    )
  }

  if (showTenDayBillingDialog) {
    TenDayBillingDialog(
      milkEntries = milkEntries,
      year = monthSummary.year,
      month = monthSummary.month,
      farmName = farmName,
      lang = lang,
      onDismiss = { showTenDayBillingDialog = false }
    )
  }
}

@Composable
fun MilkEntryCard(
  entry: MilkEntry,
  lang: AppLanguage,
  canDelete: Boolean = true,
  onDelete: () -> Unit
) {
  val isMr = lang == AppLanguage.MARATHI
  val isMorning = entry.session == MilkSession.MORNING

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Date & Session Badge
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(
                if (isMorning) Color(0xFFFFF3E0) else Color(0xFFEDE7F6)
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isMorning) Icons.Default.WbSunny else Icons.Default.WbTwilight,
              contentDescription = null,
              tint = if (isMorning) Color(0xFFE65100) else Color(0xFF4527A0),
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = DateUtils.formatDate(entry.date),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = if (isMorning) (if (isMr) "सकाळ संकलन (Morning)" else "Morning Session") else (if (isMr) "संध्याकाळ संकलन (Evening)" else "Evening Session"),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.outline
            )
          }
        }

        // Delete button
        if (canDelete) {
          IconButton(
            onClick = onDelete,
            modifier = Modifier.testTag("delete_milk_${entry.id}")
          ) {
            Icon(
              Icons.Default.DeleteOutline,
              contentDescription = "Delete entry",
              tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Cow / Source tag
      if (entry.cowName.isNotBlank()) {
        Text(
          text = "🐄 ${entry.cowName}",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(bottom = 6.dp)
        )
      }

      // Quantity, Fat, SNF, Rate, and Total row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(10.dp)
          )
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(text = if (isMr) "प्रमाण (Liters)" else "Liters", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
          Text(text = DateUtils.formatLiters(entry.liters), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (entry.fat > 0) {
          Column {
            Text(text = if (isMr) "फॅट (Fat)" else "Fat", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(text = "${entry.fat}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
          }
        }

        if (entry.ratePerLiter > 0) {
          Column {
            Text(text = if (isMr) "दर (Rate)" else "Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(text = "₹${entry.ratePerLiter}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
          }
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(text = if (isMr) "रक्कम (Total)" else "Total Amount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
          Text(
            text = DateUtils.formatCurrency(entry.totalAmount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      // Bottom Metadata: Dairy center & Author attribution
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (entry.dairyCenterName.isNotBlank()) {
          Text(
            text = "📍 ${entry.dairyCenterName}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
          )
        } else {
          Spacer(modifier = Modifier.width(1.dp))
        }

        if (entry.createdBy.isNotBlank()) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              Icons.Default.CloudDone,
              contentDescription = null,
              tint = Color(0xFF4CAF50),
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isMr) "नोंद: ${entry.createdBy}" else "By: ${entry.createdBy}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.outline
            )
          }
        }
      }
    }
  }
}
