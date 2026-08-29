package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FarmMember
import com.example.data.model.FarmProfile
import com.example.data.model.MemberRole
import com.example.util.AppLanguage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TeamSyncDialog(
  farmProfile: FarmProfile,
  lang: AppLanguage,
  onDismiss: () -> Unit,
  onInviteMember: (name: String, contact: String, role: MemberRole) -> Unit,
  onRemoveMember: (memberId: String) -> Unit,
  onUpdateMemberRole: (memberId: String, newRole: MemberRole) -> Unit,
  onSwitchActiveUser: (FarmMember) -> Unit,
  onJoinFarmCode: (code: String) -> Unit,
  onSwitchFarm: () -> Unit = {}
) {
  val isMr = lang == AppLanguage.MARATHI
  val context = LocalContext.current
  var selectedTab by remember { mutableIntStateOf(0) }
  var showSwitchFarmConfirm by remember { mutableStateOf(false) }

  // Invite Form State
  var newMemberName by remember { mutableStateOf("") }
  var newMemberContact by remember { mutableStateOf("") }
  var newMemberRole by remember { mutableStateOf(MemberRole.EDITOR) }
  var roleDropdownExpanded by remember { mutableStateOf(false) }

  // Join Farm Code State
  var joinCodeInput by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          Icons.Default.Group,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
        Text(
          text = if (isMr) "गोठा सदस्य व क्लाउड सिंक" else "Farm Team & Cloud Sync",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Realtime Sync Status Badge Card
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (isMr) "ऑनलाइन रिअल-टाईम सिंक सुरू" else "Realtime Cloud Sync Active",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF2E7D32)
                )
              }
              Text(
                text = "ID: ${farmProfile.inviteCode}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = if (isMr)
                "कुटुंब आणि कामगारांना या कोडने जोडा. एकाने केलेली नोंद सर्व मोबाईलवर लगेच दिसेल."
              else
                "Share this code with family & staff. Records sync instantly across all devices.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action: Copy Code & WhatsApp Share
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                onClick = {
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  val clip = ClipData.newPlainText("Farm Code", farmProfile.inviteCode)
                  clipboard.setPrimaryClip(clip)
                  Toast.makeText(
                    context,
                    if (isMr) "गोठा कोड कॉपी झाला: ${farmProfile.inviteCode}" else "Farm code copied: ${farmProfile.inviteCode}",
                    Toast.LENGTH_SHORT
                  ).show()
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("copy_farm_code_button")
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isMr) "कोड कॉपी" else "Copy Code", fontSize = 12.sp)
              }

              Button(
                onClick = {
                  val shareText = if (isMr)
                    "आमच्या '${farmProfile.farmName}' गोठ्यामध्ये सामील होण्यासाठी SK Dairy ॲपमध्ये हा कोड टाका: ${farmProfile.inviteCode}"
                  else
                    "Join our '${farmProfile.farmName}' on SK Dairy app using Farm Code: ${farmProfile.inviteCode}"
                  val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                  }
                  context.startActivity(Intent.createChooser(sendIntent, "Share Farm Invite Code"))
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("share_farm_code_button")
              ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isMr) "शेअर करा" else "Share Code", fontSize = 12.sp)
              }
            }
          }
        }

        // Tab Navigation: Members List vs Add Member vs Join Farm
        TabRow(selectedTabIndex = selectedTab) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text(if (isMr) "सदस्य (${farmProfile.members.size})" else "Members (${farmProfile.members.size})", fontSize = 12.sp) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text(if (isMr) "+ नवीन जोडा" else "+ Invite", fontSize = 12.sp) }
          )
          Tab(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            text = { Text(if (isMr) "कोड टाका" else "Join Farm", fontSize = 12.sp) }
          )
        }

        // TAB 0: Members List & Profile Switcher
        if (selectedTab == 0) {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height(260.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(farmProfile.members) { member ->
              MemberCard(
                member = member,
                isCurrentUser = member.name == farmProfile.currentUserName || member.isCurrentUser,
                currentUserIsAdmin = farmProfile.currentUserRole == MemberRole.ADMIN,
                isMr = isMr,
                onSelectUser = { onSwitchActiveUser(member) },
                onRemove = { onRemoveMember(member.id) },
                onChangeRole = { newRole -> onUpdateMemberRole(member.id, newRole) }
              )
            }
          }
        }

        // TAB 1: Invite / Add New Member
        if (selectedTab == 1) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .height(260.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Text(
              text = if (isMr) "नवीन सदस्य / कामगाराला जोडा" else "Invite Family Member or Staff",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
              value = newMemberName,
              onValueChange = { newMemberName = it },
              label = { Text(if (isMr) "नाव (उदा. बाबा, सचिन कामगार)" else "Name (e.g. Father, Worker)") },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("new_member_name_input")
            )

            OutlinedTextField(
              value = newMemberContact,
              onValueChange = { newMemberContact = it },
              label = { Text(if (isMr) "मोबाईल नंबर किंवा ईमेल" else "Mobile Number or Email") },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("new_member_contact_input")
            )

            // Role Selector
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isMr) "परवानगी / Role:" else "Role Permission:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
              )

              Box {
                Surface(
                  onClick = { roleDropdownExpanded = true },
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier.testTag("member_role_selector")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = getRoleLabel(newMemberRole, isMr),
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }

                DropdownMenu(
                  expanded = roleDropdownExpanded,
                  onDismissRequest = { roleDropdownExpanded = false }
                ) {
                  DropdownMenuItem(
                    text = { Text(if (isMr) "✍️ सहाय्यक / Editor (नोंद करू शकतात)" else "✍️ Editor (Can Add/Edit)") },
                    onClick = {
                      newMemberRole = MemberRole.EDITOR
                      roleDropdownExpanded = false
                    }
                  )
                  DropdownMenuItem(
                    text = { Text(if (isMr) "👁️ वाचक / Viewer (फक्त पाहू शकतात)" else "👁️ Viewer (View Only)") },
                    onClick = {
                      newMemberRole = MemberRole.VIEWER
                      roleDropdownExpanded = false
                    }
                  )
                }
              }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
              onClick = {
                if (newMemberName.isNotBlank()) {
                  onInviteMember(newMemberName, newMemberContact, newMemberRole)
                  newMemberName = ""
                  newMemberContact = ""
                  selectedTab = 0
                  Toast.makeText(
                    context,
                    if (isMr) "सदस्य यशस्वीरित्या जोडला गेला!" else "Member added successfully!",
                    Toast.LENGTH_SHORT
                  ).show()
                }
              },
              enabled = newMemberName.isNotBlank(),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("submit_invite_button")
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (isMr) "सदस्य जोडा व इनव्हाईट पाठवा" else "Add Member & Send Invite")
            }
          }
        }

        // TAB 2: Join Existing Farm Code
        if (selectedTab == 2) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .height(260.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Text(
              text = if (isMr) "दुसऱ्या गोठ्यामध्ये सामील व्हा" else "Join an Existing Farm",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )

            Text(
              text = if (isMr)
                "गोठा मालकाने (Admin) दिलेला ६ अंकी कोड येथे टाका (उदा. SK-7890)."
              else
                "Enter the Farm Code provided by the Farm Owner (e.g. SK-7890).",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
              value = joinCodeInput,
              onValueChange = { joinCodeInput = it.uppercase() },
              label = { Text(if (isMr) "गोठा कोड (Farm Code)" else "Farm Code") },
              placeholder = { Text("उदा. SK-7890") },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("join_farm_code_input")
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
              onClick = {
                if (joinCodeInput.isNotBlank()) {
                  onJoinFarmCode(joinCodeInput)
                  onDismiss()
                  Toast.makeText(
                    context,
                    if (isMr) "गोठ्याशी यशस्वीरित्या जोडले गेले!" else "Connected to farm successfully!",
                    Toast.LENGTH_SHORT
                  ).show()
                }
              },
              enabled = joinCodeInput.isNotBlank(),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("submit_join_farm_button")
            ) {
              Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (isMr) "गोठ्याशी कनेक्ट व्हा" else "Connect to Farm")
            }
          }
        }
      }
    },
    confirmButton = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextButton(
          onClick = { showSwitchFarmConfirm = true },
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("switch_farm_button")
        ) {
          Text(if (isMr) "गोठा बदला / बाहेर पडा" else "Switch / Exit Farm")
        }

        Button(onClick = onDismiss) {
          Text(if (isMr) "पूर्ण झाले" else "Done")
        }
      }
    }
  )

  if (showSwitchFarmConfirm) {
    AlertDialog(
      onDismissRequest = { showSwitchFarmConfirm = false },
      title = {
        Text(if (isMr) "गोठा बदलायची खात्री आहे का?" else "Switch / Disconnect Farm?")
      },
      text = {
        Text(
          if (isMr)
            "तुम्ही सध्याच्या गोठ्यातून बाहेर पडाल आणि नवीन गोठा तयार करू शकता किंवा दुसऱ्या गोठा कोडने कनेक्ट होऊ शकता."
          else
            "You will disconnect from the current farm session. You can create a new farm or join using another farm code."
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showSwitchFarmConfirm = false
            onDismiss()
            onSwitchFarm()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text(if (isMr) "होय, गोठा बदला" else "Yes, Switch Farm")
        }
      },
      dismissButton = {
        TextButton(onClick = { showSwitchFarmConfirm = false }) {
          Text(if (isMr) "रद्द करा" else "Cancel")
        }
      }
    )
  }
}

@Composable
fun MemberCard(
  member: FarmMember,
  isCurrentUser: Boolean,
  currentUserIsAdmin: Boolean,
  isMr: Boolean,
  onSelectUser: () -> Unit,
  onRemove: () -> Unit,
  onChangeRole: (MemberRole) -> Unit
) {
  var showRoleMenu by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isCurrentUser)
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
      else
        MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onSelectUser() }
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        // Avatar circle
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
              when (member.role) {
                MemberRole.ADMIN -> Color(0xFFFFD54F)
                MemberRole.EDITOR -> Color(0xFF81C784)
                MemberRole.VIEWER -> Color(0xFF90CAF9)
              }
            ),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = member.name.take(1).uppercase(),
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontSize = 14.sp
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = member.name,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold
            )
            if (isCurrentUser) {
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isMr) "(तुम्ही)" else "(You)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
              )
            }
          }

          if (member.contact.isNotBlank()) {
            Text(
              text = member.contact,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp
            )
          }
        }
      }

      // Role Badge and Action
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = when (member.role) {
              MemberRole.ADMIN -> Color(0xFFFFF9C4)
              MemberRole.EDITOR -> Color(0xFFE8F5E9)
              MemberRole.VIEWER -> Color(0xFFE3F2FD)
            },
            modifier = Modifier.clickable(enabled = currentUserIsAdmin && !isCurrentUser) {
              showRoleMenu = true
            }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = getRoleLabel(member.role, isMr),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = when (member.role) {
                  MemberRole.ADMIN -> Color(0xFFF57F17)
                  MemberRole.EDITOR -> Color(0xFF2E7D32)
                  MemberRole.VIEWER -> Color(0xFF1565C0)
                }
              )
            }
          }

          DropdownMenu(
            expanded = showRoleMenu,
            onDismissRequest = { showRoleMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text(if (isMr) "👑 ॲडमिन (Admin)" else "👑 Admin") },
              onClick = {
                onChangeRole(MemberRole.ADMIN)
                showRoleMenu = false
              }
            )
            DropdownMenuItem(
              text = { Text(if (isMr) "✍️ सहाय्यक (Editor)" else "✍️ Editor") },
              onClick = {
                onChangeRole(MemberRole.EDITOR)
                showRoleMenu = false
              }
            )
            DropdownMenuItem(
              text = { Text(if (isMr) "👁️ वाचक (Viewer)" else "👁️ Viewer") },
              onClick = {
                onChangeRole(MemberRole.VIEWER)
                showRoleMenu = false
              }
            )
          }
        }

        if (currentUserIsAdmin && !isCurrentUser) {
          IconButton(
            onClick = onRemove,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              Icons.Default.DeleteOutline,
              contentDescription = "Remove",
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}

fun getRoleLabel(role: MemberRole, isMr: Boolean): String {
  return when (role) {
    MemberRole.ADMIN -> if (isMr) "👑 ॲडमिन" else "👑 Admin"
    MemberRole.EDITOR -> if (isMr) "✍️ सहाय्यक" else "✍️ Editor"
    MemberRole.VIEWER -> if (isMr) "👁️ वाचक" else "👁️ Viewer"
  }
}
