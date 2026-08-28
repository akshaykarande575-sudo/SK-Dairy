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
import com.example.data.model.MilkEntry
import com.example.data.model.MilkSession
import com.example.ui.MonthSummary
import com.example.ui.dialogs.AddMilkDialog
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.DateUtils

@Composable
fun MilkScreen(
  milkEntries: List<MilkEntry>,
  cows: List<Cow>,
  monthSummary: MonthSummary,
  lang: AppLanguage,
  onAddMilk: (date: Long, cowId: Long?, cowName: String, session: MilkSession, liters: Double, fat: Double, snf: Double, rate: Double, dairyName: String) -> Unit,
  onDeleteMilk: (MilkEntry) -> Unit
) {
  var showAddDialog by remember { mutableStateOf(false) }
  val isMr = lang == AppLanguage.MARATHI

  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("milk_screen_list"),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
            onDelete = { onDeleteMilk(entry) }
          )
        }
      }
    }

    // Floating Action Button
    FloatingActionButton(
      onClick = { showAddDialog = true },
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp)
        .testTag("add_milk_fab")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Milk Log")
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isMr) "दूध नोंद" else "Record Milk",
          fontWeight = FontWeight.Bold
        )
      }
    }
  }

  if (showAddDialog) {
    AddMilkDialog(
      cows = cows,
      lang = lang,
      onDismiss = { showAddDialog = false },
      onSave = { date, cowId, cowName, session, liters, fat, snf, rate, dairyName ->
        onAddMilk(date, cowId, cowName, session, liters, fat, snf, rate, dairyName)
        showAddDialog = false
      }
    )
  }
}

@Composable
fun MilkEntryCard(
  entry: MilkEntry,
  lang: AppLanguage,
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

      if (entry.dairyCenterName.isNotBlank()) {
        Text(
          text = "📍 ${entry.dairyCenterName}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.outline,
          modifier = Modifier.padding(top = 6.dp)
        )
      }
    }
  }
}
