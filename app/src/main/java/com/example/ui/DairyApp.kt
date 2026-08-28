package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.BreedingScreen
import com.example.ui.screens.CowsScreen
import com.example.ui.screens.MilkScreen
import com.example.ui.screens.ProfitCalculatorScreen
import com.example.util.AppLanguage
import com.example.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DairyApp(viewModel: DairyViewModel) {
  var currentTab by remember { mutableStateOf(0) }

  val language by viewModel.language.collectAsStateWithLifecycle()
  val cows by viewModel.cows.collectAsStateWithLifecycle()
  val breedingRecords by viewModel.breedingRecords.collectAsStateWithLifecycle()
  val milkEntries by viewModel.milkEntries.collectAsStateWithLifecycle()
  val expenses by viewModel.expenses.collectAsStateWithLifecycle()
  val farmAlerts by viewModel.farmAlerts.collectAsStateWithLifecycle()
  val monthSummary by viewModel.monthlySummary.collectAsStateWithLifecycle()
  val selectedMonthOffset by viewModel.selectedMonthOffset.collectAsStateWithLifecycle()

  val isMr = language == AppLanguage.MARATHI

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            text = AppStrings.appTitle(language),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = MaterialTheme.colorScheme.onPrimary,
          actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        actions = {
          // Language Switcher Button
          Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(end = 4.dp)
          ) {
            IconButton(
              onClick = { viewModel.toggleLanguage() },
              modifier = Modifier.testTag("language_toggle_button")
            ) {
              Text(
                text = if (isMr) "ENG" else "मराठी",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }

          // Alerts Bell with Badge
          IconButton(
            onClick = { currentTab = 4 },
            modifier = Modifier.testTag("top_alerts_bell_button")
          ) {
            BadgedBox(
              badge = {
                if (farmAlerts.isNotEmpty()) {
                  Badge {
                    Text(text = farmAlerts.size.toString())
                  }
                }
              }
            ) {
              Icon(
                Icons.Default.Notifications,
                contentDescription = "Alerts",
                tint = MaterialTheme.colorScheme.onPrimary
              )
            }
          }
        }
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.testTag("main_navigation_bar")
      ) {
        // Tab 0: Milk Log
        NavigationBarItem(
          selected = currentTab == 0,
          onClick = { currentTab = 0 },
          icon = { Icon(Icons.Default.WaterDrop, contentDescription = "Milk Log") },
          label = { Text(AppStrings.tabMilk(language), fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal) },
          modifier = Modifier.testTag("nav_tab_milk")
        )

        // Tab 1: AI & Breeding
        NavigationBarItem(
          selected = currentTab == 1,
          onClick = { currentTab = 1 },
          icon = { Icon(Icons.Default.Science, contentDescription = "AI & Breeding") },
          label = { Text(AppStrings.tabBreeding(language), fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal) },
          modifier = Modifier.testTag("nav_tab_breeding")
        )

        // Tab 2: Profit Calculator
        NavigationBarItem(
          selected = currentTab == 2,
          onClick = { currentTab = 2 },
          icon = { Icon(Icons.Default.CurrencyRupee, contentDescription = "Profit Calculator") },
          label = { Text(AppStrings.tabProfit(language), fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal) },
          modifier = Modifier.testTag("nav_tab_profit")
        )

        // Tab 3: Cows
        NavigationBarItem(
          selected = currentTab == 3,
          onClick = { currentTab = 3 },
          icon = { Icon(Icons.Default.Pets, contentDescription = "Cattle Herd") },
          label = { Text(AppStrings.tabCows(language), fontWeight = if (currentTab == 3) FontWeight.Bold else FontWeight.Normal) },
          modifier = Modifier.testTag("nav_tab_cows")
        )

        // Tab 4: Alerts
        NavigationBarItem(
          selected = currentTab == 4,
          onClick = { currentTab = 4 },
          icon = {
            BadgedBox(
              badge = {
                if (farmAlerts.isNotEmpty()) {
                  Badge { Text(farmAlerts.size.toString()) }
                }
              }
            ) {
              Icon(Icons.Default.Notifications, contentDescription = "Alerts")
            }
          },
          label = { Text(AppStrings.tabAlerts(language), fontWeight = if (currentTab == 4) FontWeight.Bold else FontWeight.Normal) },
          modifier = Modifier.testTag("nav_tab_alerts")
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentTab) {
        0 -> MilkScreen(
          milkEntries = milkEntries,
          cows = cows,
          monthSummary = monthSummary,
          lang = language,
          onAddMilk = { date, cowId, cowName, session, liters, fat, snf, rate, dairyName ->
            viewModel.addMilkEntry(date, cowId, cowName, session, liters, fat, snf, rate, dairyName)
          },
          onDeleteMilk = { viewModel.deleteMilkEntry(it) }
        )

        1 -> BreedingScreen(
          breedingRecords = breedingRecords,
          cows = cows,
          alerts = farmAlerts,
          lang = language,
          onAddBreeding = { cowId, cowName, aiDate, bullDetails, doctorName, notes ->
            viewModel.addBreedingRecord(cowId, cowName, aiDate, bullDetails, doctorName, notes)
          },
          onConfirmPregnancy = { recordId, isConfirmed ->
            viewModel.confirmPregnancy(recordId, isConfirmed)
          },
          onRecordDelivery = { recordId, deliveryDate, calfGender, notes ->
            viewModel.recordDelivery(recordId, deliveryDate, calfGender, notes)
          },
          onDeleteRecord = { viewModel.deleteBreedingRecord(it) }
        )

        2 -> ProfitCalculatorScreen(
          monthSummary = monthSummary,
          selectedMonthOffset = selectedMonthOffset,
          expenses = expenses,
          lang = language,
          onMonthChange = { viewModel.selectMonthOffset(it) },
          onAddExpense = { date, category, amount, description ->
            viewModel.addExpense(date, category, amount, description)
          },
          onDeleteExpense = { viewModel.deleteExpense(it) }
        )

        3 -> CowsScreen(
          cows = cows,
          lang = language,
          onAddCow = { tagNumber, name, breed, status, avgMilk, notes ->
            viewModel.addCow(tagNumber, name, breed, status, avgMilk, notes)
          },
          onDeleteCow = { viewModel.deleteCow(it) }
        )

        4 -> AlertsScreen(
          alerts = farmAlerts,
          lang = language,
          onNavigateToBreeding = { currentTab = 1 }
        )
      }
    }
  }
}
