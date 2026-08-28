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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ReceiptLong
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.ExpenseCategory
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
  lang: AppLanguage,
  onDismiss: () -> Unit,
  onSave: (date: Long, category: ExpenseCategory, amount: Double, description: String) -> Unit
) {
  val isMr = lang == AppLanguage.MARATHI

  var selectedDate by remember { mutableStateOf(DateUtils.getStartOfDay(System.currentTimeMillis())) }
  var showDatePicker by remember { mutableStateOf(false) }

  var selectedCategory by remember { mutableStateOf(ExpenseCategory.CATTLE_FEED) }
  var categoryDropdownExpanded by remember { mutableStateOf(false) }

  var amountText by remember { mutableStateOf("") }
  var descriptionText by remember { mutableStateOf("") }

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
          modifier = Modifier.testTag("confirm_expense_date_button")
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
          Icons.Default.ReceiptLong,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(end = 8.dp)
        )
        Text(
          text = if (isMr) "गोठा खर्च नोंदवा (Add Expense)" else "Record Farm Expense",
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
        // Date
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
                text = if (isMr) "खर्चाची तारीख (Date):" else "Expense Date:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = DateUtils.formatDate(selectedDate),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }
            IconButton(
              onClick = { showDatePicker = true },
              modifier = Modifier.testTag("change_expense_date_button")
            ) {
              Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date", tint = MaterialTheme.colorScheme.primary)
            }
          }
        }

        // Category Selector
        ExposedDropdownMenuBox(
          expanded = categoryDropdownExpanded,
          onExpandedChange = { categoryDropdownExpanded = it },
          modifier = Modifier.fillMaxWidth()
        ) {
          val categoryLabel = when (selectedCategory) {
            ExpenseCategory.CATTLE_FEED -> if (isMr) "पशूखाद्य, पेंड, सुग्रास (Cattle Feed)" else "Cattle Feed & Concentrates"
            ExpenseCategory.FODDER -> if (isMr) "हिरवा / सुका चारा (Green/Dry Fodder)" else "Fodder & Hay"
            ExpenseCategory.MEDICAL_VET -> if (isMr) "डॉक्टर व औषधोपचार (Vet & Medicines)" else "Vet & Medicines"
            ExpenseCategory.AI_BREEDING -> if (isMr) "कृत्रिम रेतन खर्च (AI / Insemination)" else "AI & Breeding Fee"
            ExpenseCategory.LABOR -> if (isMr) "मजुरी व कामगार (Labor & Milking)" else "Labor & Milking"
            ExpenseCategory.ELECTRICITY_MAINT -> if (isMr) "वीज, पाणी व देखभाल (Maint & Power)" else "Maintenance & Power"
            ExpenseCategory.OTHER -> if (isMr) "इतर खर्च (Miscellaneous)" else "Miscellaneous"
          }
          OutlinedTextField(
            value = categoryLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(if (isMr) "खर्चाचा प्रकार (Category) *" else "Expense Category *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
              .testTag("expense_category_dropdown")
          )
          ExposedDropdownMenu(
            expanded = categoryDropdownExpanded,
            onDismissRequest = { categoryDropdownExpanded = false }
          ) {
            ExpenseCategory.values().forEach { cat ->
              val itemLabel = when (cat) {
                ExpenseCategory.CATTLE_FEED -> if (isMr) "पशूखाद्य, पेंड, सुग्रास (Cattle Feed)" else "Cattle Feed & Concentrates"
                ExpenseCategory.FODDER -> if (isMr) "हिरवा / सुका चारा (Green/Dry Fodder)" else "Fodder & Hay"
                ExpenseCategory.MEDICAL_VET -> if (isMr) "डॉक्टर व औषधोपचार (Vet & Medicines)" else "Vet & Medicines"
                ExpenseCategory.AI_BREEDING -> if (isMr) "कृत्रिम रेतन खर्च (AI / Insemination)" else "AI & Breeding Fee"
                ExpenseCategory.LABOR -> if (isMr) "मजुरी व कामगार (Labor & Milking)" else "Labor & Milking"
                ExpenseCategory.ELECTRICITY_MAINT -> if (isMr) "वीज, पाणी व देखभाल (Maint & Power)" else "Maintenance & Power"
                ExpenseCategory.OTHER -> if (isMr) "इतर खर्च (Miscellaneous)" else "Miscellaneous"
              }
              DropdownMenuItem(
                text = { Text(itemLabel) },
                onClick = {
                  selectedCategory = cat
                  categoryDropdownExpanded = false
                }
              )
            }
          }
        }

        // Amount Input
        OutlinedTextField(
          value = amountText,
          onValueChange = { amountText = it },
          label = { Text(if (isMr) "खर्च रक्कम (₹) *" else "Amount (₹) *") },
          placeholder = { Text("उदा. 2500") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("expense_amount_input"),
          singleLine = true
        )

        // Description / Details
        OutlinedTextField(
          value = descriptionText,
          onValueChange = { descriptionText = it },
          label = { Text(if (isMr) "तपशील / वर्णन" else "Description / Bill details") },
          placeholder = { Text(if (isMr) "उदा. २ पोती सरकी पेंड खरेदी" else "e.g. 2 bags cattle feed") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("expense_desc_input"),
          maxLines = 2
        )
      }
    },
    confirmButton = {
      val amount = amountText.toDoubleOrNull() ?: 0.0
      Button(
        onClick = {
          if (amount > 0) {
            onSave(selectedDate, selectedCategory, amount, descriptionText.trim())
          }
        },
        enabled = amount > 0,
        modifier = Modifier.testTag("save_expense_button")
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
        Text(AppStrings.save(lang))
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("cancel_expense_button")
      ) {
        Text(AppStrings.cancel(lang))
      }
    }
  )
}
