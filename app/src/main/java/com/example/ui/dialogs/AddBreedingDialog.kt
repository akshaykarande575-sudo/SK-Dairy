package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Cow
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.DateUtils
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBreedingDialog(
  cows: List<Cow>,
  lang: AppLanguage,
  onDismiss: () -> Unit,
  onSave: (cowId: Long, cowName: String, aiDate: Long, bullDetails: String, doctorName: String, notes: String) -> Unit
) {
  var selectedCow by remember { mutableStateOf(cows.firstOrNull()) }
  var cowDropdownExpanded by remember { mutableStateOf(false) }

  var aiDate by remember { mutableStateOf(DateUtils.getStartOfDay(System.currentTimeMillis())) }
  var showDatePicker by remember { mutableStateOf(false) }

  var bullDetails by remember { mutableStateOf("") }
  var doctorName by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }

  val isMr = lang == AppLanguage.MARATHI

  // Calculate automatic milestones
  val pregCheckDate = aiDate + TimeUnit.DAYS.toMillis(60)
  val expectedDeliveryDate = aiDate + TimeUnit.DAYS.toMillis(282)

  if (showDatePicker) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = aiDate)
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let {
              aiDate = DateUtils.getStartOfDay(it)
            }
            showDatePicker = false
          },
          modifier = Modifier.testTag("confirm_ai_date_button")
        ) {
          Text(AppStrings.confirm(lang))
        }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) {
          Text(AppStrings.cancel(lang))
        }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Default.Science,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(end = 8.dp)
        )
        Text(
          text = if (isMr) "कृत्रिम रेतन (AI) नोंद करा" else "Record AI Insemination",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Cow Selector
        ExposedDropdownMenuBox(
          expanded = cowDropdownExpanded,
          onExpandedChange = { cowDropdownExpanded = it },
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = selectedCow?.let { "${it.tagNumber} - ${it.name} (${it.breed})" } ?: (if (isMr) "गाय निवडा *" else "Select Cow *"),
            onValueChange = {},
            readOnly = true,
            label = { Text(if (isMr) "रेतन केलेली गाय *" else "Inseminated Cow *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cowDropdownExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
              .testTag("ai_cow_selector_dropdown")
          )
          ExposedDropdownMenu(
            expanded = cowDropdownExpanded,
            onDismissRequest = { cowDropdownExpanded = false }
          ) {
            cows.forEach { cow ->
              DropdownMenuItem(
                text = { Text("${cow.tagNumber} - ${cow.name} (${cow.breed})") },
                onClick = {
                  selectedCow = cow
                  cowDropdownExpanded = false
                }
              )
            }
          }
        }

        // AI Date Selection
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = if (isMr) "कृत्रिम रेतन दिनांक (AI Date):" else "AI Insemination Date:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = DateUtils.formatDate(aiDate),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }
            IconButton(
              onClick = { showDatePicker = true },
              modifier = Modifier.testTag("change_ai_date_button")
            ) {
              Icon(Icons.Default.CalendarMonth, contentDescription = "Select AI Date", tint = MaterialTheme.colorScheme.primary)
            }
          }
        }

        // Automatic Milestone Forecast Card
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = if (isMr) "📊 आपोआप गणना केलेले टप्पे (Auto Milestones):" else "📊 Calculated Milestones:",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
              text = "• ${if (isMr) "गर्भ तपासणी (PD) तारीख:" else "Pregnancy Check Due:"} ${DateUtils.formatDate(pregCheckDate)} (६० दिवस)",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
              text = "• ${if (isMr) "अपेक्षित प्रसूती (Delivery):" else "Expected Calving:"} ${DateUtils.formatDate(expectedDeliveryDate)} (२८२ दिवस)",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
              text = "• ${if (isMr) "५ दिवस आधी प्रसूती सूचना:" else "5 Days Prior Alert:"} ${DateUtils.formatDate(expectedDeliveryDate - TimeUnit.DAYS.toMillis(5))}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
          }
        }

        // Bull / Semen Details
        OutlinedTextField(
          value = bullDetails,
          onValueChange = { bullDetails = it },
          label = { Text(if (isMr) "वळू / सिमेन स्ट्रा तपशील" else "Bull / Semen Straw Details") },
          placeholder = { Text(if (isMr) "उदा. HF 4088 Sexed Semen / Girraj" else "e.g. HF ABS Bull #4088") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_bull_details_input"),
          singleLine = true
        )

        // Doctor / AI Inseminator Name
        OutlinedTextField(
          value = doctorName,
          onValueChange = { doctorName = it },
          label = { Text(if (isMr) "डॉक्टर / रेतन तंत्रज्ञांचे नाव" else "Doctor / Inseminator Name") },
          placeholder = { Text(if (isMr) "उदा. डॉ. पाटील" else "e.g. Dr. Patil") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_doctor_name_input"),
          singleLine = true
        )

        // Notes
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text(if (isMr) "इतर टीप / शेरा" else "Notes / Observations") },
          placeholder = { Text(if (isMr) "उदा. दुसरा माज, योग्य वेळात रेतन" else "e.g. Standing heat, good timing") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_notes_input"),
          maxLines = 2
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          selectedCow?.let { cow ->
            onSave(
              cow.id,
              "${cow.name} (${cow.tagNumber})",
              aiDate,
              bullDetails.trim(),
              doctorName.trim(),
              notes.trim()
            )
          }
        },
        enabled = selectedCow != null,
        modifier = Modifier.testTag("save_ai_record_button")
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
        Text(AppStrings.save(lang))
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("cancel_ai_record_button")
      ) {
        Text(AppStrings.cancel(lang))
      }
    }
  )
}
