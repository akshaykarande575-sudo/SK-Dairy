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
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntry
import com.example.ui.MonthSummary
import com.example.ui.dialogs.AddExpenseDialog
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.DateUtils

import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import com.example.util.WhatsAppShareHelper

@Composable
fun ProfitCalculatorScreen(
  monthSummary: MonthSummary,
  selectedMonthOffset: Int,
  expenses: List<ExpenseEntry>,
  lang: AppLanguage,
  farmName: String = "SK Dairy",
  onMonthChange: (Int) -> Unit,
  onAddExpense: (date: Long, category: ExpenseCategory, amount: Double, description: String) -> Unit,
  onDeleteExpense: (ExpenseEntry) -> Unit
) {
  val context = LocalContext.current
  var showAddExpenseDialog by remember { mutableStateOf(false) }
  val isMr = lang == AppLanguage.MARATHI

  val isProfitable = monthSummary.netProfit >= 0
  val profitMarginPercent = if (monthSummary.totalRevenue > 0) {
    (monthSummary.netProfit / monthSummary.totalRevenue) * 100
  } else 0.0

  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("profit_screen_list"),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Month Switcher Header
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = { onMonthChange(selectedMonthOffset - 1) },
              modifier = Modifier.testTag("prev_month_button")
            ) {
              Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Previous Month")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = monthSummary.monthName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = if (isMr) "महिना अखेर एकूण हिशोब" else "Month-End Final Balance",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
              )
            }

            IconButton(
              onClick = { onMonthChange(selectedMonthOffset + 1) },
              enabled = selectedMonthOffset < 0,
              modifier = Modifier.testTag("next_month_button")
            ) {
              Icon(Icons.Default.ArrowForwardIos, contentDescription = "Next Month")
            }
          }
        }
      }

      // Big Net Profit / Loss Highlight Card
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isProfitable) Color(0xFF1B5E20) else Color(0xFFB71C1C)
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (isProfitable) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isProfitable) (if (isMr) "निव्वळ नफा (Net Profit)" else "Net Profit") else (if (isMr) "तोटा (Net Loss)" else "Net Loss"),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }

              Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = "${String.format("%.1f", profitMarginPercent)}% ${if (isMr) "नफा प्रमाण" else "Margin"}",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = DateUtils.formatCurrency(monthSummary.netProfit),
              style = MaterialTheme.typography.displaySmall,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "${if (isMr) "दूध उत्पन्न:" else "Income:"} ${DateUtils.formatCurrency(monthSummary.totalRevenue)}  |  ${if (isMr) "एकूण खर्च:" else "Expenses:"} ${DateUtils.formatCurrency(monthSummary.totalExpenses)}",
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // WhatsApp Share Profit/Loss Summary Button
            Button(
              onClick = {
                val text = WhatsAppShareHelper.generateMonthlySummaryWhatsAppText(
                  farmName = farmName,
                  monthSummary = monthSummary
                )
                WhatsAppShareHelper.shareViaWhatsApp(context, text)
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF25D366),
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("share_whatsapp_profit_summary_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  Icons.Default.Share,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isMr) "व्हाट्सअॅपवर नफा-तोटा अहवाल पाठवा" else "Share Profit Report on WhatsApp",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }

      // Summary 2-Card Row: Milk Sales vs Farm Expenses
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Milk Sales Card
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.weight(1f)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalDrink, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isMr) "दूध विक्री" else "Milk Sales", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = DateUtils.formatCurrency(monthSummary.totalRevenue),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                text = "${DateUtils.formatLiters(monthSummary.totalLiters)} | ₹${String.format("%.1f", monthSummary.avgRate)}/L",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
              )
            }
          }

          // Expenses Card
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier.weight(1f)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isMr) "गोठा खर्च" else "Expenses", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = DateUtils.formatCurrency(monthSummary.totalExpenses),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
              )
              Text(
                text = "${expenses.size} ${if (isMr) "खर्च नोंदी" else "bills recorded"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
              )
            }
          }
        }
      }

      // Expense Category Breakdown
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = if (isMr) "खर्च वर्गवारी व तपशील (Expense Breakdown)" else "Expense Category Breakdown",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            ExpenseCategory.values().forEach { cat ->
              val catAmount = monthSummary.expenseBreakdown[cat] ?: 0.0
              if (catAmount > 0 || cat == ExpenseCategory.CATTLE_FEED || cat == ExpenseCategory.FODDER) {
                val catLabel = when (cat) {
                  ExpenseCategory.CATTLE_FEED -> if (isMr) "पशूखाद्य, पेंड, सुग्रास" else "Cattle Feed & Cakes"
                  ExpenseCategory.FODDER -> if (isMr) "हिरवा / सुका चारा" else "Green & Dry Fodder"
                  ExpenseCategory.MEDICAL_VET -> if (isMr) "डॉक्टर व औषधोपचार" else "Vet & Medicines"
                  ExpenseCategory.AI_BREEDING -> if (isMr) "कृत्रिम रेतन (AI) खर्च" else "AI & Breeding"
                  ExpenseCategory.LABOR -> if (isMr) "मजुरी व कामगार" else "Labor Charges"
                  ExpenseCategory.ELECTRICITY_MAINT -> if (isMr) "वीज, पाणी व देखभाल" else "Maintenance & Power"
                  ExpenseCategory.OTHER -> if (isMr) "इतर गोठा खर्च" else "Miscellaneous"
                }

                val progress = if (monthSummary.totalExpenses > 0) (catAmount / monthSummary.totalExpenses).toFloat() else 0f

                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(text = catLabel, style = MaterialTheme.typography.bodyMedium)
                    Text(text = DateUtils.formatCurrency(catAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                  }
                  Spacer(modifier = Modifier.height(3.dp))
                  LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(6.dp)
                      .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                  )
                }
              }
            }
          }
        }
      }

      // Month Expenses List Section Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isMr) "खर्च नोंदी (Expenses Log)" else "Recorded Expenses",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
        }
      }

      if (expenses.isEmpty()) {
        item {
          Text(
            text = if (isMr) "या महिन्यात कोणताही खर्च नोंदवला नाही." else "No expenses recorded this month.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(vertical = 8.dp)
          )
        }
      } else {
        items(expenses, key = { it.id }) { expense ->
          ExpenseItemCard(
            expense = expense,
            lang = lang,
            onDelete = { onDeleteExpense(expense) }
          )
        }
      }
    }

    // Add Expense FAB
    FloatingActionButton(
      onClick = { showAddExpenseDialog = true },
      containerColor = MaterialTheme.colorScheme.secondary,
      contentColor = MaterialTheme.colorScheme.onSecondary,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp)
        .testTag("add_expense_fab")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Expense")
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isMr) "खर्च नोंदवा" else "Add Expense",
          fontWeight = FontWeight.Bold
        )
      }
    }
  }

  if (showAddExpenseDialog) {
    AddExpenseDialog(
      lang = lang,
      onDismiss = { showAddExpenseDialog = false },
      onSave = { date, category, amount, description ->
        onAddExpense(date, category, amount, description)
        showAddExpenseDialog = false
      }
    )
  }
}

@Composable
fun ExpenseItemCard(
  expense: ExpenseEntry,
  lang: AppLanguage,
  onDelete: () -> Unit
) {
  val isMr = lang == AppLanguage.MARATHI

  val catLabel = when (expense.category) {
    ExpenseCategory.CATTLE_FEED -> if (isMr) "पशूखाद्य / पेंड" else "Cattle Feed"
    ExpenseCategory.FODDER -> if (isMr) "हिरवा / सुका चारा" else "Fodder"
    ExpenseCategory.MEDICAL_VET -> if (isMr) "डॉक्टर व औषधोपचार" else "Vet & Medicine"
    ExpenseCategory.AI_BREEDING -> if (isMr) "कृत्रिम रेतन (AI)" else "AI Breeding"
    ExpenseCategory.LABOR -> if (isMr) "मजुरी व कामगार" else "Labor"
    ExpenseCategory.ELECTRICITY_MAINT -> if (isMr) "वीज व देखभाल" else "Maintenance"
    ExpenseCategory.OTHER -> if (isMr) "इतर खर्च" else "Other"
  }

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = catLabel,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        if (expense.description.isNotBlank()) {
          Text(
            text = expense.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Text(
          text = DateUtils.formatDate(expense.date),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.outline
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = DateUtils.formatCurrency(expense.amount),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.error
        )
        IconButton(
          onClick = onDelete,
          modifier = Modifier.testTag("delete_expense_${expense.id}")
        ) {
          Icon(Icons.Default.DeleteOutline, contentDescription = "Delete expense", tint = MaterialTheme.colorScheme.outline)
        }
      }
    }
  }
}
