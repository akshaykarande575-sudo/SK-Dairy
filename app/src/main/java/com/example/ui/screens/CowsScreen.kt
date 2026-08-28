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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Cow
import com.example.data.model.CowStatus
import com.example.ui.dialogs.AddCowDialog
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.DateUtils

@Composable
fun CowsScreen(
  cows: List<Cow>,
  lang: AppLanguage,
  onAddCow: (tagNumber: String, name: String, breed: String, status: CowStatus, avgMilk: Double, notes: String) -> Unit,
  onDeleteCow: (Cow) -> Unit
) {
  var showAddCowDialog by remember { mutableStateOf(false) }
  var selectedFilter by remember { mutableStateOf<CowStatus?>(null) }

  val isMr = lang == AppLanguage.MARATHI

  val filteredCows = if (selectedFilter == null) cows else cows.filter { it.status == selectedFilter }

  val milkingCount = cows.count { it.status == CowStatus.MILKING }
  val pregnantCount = cows.count { it.status == CowStatus.PREGNANT }
  val aiDoneCount = cows.count { it.status == CowStatus.AI_DONE }
  val dryCount = cows.count { it.status == CowStatus.DRY }

  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("cows_screen_list"),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Herd Status Overview Card
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
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
                text = if (isMr) "गोपालनातील एकूण जनावरे" else "Dairy Herd Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                text = "${cows.size} ${if (isMr) "गाई / म्हशी" else "Cattle"}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4 mini cards: Milking, Pregnant, AI Done, Dry
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              StatusCounterCard(
                label = if (isMr) "दुभती" else "Milking",
                count = milkingCount,
                color = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
              )
              StatusCounterCard(
                label = if (isMr) "गाभण" else "Pregnant",
                count = pregnantCount,
                color = Color(0xFF1565C0),
                modifier = Modifier.weight(1f)
              )
              StatusCounterCard(
                label = if (isMr) "रेतन (AI)" else "AI Done",
                count = aiDoneCount,
                color = Color(0xFFEF6C00),
                modifier = Modifier.weight(1f)
              )
              StatusCounterCard(
                label = if (isMr) "आटलेली" else "Dry",
                count = dryCount,
                color = Color(0xFF616161),
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }

      // Filter Chips Row
      item {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          item {
            FilterChip(
              selected = selectedFilter == null,
              onClick = { selectedFilter = null },
              label = { Text("${if (isMr) "सर्व" else "All"} (${cows.size})") }
            )
          }
          item {
            FilterChip(
              selected = selectedFilter == CowStatus.MILKING,
              onClick = { selectedFilter = if (selectedFilter == CowStatus.MILKING) null else CowStatus.MILKING },
              label = { Text("${if (isMr) "दुभती" else "Milking"} ($milkingCount)") }
            )
          }
          item {
            FilterChip(
              selected = selectedFilter == CowStatus.PREGNANT,
              onClick = { selectedFilter = if (selectedFilter == CowStatus.PREGNANT) null else CowStatus.PREGNANT },
              label = { Text("${if (isMr) "गाभण" else "Pregnant"} ($pregnantCount)") }
            )
          }
          item {
            FilterChip(
              selected = selectedFilter == CowStatus.AI_DONE,
              onClick = { selectedFilter = if (selectedFilter == CowStatus.AI_DONE) null else CowStatus.AI_DONE },
              label = { Text("${if (isMr) "रेतन (AI)" else "AI Done"} ($aiDoneCount)") }
            )
          }
          item {
            FilterChip(
              selected = selectedFilter == CowStatus.DRY,
              onClick = { selectedFilter = if (selectedFilter == CowStatus.DRY) null else CowStatus.DRY },
              label = { Text("${if (isMr) "आटलेली" else "Dry"} ($dryCount)") }
            )
          }
        }
      }

      // Cattle list
      if (filteredCows.isEmpty()) {
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
                Icons.Default.Pets,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = if (isMr) "कोणतीही गाय आढळली नाही" else "No cows found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = if (isMr) "नवीन गाय जोडण्यासाठी खालील '+' बटनावर क्लिक करा." else "Tap '+' button below to add a cow to your herd.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp)
              )
            }
          }
        }
      } else {
        items(filteredCows, key = { it.id }) { cow ->
          CowCard(
            cow = cow,
            lang = lang,
            onDelete = { onDeleteCow(cow) }
          )
        }
      }
    }

    // Add Cow FAB
    FloatingActionButton(
      onClick = { showAddCowDialog = true },
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp)
        .testTag("add_cow_fab")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Cow")
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isMr) "गाय जोडा" else "Add Cow",
          fontWeight = FontWeight.Bold
        )
      }
    }
  }

  if (showAddCowDialog) {
    AddCowDialog(
      lang = lang,
      onDismiss = { showAddCowDialog = false },
      onSave = { tagNumber, name, breed, status, avgMilk, notes ->
        onAddCow(tagNumber, name, breed, status, avgMilk, notes)
        showAddCowDialog = false
      }
    )
  }
}

@Composable
fun StatusCounterCard(
  label: String,
  count: Int,
  color: Color,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = count.toString(),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = color
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline
      )
    }
  }
}

@Composable
fun CowCard(
  cow: Cow,
  lang: AppLanguage,
  onDelete: () -> Unit
) {
  val isMr = lang == AppLanguage.MARATHI

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(44.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = cow.tagNumber.takeLast(3),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = "${cow.name} (${cow.tagNumber})",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = cow.breed,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.outline
            )
          }
        }

        // Status Badge
        Surface(
          shape = CircleShape,
          color = when (cow.status) {
            CowStatus.MILKING -> Color(0xFFC8E6C9)
            CowStatus.PREGNANT -> Color(0xFFBBDEFB)
            CowStatus.AI_DONE -> Color(0xFFFFE0B2)
            CowStatus.DRY -> Color(0xFFE0E0E0)
            CowStatus.HEIFER -> Color(0xFFE1BEE7)
          }
        ) {
          Text(
            text = when (cow.status) {
              CowStatus.MILKING -> if (isMr) "दुभती" else "Milking"
              CowStatus.PREGNANT -> if (isMr) "गाभण" else "Pregnant"
              CowStatus.AI_DONE -> if (isMr) "रेतन झालेली" else "AI Done"
              CowStatus.DRY -> if (isMr) "आटलेली" else "Dry"
              CowStatus.HEIFER -> if (isMr) "कालवड" else "Heifer"
            },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = when (cow.status) {
              CowStatus.MILKING -> Color(0xFF1B5E20)
              CowStatus.PREGNANT -> Color(0xFF0D47A1)
              CowStatus.AI_DONE -> Color(0xFFE65100)
              CowStatus.DRY -> Color(0xFF424242)
              CowStatus.HEIFER -> Color(0xFF4A148C)
            },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${if (isMr) "दैनिक सरासरी दूध:" else "Daily Avg:"} ${DateUtils.formatLiters(cow.dailyAvgMilk)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
          )
        }

        IconButton(
          onClick = onDelete,
          modifier = Modifier
            .size(32.dp)
            .testTag("delete_cow_${cow.id}")
        ) {
          Icon(Icons.Default.DeleteOutline, contentDescription = "Delete cow", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
        }
      }

      if (cow.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "📝 ${cow.notes}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
