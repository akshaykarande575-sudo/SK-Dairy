package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.MemberRole
import com.example.util.AppLanguage

@Composable
fun FarmSetupScreen(
  language: AppLanguage,
  onLanguageToggle: () -> Unit,
  onCreateFarm: (farmName: String, ownerName: String, contact: String) -> Unit,
  onJoinFarm: (farmCode: String, userName: String, contact: String, role: MemberRole, onResult: (Boolean, String) -> Unit) -> Unit
) {
  val context = LocalContext.current
  val isMr = language == AppLanguage.MARATHI

  // 0 = Create New Farm, 1 = Join Existing Farm
  var selectedTab by remember { mutableIntStateOf(0) }

  // Create Farm State
  var newFarmName by remember { mutableStateOf("") }
  var ownerName by remember { mutableStateOf("") }
  var ownerContact by remember { mutableStateOf("") }

  // Join Farm State
  var joinFarmCode by remember { mutableStateOf("") }
  var joinUserName by remember { mutableStateOf("") }
  var joinUserContact by remember { mutableStateOf("") }
  var joinUserRole by remember { mutableStateOf(MemberRole.EDITOR) }
  var isJoiningLoading by remember { mutableStateOf(false) }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    modifier = Modifier.testTag("farm_setup_screen")
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentAlignment = Alignment.TopCenter
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 600.dp)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Top Language Switcher Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clickable { onLanguageToggle() }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                Icons.Default.Language,
                contentDescription = "Language",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isMr) "English" else "मराठी",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Logo & Title Badge
        Box(
          modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(
                colors = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "🥛",
            style = MaterialTheme.typography.headlineLarge
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = if (isMr) "एस. के. डेअरी फार्म" else "SK Dairy Farm",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.primary
        )

        Text(
          text = if (isMr) "स्मार्ट गोठा व्यवस्थापन व रिअल-टाइम मल्टी-युझर सिंक" else "Smart Dairy Management & Multi-User Realtime Cloud Sync",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.outline,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tab Selection: Create New vs Join Existing
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (isMr) "नवीन गोठा तयार करा" else "Create New Farm",
                  fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                )
              }
            },
            modifier = Modifier.testTag("tab_create_farm")
          )

          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (isMr) "गोठ्याशी जोडा" else "Join Farm",
                  fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                )
              }
            },
            modifier = Modifier.testTag("tab_join_farm")
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedTab == 0) {
          // --- TAB 0: CREATE NEW FARM ---
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(20.dp)) {
              Text(
                text = if (isMr) "नवीन गोठा नोंदणी (Farm Registration)" else "Register New Farm",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (isMr) "आपल्या डेअरी फार्मचे नाव व माहिती भरा. आपोआप एक युनिक गोठा कोड तयार केला जाईल." else "Enter farm details. A unique farm code will be generated to share with family & workers.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
              )

              Spacer(modifier = Modifier.height(16.dp))

              // Farm Name Field
              OutlinedTextField(
                value = newFarmName,
                onValueChange = { newFarmName = it },
                label = { Text(if (isMr) "गोठ्याचे / डेअरीचे नाव *" else "Farm / Dairy Name *") },
                placeholder = { Text("उदा. SK Dairy, श्री गणेश डेअरी") },
                leadingIcon = {
                  Icon(Icons.Default.AddBusiness, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                  capitalization = KeyboardCapitalization.Words,
                  imeAction = ImeAction.Next
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("setup_farm_name_input")
              )

              Spacer(modifier = Modifier.height(12.dp))

              // Owner Name Field
              OutlinedTextField(
                value = ownerName,
                onValueChange = { ownerName = it },
                label = { Text(if (isMr) "मालकाचे नाव (Owner / Your Name) *" else "Owner Name *") },
                placeholder = { Text("उदा. अक्षय करांडे / Akshay") },
                leadingIcon = {
                  Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                  capitalization = KeyboardCapitalization.Words,
                  imeAction = ImeAction.Next
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("setup_owner_name_input")
              )

              Spacer(modifier = Modifier.height(12.dp))

              // Owner Contact (Optional)
              OutlinedTextField(
                value = ownerContact,
                onValueChange = { ownerContact = it },
                label = { Text(if (isMr) "मोबाईल नंबर (ऐच्छिक)" else "Mobile Number (Optional)") },
                placeholder = { Text("उदा. 98223XXXXX") },
                leadingIcon = {
                  Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                  keyboardType = KeyboardType.Phone,
                  imeAction = ImeAction.Done
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("setup_owner_contact_input")
              )

              Spacer(modifier = Modifier.height(16.dp))

              // Benefit Badges
              Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      Icons.Default.CheckCircle,
                      contentDescription = null,
                      tint = Color(0xFF2E7D32),
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = if (isMr) "तुम्ही मुख्य ॲडमिन (Owner) म्हणून नियुक्त व्हाल" else "You will have full Owner / Admin access",
                      style = MaterialTheme.typography.bodySmall,
                      fontWeight = FontWeight.Medium
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      Icons.Default.CheckCircle,
                      contentDescription = null,
                      tint = Color(0xFF2E7D32),
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = if (isMr) "कुटुंब व कामगारांसाठी गोठा कोड (Farm Code) तयार होईल" else "A unique Farm Code will be generated to share",
                      style = MaterialTheme.typography.bodySmall,
                      fontWeight = FontWeight.Medium
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(20.dp))

              // Create Farm Button
              Button(
                onClick = {
                  if (newFarmName.isBlank()) {
                    Toast.makeText(context, if (isMr) "कृपया गोठ्याचे नाव प्रविष्ट करा" else "Please enter farm name", Toast.LENGTH_SHORT).show()
                    return@Button
                  }
                  if (ownerName.isBlank()) {
                    Toast.makeText(context, if (isMr) "कृपया मालकाचे नाव प्रविष्ट करा" else "Please enter owner name", Toast.LENGTH_SHORT).show()
                    return@Button
                  }
                  onCreateFarm(newFarmName.trim(), ownerName.trim(), ownerContact.trim())
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = Color(0xFF2E7D32),
                  contentColor = Color.White
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(52.dp)
                  .testTag("submit_create_farm_button")
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(20.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = if (isMr) "गोठा तयार करा व सुरू करा (Create Farm)" else "Create Farm & Get Started",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        } else {
          // --- TAB 1: JOIN EXISTING FARM ---
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(20.dp)) {
              Text(
                text = if (isMr) "विद्यमान गोठ्याशी जोडा (Join Existing Farm)" else "Join an Existing Farm",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (isMr) "गोठा मालकाने शेअर केलेला गोठा कोड (Farm Code) प्रविष्ट करा." else "Enter the Farm Code provided by the farm owner to connect and sync records.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
              )

              Spacer(modifier = Modifier.height(16.dp))

              // Farm Code Field
              OutlinedTextField(
                value = joinFarmCode,
                onValueChange = { joinFarmCode = it.uppercase() },
                label = { Text(if (isMr) "गोठा कोड (Farm Code / Invite Code) *" else "Farm Code / Invite Code *") },
                placeholder = { Text("उदा. SK-7890") },
                leadingIcon = {
                  Icon(Icons.Default.QrCode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                  capitalization = KeyboardCapitalization.Characters,
                  imeAction = ImeAction.Next
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("join_farm_code_input")
              )

              Spacer(modifier = Modifier.height(12.dp))

              // Member Name Field
              OutlinedTextField(
                value = joinUserName,
                onValueChange = { joinUserName = it },
                label = { Text(if (isMr) "आपले नाव (Your Name) *" else "Your Name *") },
                placeholder = { Text("उदा. बाबा / सुरेश कामगार") },
                leadingIcon = {
                  Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                  capitalization = KeyboardCapitalization.Words,
                  imeAction = ImeAction.Next
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("join_user_name_input")
              )

              Spacer(modifier = Modifier.height(12.dp))

              // Member Contact
              OutlinedTextField(
                value = joinUserContact,
                onValueChange = { joinUserContact = it },
                label = { Text(if (isMr) "मोबाईल नंबर (ऐच्छिक)" else "Mobile Number (Optional)") },
                placeholder = { Text("उदा. 97654XXXXX") },
                leadingIcon = {
                  Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                  keyboardType = KeyboardType.Phone,
                  imeAction = ImeAction.Done
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("join_user_contact_input")
              )

              Spacer(modifier = Modifier.height(16.dp))

              // Role Selector
              Text(
                text = if (isMr) "आपली भूमिका निवडा:" else "Select Your Role:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
              )

              Spacer(modifier = Modifier.height(6.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                // Editor Role
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = if (joinUserRole == MemberRole.EDITOR) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier
                    .weight(1f)
                    .clickable { joinUserRole = MemberRole.EDITOR }
                ) {
                  Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    RadioButton(
                      selected = joinUserRole == MemberRole.EDITOR,
                      onClick = { joinUserRole = MemberRole.EDITOR }
                    )
                    Column {
                      Text(
                        text = if (isMr) "कामगार / सहाय्यक" else "Editor / Staff",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                      )
                      Text(
                        text = if (isMr) "नोंदी करू शकता" else "Can add milk",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                      )
                    }
                  }
                }

                // Viewer Role
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = if (joinUserRole == MemberRole.VIEWER) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier
                    .weight(1f)
                    .clickable { joinUserRole = MemberRole.VIEWER }
                ) {
                  Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    RadioButton(
                      selected = joinUserRole == MemberRole.VIEWER,
                      onClick = { joinUserRole = MemberRole.VIEWER }
                    )
                    Column {
                      Text(
                        text = if (isMr) "कुटुंब सदस्य" else "Family Viewer",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                      )
                      Text(
                        text = if (isMr) "फक्त पाहू शकता" else "View only",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                      )
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(20.dp))

              // Join Button
              Button(
                onClick = {
                  if (joinFarmCode.isBlank()) {
                    Toast.makeText(context, if (isMr) "कृपया गोठा कोड प्रविष्ट करा" else "Please enter farm code", Toast.LENGTH_SHORT).show()
                    return@Button
                  }
                  if (joinUserName.isBlank()) {
                    Toast.makeText(context, if (isMr) "कृपया आपले नाव प्रविष्ट करा" else "Please enter your name", Toast.LENGTH_SHORT).show()
                    return@Button
                  }

                  isJoiningLoading = true
                  onJoinFarm(
                    joinFarmCode.trim(),
                    joinUserName.trim(),
                    joinUserContact.trim(),
                    joinUserRole
                  ) { success, message ->
                    isJoiningLoading = false
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                  }
                },
                enabled = !isJoiningLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = Color(0xFF1976D2),
                  contentColor = Color.White
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(52.dp)
                  .testTag("submit_join_farm_button")
              ) {
                if (isJoiningLoading) {
                  CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = if (isMr) "गोठ्याशी कनेक्ट व्हा (Join & Sync)" else "Join & Connect Farm",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold
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
}
