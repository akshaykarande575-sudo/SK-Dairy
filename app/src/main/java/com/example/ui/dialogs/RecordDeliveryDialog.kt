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
import androidx.compose.material.icons.filled.ChildCare
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
import com.example.data.model.BreedingRecord
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.DateUtils
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDeliveryDialog(
  record: BreedingRecord,
  lang: AppLanguage,
  onDismiss: () -> Unit,
  onSave: (deliveryDate: Long, calfGender: String, notes: String) -> Unit
) {
  var deliveryDate by remember { mutableStateOf(DateUtils.getStartOfDay(System.currentTimeMillis())) }
  var showDatePicker by remember { mutableStateOf(false) }

  val isMr = lang == AppLanguage.MARATHI

  val calfGenderOptions = if (isMr) {
    listOf("कालवड (मादी वासरू / Female)", "गोऱ्हा (नर वासरू / Male)", "जुळी वासरे (Twins)")
  } else {
    listOf("Female (Heifer Calf)", "Male (Bull Calf)", "Twins")
  }

  var selectedCalfGender by remember { mutableStateOf(calfGenderOptions.first()) }
  var genderDropdownExpanded by remember { mutableStateOf(false) }
  var notes by remember { mutableStateOf("") }

  // 3-month reminder date calculation
  val post3MoDate = deliveryDate + TimeUnit.DAYS.toMillis(90)

  if (showDatePicker) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = deliveryDate)
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let {
              deliveryDate = DateUtils.getStartOfDay(it)
            }
            showDatePicker = false
          },
          modifier = Modifier.testTag("confirm_delivery_date_button")
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
          Icons.Default.ChildCare,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(end = 8.dp)
        )
        Text(
          text = if (isMr) "प्रसूतीची नोंद करा (Calving Record)" else "Record Calving / Delivery",
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
        Text(
          text = "${if (isMr) "गाय:" else "Cow:"} ${record.cowTagOrName}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )

        // Actual Delivery Date
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
                text = if (isMr) "प्रत्यक्ष प्रसूती दिनांक (Delivery Date):" else "Actual Calving Date:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = DateUtils.formatDate(deliveryDate),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }
            IconButton(
              onClick = { showDatePicker = true },
              modifier = Modifier.testTag("change_delivery_date_button")
            ) {
              Icon(Icons.Default.CalendarMonth, contentDescription = "Select Delivery Date", tint = MaterialTheme.colorScheme.primary)
            }
          }
        }

        // Calf Gender Dropdown
        ExposedDropdownMenuBox(
          expanded = genderDropdownExpanded,
          onExpandedChange = { genderDropdownExpanded = it },
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = selectedCalfGender,
            onValueChange = {},
            readOnly = true,
            label = { Text(if (isMr) "वासरू लिंग (Calf Gender) *" else "Calf Gender *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderDropdownExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
              .testTag("calf_gender_dropdown")
          )
          ExposedDropdownMenu(
            expanded = genderDropdownExpanded,
            onDismissRequest = { genderDropdownExpanded = false }
          ) {
            calfGenderOptions.forEach { option ->
              DropdownMenuItem(
                text = { Text(option) },
                onClick = {
                  selectedCalfGender = option
                  genderDropdownExpanded = false
                }
              )
            }
          }
        }

        // Automatic 3-Month Reminder Notice
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = if (isMr) "🔔 प्रसूतीनंतर ३ महिन्यांचे स्मरणपत्र:" else "🔔 3-Month Post-Calving Alert:",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
              text = if (isMr) {
                "${DateUtils.formatDate(post3MoDate)} रोजी नवीन माज व कृत्रिम रेतन (AI) तपासणीची सूचना आपोआप सेट होईल."
              } else {
                "Alert will be scheduled on ${DateUtils.formatDate(post3MoDate)} for next heat cycle and AI planning."
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
          }
        }

        // Notes
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text(if (isMr) "प्रसूती शेरा / वासराबद्दल माहिती" else "Calving Notes / Calf health") },
          placeholder = { Text(if (isMr) "उदा. प्रसूती सुलभ झाली, वासरू निरोगी आहे" else "e.g. Normal calving, healthy calf") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("delivery_notes_input"),
          maxLines = 2
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(deliveryDate, selectedCalfGender, notes.trim())
        },
        modifier = Modifier.testTag("save_delivery_button")
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
        Text(AppStrings.save(lang))
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("cancel_delivery_button")
      ) {
        Text(AppStrings.cancel(lang))
      }
    }
  )
}
