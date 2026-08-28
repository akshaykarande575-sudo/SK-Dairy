package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Cow
import com.example.data.model.MilkSession
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMilkDialog(
  cows: List<Cow>,
  lang: AppLanguage,
  onDismiss: () -> Unit,
  onSave: (date: Long, cowId: Long?, cowName: String, session: MilkSession, liters: Double, fat: Double, snf: Double, rate: Double, dairyName: String) -> Unit
) {
  var selectedDate by remember { mutableStateOf(DateUtils.getStartOfDay(System.currentTimeMillis())) }
  var showDatePicker by remember { mutableStateOf(false) }
  
  var selectedSession by remember { mutableStateOf(MilkSession.MORNING) }
  var selectedCowId by remember { mutableStateOf<Long?>(null) }
  var selectedCowName by remember { mutableStateOf(if (lang == AppLanguage.MARATHI) "गोठा एकूण दूध (All Herd)" else "All Herd Bulk Milk") }
  var cowDropdownExpanded by remember { mutableStateOf(false) }

  var litersText by remember { mutableStateOf("") }
  var fatText by remember { mutableStateOf("3.8") }
  var snfText by remember { mutableStateOf("8.5") }
  var rateText by remember { mutableStateOf("38.0") }
  var dairyCenterText by remember { mutableStateOf(if (lang == AppLanguage.MARATHI) "गोकुळ दूध संकलन केंद्र" else "Dairy Collection Center") }

  val isMr = lang == AppLanguage.MARATHI

  val liters = litersText.toDoubleOrNull() ?: 0.0
  val rate = rateText.toDoubleOrNull() ?: 0.0
  val calculatedTotal = liters * rate

  if (showDatePicker) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let {
              selectedDate = DateUtils.getStartOfDay(it)
            }
            showDatePicker = false
          },
          modifier = Modifier.testTag("confirm_date_button")
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
          Icons.Default.WaterDrop,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(end = 8.dp)
        )
        Text(
          text = if (isMr) "दैनंदिन दूध नोंद करा" else "Record Daily Milk",
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
        // Date Selector
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
                text = if (isMr) "तारीख (Date):" else "Date:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = DateUtils.formatDate(selectedDate),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
              )
            }
            IconButton(
              onClick = { showDatePicker = true },
              modifier = Modifier.testTag("change_date_button")
            ) {
              Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date", tint = MaterialTheme.colorScheme.primary)
            }
          }
        }

        // Morning / Evening Session Radio Buttons
        Text(
          text = if (isMr) "वेळ (Session):" else "Session:",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Medium
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .selectable(
                selected = selectedSession == MilkSession.MORNING,
                onClick = { selectedSession = MilkSession.MORNING },
                role = Role.RadioButton
              )
              .testTag("session_morning")
          ) {
            RadioButton(
              selected = selectedSession == MilkSession.MORNING,
              onClick = null
            )
            Text(
              text = if (isMr) "🌅 सकाळ (Morning)" else "🌅 Morning",
              modifier = Modifier.padding(start = 6.dp)
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .selectable(
                selected = selectedSession == MilkSession.EVENING,
                onClick = { selectedSession = MilkSession.EVENING },
                role = Role.RadioButton
              )
              .testTag("session_evening")
          ) {
            RadioButton(
              selected = selectedSession == MilkSession.EVENING,
              onClick = null
            )
            Text(
              text = if (isMr) "🌇 संध्याकाळ (Evening)" else "🌇 Evening",
              modifier = Modifier.padding(start = 6.dp)
            )
          }
        }

        // Cow Selector Dropdown
        ExposedDropdownMenuBox(
          expanded = cowDropdownExpanded,
          onExpandedChange = { cowDropdownExpanded = it },
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = selectedCowName,
            onValueChange = {},
            readOnly = true,
            label = { Text(if (isMr) "गाय निवडा किंवा एकूण गोठा" else "Select Cow / All Herd") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cowDropdownExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
              .testTag("cow_selector_dropdown")
          )
          ExposedDropdownMenu(
            expanded = cowDropdownExpanded,
            onDismissRequest = { cowDropdownExpanded = false }
          ) {
            val bulkLabel = if (isMr) "गोठा एकूण दूध (All Herd Bulk)" else "All Herd Bulk Milk"
            DropdownMenuItem(
              text = { Text(bulkLabel, fontWeight = FontWeight.Bold) },
              onClick = {
                selectedCowId = null
                selectedCowName = bulkLabel
                cowDropdownExpanded = false
              }
            )
            cows.forEach { cow ->
              DropdownMenuItem(
                text = { Text("${cow.tagNumber} - ${cow.name} (${cow.breed})") },
                onClick = {
                  selectedCowId = cow.id
                  selectedCowName = "${cow.tagNumber} - ${cow.name}"
                  cowDropdownExpanded = false
                }
              )
            }
          }
        }

        // Liters Input (Mandatory)
        OutlinedTextField(
          value = litersText,
          onValueChange = { litersText = it },
          label = { Text(if (isMr) "दूध प्रमाण (लिटर) *" else "Milk Quantity (Liters) *") },
          placeholder = { Text("उदा. 24.5") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("milk_liters_input"),
          singleLine = true
        )

        // Fat % & SNF Inputs
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = fatText,
            onValueChange = { fatText = it },
            label = { Text(if (isMr) "फॅट (Fat %)" else "Fat %") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
              .weight(1f)
              .testTag("milk_fat_input"),
            singleLine = true
          )
          OutlinedTextField(
            value = snfText,
            onValueChange = { snfText = it },
            label = { Text(if (isMr) "एसएनएफ (SNF)" else "SNF") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
              .weight(1f)
              .testTag("milk_snf_input"),
            singleLine = true
          )
        }

        // Rate per Liter & Auto-Calculated Amount
        OutlinedTextField(
          value = rateText,
          onValueChange = { rateText = it },
          label = { Text(if (isMr) "दर प्रति लिटर (₹) *" else "Rate per Liter (₹) *") },
          placeholder = { Text("उदा. 38.0") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("milk_rate_input"),
          singleLine = true
        )

        // Calculated Total Box
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isMr) "अपेक्षित रक्कम (Total):" else "Calculated Total:",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
              text = DateUtils.formatCurrency(calculatedTotal),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        }

        // Dairy Name (Optional)
        OutlinedTextField(
          value = dairyCenterText,
          onValueChange = { dairyCenterText = it },
          label = { Text(if (isMr) "डेअरी / संकलन केंद्र नाव" else "Dairy Collection Center") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("dairy_center_input"),
          singleLine = true
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (liters > 0) {
            val fat = fatText.toDoubleOrNull() ?: 0.0
            val snf = snfText.toDoubleOrNull() ?: 0.0
            onSave(
              selectedDate,
              selectedCowId,
              selectedCowName,
              selectedSession,
              liters,
              fat,
              snf,
              rate,
              dairyCenterText.trim()
            )
          }
        },
        enabled = liters > 0,
        modifier = Modifier.testTag("save_milk_entry_button")
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
        Text(AppStrings.save(lang))
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("cancel_milk_entry_button")
      ) {
        Text(AppStrings.cancel(lang))
      }
    }
  )
}
