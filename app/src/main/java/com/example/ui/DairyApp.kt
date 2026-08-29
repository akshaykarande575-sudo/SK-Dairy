package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Group
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MemberRole
import com.example.ui.dialogs.TeamSyncDialog
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.BreedingScreen
import com.example.ui.screens.CowsScreen
import com.example.ui.screens.FarmSetupScreen
import com.example.ui.screens.MilkScreen
import com.example.ui.screens.ProfitCalculatorScreen
import com.example.util.AppLanguage
import com.example.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DairyApp(viewModel: DairyViewModel) {
  val language by viewModel.language.collectAsStateWithLifecycle()
  val farmProfile by viewModel.farmProfile.collectAsStateWithLifecycle()

  // If farm setup is not yet completed on this device, show First-Launch Onboarding / Setup Flow
  if (!farmProfile.isSetupCompleted) {
    FarmSetupScreen(
      language = language,
      onLanguageToggle = {
        viewModel.setLanguage(
          if (language == AppLanguage.MARATHI) AppLanguage.ENGLISH else AppLanguage.MARATHI
        )
      },
      onCreateFarm = { farmName, ownerName, contact ->
        viewModel.createNewFarm(farmName, ownerName, contact)
      },
      onJoinFarm = { farmCode, userName, contact, role, onComplete ->
        viewModel.joinExistingFarm(farmCode, userName, contact, role, onComplete)
      }
    )
    return
  }

  var currentTab by remember { mutableStateOf(0) }
  var showTeamSyncDialog by remember { mutableStateOf(false) }

  val cows by viewModel.cows.collectAsStateWithLifecycle()
  val breedingRecords by viewModel.breedingRecords.collectAsStateWithLifecycle()
  val milkEntries by viewModel.milkEntries.collectAsStateWithLifecycle()
  val expenses by viewModel.expenses.collectAsStateWithLifecycle()
  val farmAlerts by viewModel.farmAlerts.collectAsStateWithLifecycle()
  val monthSummary by viewModel.monthlySummary.collectAsStateWithLifecycle()
  val selectedMonthOffset by viewModel.selectedMonthOffset.collectAsStateWithLifecycle()
  val defaultBaseRate by viewModel.defaultBaseRate.collectAsStateWithLifecycle()

  val isMr = language == AppLanguage.MARATHI

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        navigationIcon = {
          // Cloud Realtime & Team Member Badge in Top Bar
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
            modifier = Modifier
              .padding(start = 8.dp)
              .clickable { showTeamSyncDialog = true }
              .testTag("team_sync_top_badge")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF4CAF50))
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                Icons.Default.Group,
                contentDescription = "Farm Team",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${farmProfile.members.size}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }
        },
        title = {
          Text(
            text = if (farmProfile.farmName.isNotBlank()) farmProfile.farmName else AppStrings.appTitle(language),
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
              onClick = {
                viewModel.setLanguage(
                  if (language == AppLanguage.MARATHI) AppLanguage.ENGLISH else AppLanguage.MARATHI
                )
              },
              modifier = Modifier.testTag("language_toggle_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
              ) {
                Icon(
                  Icons.Default.Language,
                  contentDescription = "Toggle Language",
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                  text = if (isMr) "EN" else "मराठी",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }
            }
          }
        }
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
      ) {
        NavigationBarItem(
          icon = {
            Icon(Icons.Default.WaterDrop, contentDescription = AppStrings.tabMilk(language))
          },
          label = {
            Text(
              text = AppStrings.tabMilk(language),
              fontSize = 11.sp,
              fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal
            )
          },
          selected = currentTab == 0,
          onClick = { currentTab = 0 },
          modifier = Modifier.testTag("nav_tab_milk")
        )

        NavigationBarItem(
          icon = {
            Icon(Icons.Default.Science, contentDescription = AppStrings.tabBreeding(language))
          },
          label = {
            Text(
              text = AppStrings.tabBreeding(language),
              fontSize = 11.sp,
              fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal
            )
          },
          selected = currentTab == 1,
          onClick = { currentTab = 1 },
          modifier = Modifier.testTag("nav_tab_breeding")
        )

        NavigationBarItem(
          icon = {
            Icon(Icons.Default.CurrencyRupee, contentDescription = AppStrings.tabProfit(language))
          },
          label = {
            Text(
              text = AppStrings.tabProfit(language),
              fontSize = 11.sp,
              fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal
            )
          },
          selected = currentTab == 2,
          onClick = { currentTab = 2 },
          modifier = Modifier.testTag("nav_tab_profit")
        )

        NavigationBarItem(
          icon = {
            Icon(Icons.Default.Pets, contentDescription = AppStrings.tabCows(language))
          },
          label = {
            Text(
              text = AppStrings.tabCows(language),
              fontSize = 11.sp,
              fontWeight = if (currentTab == 3) FontWeight.Bold else FontWeight.Normal
            )
          },
          selected = currentTab == 3,
          onClick = { currentTab = 3 },
          modifier = Modifier.testTag("nav_tab_cows")
        )

        val urgentAlertsCount = farmAlerts.size
        NavigationBarItem(
          icon = {
            if (urgentAlertsCount > 0) {
              BadgedBox(
                badge = {
                  Badge(containerColor = MaterialTheme.colorScheme.error) {
                    Text("$urgentAlertsCount")
                  }
                }
              ) {
                Icon(
                  Icons.Default.Notifications,
                  contentDescription = AppStrings.tabAlerts(language)
                )
              }
            } else {
              Icon(
                Icons.Default.Notifications,
                contentDescription = AppStrings.tabAlerts(language)
              )
            }
          },
          label = {
            Text(
              text = AppStrings.tabAlerts(language),
              fontSize = 11.sp,
              fontWeight = if (currentTab == 4) FontWeight.Bold else FontWeight.Normal
            )
          },
          selected = currentTab == 4,
          onClick = { currentTab = 4 },
          modifier = Modifier.testTag("nav_tab_alerts")
        )
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      when (currentTab) {
        0 -> MilkScreen(
          milkEntries = milkEntries,
          cows = cows,
          monthSummary = monthSummary,
          lang = language,
          currentUserRole = farmProfile.currentUserRole,
          currentUserName = farmProfile.currentUserName,
          farmName = farmProfile.farmName,
          defaultRate = defaultBaseRate,
          onSetDefaultRate = { viewModel.setDefaultBaseRate(it) },
          onClearAllMilk = { viewModel.clearAllMilkEntries() },
          onAddMilk = { date, cowId, cowName, session, liters, fat, snf, rate, dairyName ->
            viewModel.addMilkEntry(
              date = date,
              cowId = cowId,
              cowName = cowName,
              session = session,
              liters = liters,
              fat = fat,
              snf = snf,
              ratePerLiter = rate,
              dairyCenter = dairyName
            )
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
          farmName = farmProfile.farmName,
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

  if (showTeamSyncDialog) {
    TeamSyncDialog(
      farmProfile = farmProfile,
      lang = language,
      onDismiss = { showTeamSyncDialog = false },
      onInviteMember = { name, contact, role -> viewModel.inviteMember(name, contact, role) },
      onRemoveMember = { viewModel.removeMember(it) },
      onUpdateMemberRole = { id, role -> viewModel.updateMemberRole(id, role) },
      onSwitchActiveUser = { viewModel.switchActiveUser(it) },
      onJoinFarmCode = { viewModel.joinFarmCode(it) },
      onSwitchFarm = { viewModel.switchFarmOrLogout() }
    )
  }
}
