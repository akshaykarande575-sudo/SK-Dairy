package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MilkEntry
import com.example.data.model.MilkSession
import com.example.util.AppLanguage
import com.example.util.DateUtils
import com.example.util.TenDayPeriodSummary
import com.example.util.WhatsAppShareHelper

@Composable
fun TenDayBillingDialog(
  milkEntries: List<MilkEntry>,
  year: Int,
  month: Int,
  farmName: String,
  lang: AppLanguage,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val isMr = lang == AppLanguage.MARATHI

  val periods = remember(milkEntries, year, month) {
    WhatsAppShareHelper.calculateTenDayPeriods(milkEntries, year, month)
  }

  var selectedPeriodIndex by remember { mutableIntStateOf(0) }
  val activePeriod = periods.getOrElse(selectedPeriodIndex) { periods[0] }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 500.dp)
        .testTag("ten_day_billing_dialog")
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        // Dialog Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF2E7D32)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = if (isMr) "१०-दिवसीय बिल व अहवाल" else "10-Day Milk Billing",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = farmName.ifBlank { "SK Dairy Farm" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
              )
            }
          }

          IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_10day_dialog_button")) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Period Tab Selector (1-10, 11-20, 21-End)
        ScrollableTabRow(
          selectedTabIndex = selectedPeriodIndex,
          edgePadding = 0.dp,
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
        ) {
          periods.forEachIndexed { index, period ->
            val label = when (index) {
              0 -> if (isMr) "१ ते १० तारीख" else "1st - 10th"
              1 -> if (isMr) "११ ते २० तारीख" else "11th - 20th"
              else -> if (isMr) "२१ ते अखेर" else "21st - End"
            }
            Tab(
              selected = selectedPeriodIndex == index,
              onClick = { selectedPeriodIndex = index },
              text = {
                Text(
                  text = label,
                  fontWeight = if (selectedPeriodIndex == index) FontWeight.Bold else FontWeight.Normal
                )
              },
              modifier = Modifier.testTag("billing_period_tab_$index")
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Active Period Card Summary
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = activePeriod.dateRangeText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
              ) {
                Text(
                  text = "${activePeriod.entriesCount} ${if (isMr) "नोंदी" else "logs"}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimary,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Total Liters & Total Amount
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = if (isMr) "एकूण दूध संकलन" else "Total Milk",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                  text = DateUtils.formatLiters(activePeriod.totalLiters),
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.ExtraBold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = if (isMr) "एकूण देय रक्कम (Bill)" else "Total Bill Amount",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                  text = DateUtils.formatCurrency(activePeriod.totalAmount),
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color(0xFF1B5E20)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Breakdown Stats
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                  RoundedCornerShape(10.dp)
                )
                .padding(10.dp),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = if (isMr) "सकाळ" else "Morning", style = MaterialTheme.typography.labelSmall)
                Text(text = "${String.format("%.1f", activePeriod.morningLiters)}L", fontWeight = FontWeight.Bold)
              }
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = if (isMr) "संध्याकाळ" else "Evening", style = MaterialTheme.typography.labelSmall)
                Text(text = "${String.format("%.1f", activePeriod.eveningLiters)}L", fontWeight = FontWeight.Bold)
              }
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = if (isMr) "सरासरी फॅट" else "Avg Fat", style = MaterialTheme.typography.labelSmall)
                Text(text = "${String.format("%.1f", activePeriod.avgFat)}%", fontWeight = FontWeight.Bold)
              }
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = if (isMr) "सरासरी दर" else "Avg Rate", style = MaterialTheme.typography.labelSmall)
                Text(text = "₹${String.format("%.1f", activePeriod.avgRate)}", fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // WhatsApp Share Button (Prominent & Green)
        Button(
          onClick = {
            val shareText = WhatsAppShareHelper.generateTenDayBillingWhatsAppText(
              farmName = farmName,
              periodSummary = activePeriod
            )
            WhatsAppShareHelper.shareViaWhatsApp(context, shareText)
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF25D366),
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("share_whatsapp_10day_bill_button")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              Icons.Default.Share,
              contentDescription = "Share on WhatsApp",
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isMr) "व्हाट्सअॅपवर बिल पाठवा (Share on WhatsApp)" else "Share Bill on WhatsApp",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Entries List in Period
        Text(
          text = if (isMr) "कालावधीतील नोंदी (${activePeriod.entries.size})" else "Daily Records (${activePeriod.entries.size})",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (activePeriod.entries.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(100.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (isMr) "या १० दिवसांच्या कालावधीत कोणतीही नोंद नाही." else "No records in this 10-day period.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.outline
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            items(activePeriod.entries, key = { it.id }) { entry ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(
                      text = "${DateUtils.formatShortDate(entry.date)} (${if (entry.session == MilkSession.MORNING) if (isMr) "सकाळ" else "Morn" else if (isMr) "संध्याकाळ" else "Eve"})",
                      style = MaterialTheme.typography.bodySmall,
                      fontWeight = FontWeight.SemiBold
                    )
                    Text(
                      text = "${DateUtils.formatLiters(entry.liters)} | ${entry.fat}% F | ₹${entry.ratePerLiter}/L",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.outline
                    )
                  }

                  Text(
                    text = DateUtils.formatCurrency(entry.totalAmount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
