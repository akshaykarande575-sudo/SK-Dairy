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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AlertPriority
import com.example.data.model.AlertType
import com.example.data.model.FarmAlert
import com.example.util.AppLanguage
import com.example.util.DateUtils
import com.example.util.NotificationHelper

@Composable
fun AlertsScreen(
  alerts: List<FarmAlert>,
  lang: AppLanguage,
  onNavigateToBreeding: () -> Unit
) {
  val context = LocalContext.current
  val isMr = lang == AppLanguage.MARATHI

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("alerts_screen_list"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header Explanation Card
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              Icons.Default.NotificationsActive,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isMr) "स्वयंचलित गोठा सूचना व स्मरणपत्रे" else "Automated Smart Dairy Alerts",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = if (isMr) {
              "• प्रसूतीच्या ५ दिवस आधी अलर्ट (5-Day Calving Alert)\n• प्रसूतीनंतर ३ महिन्यांनी नवीन माज व रेतन सूचना (3-Month Post-Calving Alert)\n• ६० दिवसांनी गर्भ तपासणी (PD) स्मरणपत्र"
            } else {
              "• 5 Days prior calving delivery alerts\n• 3 Months post-calving heat & AI reminders\n• 60-day pregnancy diagnosis reminders"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedButton(
            onClick = {
              alerts.firstOrNull()?.let {
                NotificationHelper.showNotification(context, it, isMr)
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("test_push_notification_button")
          ) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isMr) "मोबाईलवर चाचणी नोटिफिकेशन पाठवा" else "Send Test System Notification")
          }
        }
      }
    }

    // Alerts Feed
    if (alerts.isEmpty()) {
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
              Icons.Default.DoneAll,
              contentDescription = null,
              tint = Color(0xFF2E7D32),
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (isMr) "सध्या कोणतीही तातडीची सूचना नाही" else "All up to date! No pending alerts",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = if (isMr) "सर्व गाईंचे प्रसूती व रेतन टप्पे सुरळीत सुरू आहेत." else "All cattle breeding milestones are currently on schedule.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.outline,
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        }
      }
    } else {
      items(alerts, key = { it.id }) { alert ->
        AlertCard(
          alert = alert,
          lang = lang,
          onAction = onNavigateToBreeding
        )
      }
    }
  }
}

@Composable
fun AlertCard(
  alert: FarmAlert,
  lang: AppLanguage,
  onAction: () -> Unit
) {
  val isMr = lang == AppLanguage.MARATHI

  val isDelivery5Days = alert.type == AlertType.DELIVERY_IN_5_DAYS
  val is3MonthsPost = alert.type == AlertType.THREE_MONTHS_POST_DELIVERY

  val cardBg = when (alert.priority) {
    AlertPriority.HIGH -> Color(0xFFFFF0F0)
    AlertPriority.MEDIUM -> Color(0xFFFFF8E1)
    AlertPriority.INFO -> Color(0xFFE8F5E9)
  }

  val accentColor = when (alert.priority) {
    AlertPriority.HIGH -> Color(0xFFC62828)
    AlertPriority.MEDIUM -> Color(0xFFE65100)
    AlertPriority.INFO -> Color(0xFF2E7D32)
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = cardBg),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = CircleShape,
            color = accentColor.copy(alpha = 0.15f),
            modifier = Modifier.size(36.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = if (isDelivery5Days) Icons.Default.Alarm else if (is3MonthsPost) Icons.Default.NotificationsActive else Icons.Default.ChildCare,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
              )
            }
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = if (isMr) alert.titleMr else alert.titleEn,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accentColor
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = if (isMr) alert.messageMr else alert.messageEn,
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF263238)
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${if (isMr) "अपेक्षित दिनांक:" else "Due Date:"} ${DateUtils.formatDate(alert.dueDateEpoch)}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.outline
        )

        Button(
          onClick = onAction,
          colors = ButtonDefaults.buttonColors(containerColor = accentColor),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(if (isMr) "तपशील पहा व नोंद करा" else "View & Update")
        }
      }
    }
  }
}
