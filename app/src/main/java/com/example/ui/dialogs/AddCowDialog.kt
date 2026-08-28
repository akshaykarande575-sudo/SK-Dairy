package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.CowStatus
import com.example.util.AppLanguage
import com.example.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCowDialog(
  lang: AppLanguage,
  onDismiss: () -> Unit,
  onSave: (tagNumber: String, name: String, breed: String, status: CowStatus, avgMilk: Double, notes: String) -> Unit
) {
  val isMr = lang == AppLanguage.MARATHI

  var tagNumber by remember { mutableStateOf("") }
  var name by remember { mutableStateOf("") }
  
  val commonBreeds = listOf(
    "HF Cross (एचएफ संकरित)",
    "Jersey (जर्सी)",
    "Gir (गीर गाय)",
    "Sahiwal (साहिवाल)",
    "Khillar (खिल्लार)",
    "Murrah Buffalo (मुऱ्हा म्हैस)",
    "Jafrabadi Buffalo (जाफराबादी म्हैस)",
    "Local Desi (देशी/गावरान)"
  )
  var breed by remember { mutableStateOf(commonBreeds.first()) }
  var breedDropdownExpanded by remember { mutableStateOf(false) }

  var status by remember { mutableStateOf(CowStatus.MILKING) }
  var statusDropdownExpanded by remember { mutableStateOf(false) }

  var avgMilkText by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Default.Pets,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(end = 8.dp)
        )
        Text(
          text = if (isMr) "नवीन गाय जोडा" else "Add New Cow / Buffalo",
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
        OutlinedTextField(
          value = tagNumber,
          onValueChange = { tagNumber = it },
          label = { Text(if (isMr) "टॅग / बिल्ला क्रमांक (Tag No) *" else "Tag / ID Number *") },
          placeholder = { Text("उदा. MH-105") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("cow_tag_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(if (isMr) "गाईचे नाव (Cow Name) *" else "Cow Name *") },
          placeholder = { Text("उदा. गोदावरी / Godavari") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("cow_name_input"),
          singleLine = true
        )

        // Breed Selector
        ExposedDropdownMenuBox(
          expanded = breedDropdownExpanded,
          onExpandedChange = { breedDropdownExpanded = it },
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = breed,
            onValueChange = {},
            readOnly = true,
            label = { Text(if (isMr) "जात (Breed) *" else "Breed *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = breedDropdownExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
              .testTag("cow_breed_dropdown")
          )
          ExposedDropdownMenu(
            expanded = breedDropdownExpanded,
            onDismissRequest = { breedDropdownExpanded = false }
          ) {
            commonBreeds.forEach { b ->
              DropdownMenuItem(
                text = { Text(b) },
                onClick = {
                  breed = b
                  breedDropdownExpanded = false
                }
              )
            }
          }
        }

        // Status Selector
        ExposedDropdownMenuBox(
          expanded = statusDropdownExpanded,
          onExpandedChange = { statusDropdownExpanded = it },
          modifier = Modifier.fillMaxWidth()
        ) {
          val statusText = when (status) {
            CowStatus.MILKING -> if (isMr) "दुभती (In Milk / Milking)" else "Milking"
            CowStatus.PREGNANT -> if (isMr) "गाभण (Pregnant)" else "Pregnant"
            CowStatus.AI_DONE -> if (isMr) "रेतन झालेली (AI Done)" else "AI Done (Awaiting Check)"
            CowStatus.DRY -> if (isMr) "आटलेली (Dry Period)" else "Dry Cow"
            CowStatus.HEIFER -> if (isMr) "कालवड (Heifer)" else "Heifer"
          }
          OutlinedTextField(
            value = statusText,
            onValueChange = {},
            readOnly = true,
            label = { Text(if (isMr) "सद्यस्थिती (Current Status)" else "Current Status") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
              .testTag("cow_status_dropdown")
          )
          ExposedDropdownMenu(
            expanded = statusDropdownExpanded,
            onDismissRequest = { statusDropdownExpanded = false }
          ) {
            CowStatus.values().forEach { st ->
              val itemText = when (st) {
                CowStatus.MILKING -> if (isMr) "दुभती (In Milk / Milking)" else "Milking"
                CowStatus.PREGNANT -> if (isMr) "गाभण (Pregnant)" else "Pregnant"
                CowStatus.AI_DONE -> if (isMr) "रेतन झालेली (AI Done)" else "AI Done (Awaiting Check)"
                CowStatus.DRY -> if (isMr) "आटलेली (Dry Period)" else "Dry Cow"
                CowStatus.HEIFER -> if (isMr) "कालवड (Heifer)" else "Heifer"
              }
              DropdownMenuItem(
                text = { Text(itemText) },
                onClick = {
                  status = st
                  statusDropdownExpanded = false
                }
              )
            }
          }
        }

        OutlinedTextField(
          value = avgMilkText,
          onValueChange = { avgMilkText = it },
          label = { Text(if (isMr) "दैनंदिन सरासरी दूध (लिटर)" else "Daily Avg Milk (Liters)") },
          placeholder = { Text("उदा. 15.0") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("cow_avg_milk_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text(if (isMr) "इतर माहिती / टीप" else "Notes / History") },
          placeholder = { Text(if (isMr) "उदा. वेत क्रमांक २, निरोगी तब्येत" else "e.g. 2nd Lactation") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("cow_notes_input"),
          maxLines = 2
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (tagNumber.isNotBlank() && name.isNotBlank()) {
            val avgMilk = avgMilkText.toDoubleOrNull() ?: 0.0
            onSave(tagNumber, name, breed, status, avgMilk, notes)
          }
        },
        enabled = tagNumber.isNotBlank() && name.isNotBlank(),
        modifier = Modifier.testTag("save_cow_button")
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
        Text(AppStrings.save(lang))
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("cancel_cow_button")
      ) {
        Text(AppStrings.cancel(lang))
      }
    }
  )
}
