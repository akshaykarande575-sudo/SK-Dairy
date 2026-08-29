package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.Cow
import com.example.data.model.CowStatus
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntry
import com.example.data.model.FarmMember
import com.example.data.model.FarmProfile
import com.example.data.model.MemberRole
import com.example.data.model.MilkEntry
import com.example.data.model.MilkSession
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CloudSyncService(
  private val context: Context,
  private val repository: DairyRepository,
  private val scope: CoroutineScope
) {
  private val TAG = "CloudSyncService"

  private val prefs: SharedPreferences =
    context.getSharedPreferences("sk_dairy_farm_session_prefs", Context.MODE_PRIVATE)

  private var firestore: FirebaseFirestore? = null
  private var milkListener: ListenerRegistration? = null
  private var membersListener: ListenerRegistration? = null
  private var isFirebaseAvailable: Boolean = false

  private val _farmProfile = MutableStateFlow(FarmProfile())
  val farmProfile: StateFlow<FarmProfile> = _farmProfile.asStateFlow()

  private val _syncStatusText = MutableStateFlow("🟢 Live Cloud Synced")
  val syncStatusText: StateFlow<String> = _syncStatusText.asStateFlow()

  init {
    loadSavedSession()
    initFirebase()
  }

  private fun loadSavedSession() {
    val isSetup = prefs.getBoolean("key_setup_completed", false)
    if (isSetup) {
      val farmId = prefs.getString("key_farm_id", "") ?: ""
      val farmName = prefs.getString("key_farm_name", "SK Dairy") ?: "SK Dairy"
      val inviteCode = prefs.getString("key_invite_code", "") ?: ""
      val ownerName = prefs.getString("key_owner_name", "") ?: ""
      val ownerContact = prefs.getString("key_owner_contact", "") ?: ""
      val userName = prefs.getString("key_user_name", "") ?: ""
      val roleStr = prefs.getString("key_user_role", MemberRole.ADMIN.name) ?: MemberRole.ADMIN.name
      val userContact = prefs.getString("key_user_contact", "") ?: ""

      val role = try {
        MemberRole.valueOf(roleStr)
      } catch (e: Exception) {
        MemberRole.ADMIN
      }

      val currentMember = FarmMember(
        id = "m_self",
        name = userName.ifBlank { "Farm User" },
        contact = userContact,
        role = role,
        isCurrentUser = true
      )

      _farmProfile.value = FarmProfile(
        isSetupCompleted = true,
        farmId = farmId,
        farmName = farmName,
        inviteCode = inviteCode,
        ownerName = ownerName,
        ownerContact = ownerContact,
        currentUserRole = role,
        currentUserName = userName,
        currentUserContact = userContact,
        isOnlineSyncActive = true,
        isRealtimeConnected = true,
        members = listOf(currentMember)
      )
    } else {
      _farmProfile.value = FarmProfile(isSetupCompleted = false)
    }
  }

  private fun saveSessionToPrefs(profile: FarmProfile) {
    prefs.edit().apply {
      putBoolean("key_setup_completed", profile.isSetupCompleted)
      putString("key_farm_id", profile.farmId)
      putString("key_farm_name", profile.farmName)
      putString("key_invite_code", profile.inviteCode)
      putString("key_owner_name", profile.ownerName)
      putString("key_owner_contact", profile.ownerContact)
      putString("key_user_name", profile.currentUserName)
      putString("key_user_role", profile.currentUserRole.name)
      putString("key_user_contact", profile.currentUserContact)
      apply()
    }
  }

  private fun initFirebase() {
    try {
      val db = FirebaseFirestore.getInstance()
      try {
        // Enable offline persistence settings
        val settings = FirebaseFirestoreSettings.Builder()
          .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
          .build()
        db.firestoreSettings = settings
      } catch (e: Exception) {
        Log.d(TAG, "Firestore settings already configured or non-fatal: ${e.message}")
      }
      firestore = db
      isFirebaseAvailable = true

      if (_farmProfile.value.isSetupCompleted && _farmProfile.value.farmId.isNotBlank()) {
        startRealtimeListeners(_farmProfile.value.farmId)
      }
    } catch (e: Exception) {
      Log.w(TAG, "Firebase not yet active: ${e.message}. Using offline-first cache.")
      isFirebaseAvailable = false
    }
  }

  fun createNewFarm(farmName: String, ownerName: String, ownerContact: String = "") {
    val cleanFarmName = farmName.trim().ifBlank { "SK Dairy Farm" }
    val cleanOwnerName = ownerName.trim().ifBlank { "गोठा मालक" }
    val randomNum = (1000..9999).random()
    val cleanCode = "SK-$randomNum"
    val newFarmId = "farm_${System.currentTimeMillis()}_$randomNum"

    val ownerMember = FarmMember(
      id = "m_owner",
      name = cleanOwnerName,
      contact = ownerContact.trim(),
      role = MemberRole.ADMIN,
      isCurrentUser = true
    )

    val updatedProfile = FarmProfile(
      isSetupCompleted = true,
      farmId = newFarmId,
      farmName = cleanFarmName,
      inviteCode = cleanCode,
      ownerName = cleanOwnerName,
      ownerContact = ownerContact.trim(),
      currentUserRole = MemberRole.ADMIN,
      currentUserName = "$cleanOwnerName (Admin)",
      currentUserContact = ownerContact.trim(),
      isOnlineSyncActive = true,
      isRealtimeConnected = true,
      lastSyncedTimestamp = System.currentTimeMillis(),
      members = listOf(ownerMember)
    )

    _farmProfile.value = updatedProfile
    saveSessionToPrefs(updatedProfile)

    // Sync to Firestore
    firestore?.let { db ->
      scope.launch(Dispatchers.IO) {
        try {
          val farmData = hashMapOf(
            "farmId" to newFarmId,
            "farmName" to cleanFarmName,
            "inviteCode" to cleanCode,
            "ownerName" to cleanOwnerName,
            "ownerContact" to ownerContact.trim(),
            "createdAt" to System.currentTimeMillis()
          )
          db.collection("farms").document(newFarmId).set(farmData, SetOptions.merge())

          // Lookup document by invite code for easy joining
          db.collection("farm_codes").document(cleanCode).set(
            hashMapOf(
              "farmId" to newFarmId,
              "farmName" to cleanFarmName,
              "inviteCode" to cleanCode,
              "ownerName" to cleanOwnerName
            )
          )

          // Add creator to members collection
          db.collection("farms").document(newFarmId).collection("members").document("m_owner").set(
            hashMapOf(
              "name" to cleanOwnerName,
              "contact" to ownerContact.trim(),
              "role" to MemberRole.ADMIN.name,
              "joinedAt" to System.currentTimeMillis()
            )
          )
        } catch (e: Exception) {
          Log.e(TAG, "Error writing new farm to Firestore", e)
        }
      }
    }

    startRealtimeListeners(newFarmId)
  }

  fun joinExistingFarm(
    farmCode: String,
    userName: String,
    userContact: String = "",
    role: MemberRole = MemberRole.EDITOR,
    onComplete: (Boolean, String) -> Unit = { _, _ -> }
  ) {
    val cleanCode = farmCode.trim().uppercase()
    val cleanUserName = userName.trim().ifBlank { "गोठा सहाय्यक" }

    if (cleanCode.isBlank()) {
      onComplete(false, "कृपया वैध गोठा कोड प्रविष्ट करा (Please enter Farm Code)")
      return
    }

    scope.launch(Dispatchers.IO) {
      val db = firestore
      var resolvedFarmId = if (cleanCode.startsWith("SK-")) cleanCode else "SK-$cleanCode"
      var resolvedFarmName = "SK Dairy Farm ($cleanCode)"
      var resolvedOwnerName = "गोठा मालक (Admin)"

      if (db != null) {
        try {
          val codeDoc = db.collection("farm_codes").document(cleanCode).get().awaitIfPossible()
          if (codeDoc != null && codeDoc.exists()) {
            resolvedFarmId = codeDoc.getString("farmId") ?: resolvedFarmId
            resolvedFarmName = codeDoc.getString("farmName") ?: resolvedFarmName
            resolvedOwnerName = codeDoc.getString("ownerName") ?: resolvedOwnerName
          } else {
            // Direct query on farms
            val farmQuery = db.collection("farms").whereEqualTo("inviteCode", cleanCode).limit(1).get().awaitIfPossible()
            if (farmQuery != null && !farmQuery.isEmpty) {
              val firstDoc = farmQuery.documents[0]
              resolvedFarmId = firstDoc.id
              resolvedFarmName = firstDoc.getString("farmName") ?: resolvedFarmName
              resolvedOwnerName = firstDoc.getString("ownerName") ?: resolvedOwnerName
            }
          }
        } catch (e: Exception) {
          Log.w(TAG, "Error querying farm code online: ${e.message}. Using code as farm ID directly.")
        }
      }

      val memberId = "m_${System.currentTimeMillis()}"
      val userMember = FarmMember(
        id = memberId,
        name = cleanUserName,
        contact = userContact.trim(),
        role = role,
        isCurrentUser = true
      )

      val profile = FarmProfile(
        isSetupCompleted = true,
        farmId = resolvedFarmId,
        farmName = resolvedFarmName,
        inviteCode = cleanCode,
        ownerName = resolvedOwnerName,
        ownerContact = "",
        currentUserRole = role,
        currentUserName = cleanUserName,
        currentUserContact = userContact.trim(),
        isOnlineSyncActive = true,
        isRealtimeConnected = true,
        lastSyncedTimestamp = System.currentTimeMillis(),
        members = listOf(userMember)
      )

      withContext(Dispatchers.Main) {
        _farmProfile.value = profile
        saveSessionToPrefs(profile)
      }

      // Sync member record to cloud
      db?.let {
        try {
          it.collection("farms")
            .document(resolvedFarmId)
            .collection("members")
            .document(memberId)
            .set(
              hashMapOf(
                "name" to cleanUserName,
                "contact" to userContact.trim(),
                "role" to role.name,
                "joinedAt" to System.currentTimeMillis()
              )
            )
        } catch (e: Exception) {
          Log.e(TAG, "Error saving joined member to Firestore", e)
        }
      }

      startRealtimeListeners(resolvedFarmId)

      withContext(Dispatchers.Main) {
        onComplete(true, "यशस्वीरित्या गोठ्याशी जोडले गेले! (Connected successfully)")
      }
    }
  }

  fun switchFarmOrLogout() {
    milkListener?.remove()
    membersListener?.remove()

    prefs.edit().clear().apply()

    scope.launch(Dispatchers.IO) {
      repository.clearAllLocalData()
    }

    _farmProfile.value = FarmProfile(isSetupCompleted = false)
    _syncStatusText.value = "Disconnected"
  }

  fun startRealtimeListeners(farmId: String) {
    if (farmId.isBlank()) return
    milkListener?.remove()
    membersListener?.remove()

    val db = firestore ?: return

    try {
      // 1. Realtime Milk Sync Listener
      milkListener = db.collection("farms")
        .document(farmId)
        .collection("milk_entries")
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.e(TAG, "Milk listen failed", error)
            return@addSnapshotListener
          }

          if (snapshot != null) {
            _farmProfile.value = _farmProfile.value.copy(
              lastSyncedTimestamp = System.currentTimeMillis()
            )
            _syncStatusText.value = "🟢 Live Synced (${snapshot.size()} entries)"

            // Pull cloud records to local SQLite Room database in background
            scope.launch(Dispatchers.IO) {
              snapshot.documents.forEach { doc ->
                try {
                  val date = doc.getLong("date") ?: System.currentTimeMillis()
                  val cowName = doc.getString("cowName") ?: "गाय"
                  val sessionStr = doc.getString("session") ?: MilkSession.MORNING.name
                  val session = try { MilkSession.valueOf(sessionStr) } catch (e: Exception) { MilkSession.MORNING }
                  val liters = doc.getDouble("liters") ?: 0.0
                  val fat = doc.getDouble("fat") ?: 0.0
                  val snf = doc.getDouble("snf") ?: 0.0
                  val ratePerLiter = doc.getDouble("ratePerLiter") ?: 0.0
                  val totalAmount = doc.getDouble("totalAmount") ?: (liters * ratePerLiter)
                  val dairyCenterName = doc.getString("dairyCenterName") ?: ""
                  val createdBy = doc.getString("createdBy") ?: ""

                  if (liters > 0) {
                    val entry = MilkEntry(
                      date = date,
                      cowId = 0L,
                      cowName = cowName,
                      session = session,
                      liters = liters,
                      fat = fat,
                      snf = snf,
                      ratePerLiter = ratePerLiter,
                      totalAmount = totalAmount,
                      dairyCenterName = dairyCenterName,
                      createdBy = createdBy
                    )
                    repository.insertMilkEntry(entry)
                  }
                } catch (e: Exception) {
                  Log.e(TAG, "Error inserting synced milk entry", e)
                }
              }
            }
          }
        }

      // 2. Realtime Members Sync Listener
      membersListener = db.collection("farms")
        .document(farmId)
        .collection("members")
        .addSnapshotListener { snapshot, error ->
          if (error == null && snapshot != null) {
            val memberList = snapshot.documents.mapNotNull { doc ->
              val name = doc.getString("name") ?: return@mapNotNull null
              val contact = doc.getString("contact") ?: ""
              val roleStr = doc.getString("role") ?: MemberRole.EDITOR.name
              val role = try { MemberRole.valueOf(roleStr) } catch (e: Exception) { MemberRole.EDITOR }
              val joinedAt = doc.getLong("joinedAt") ?: System.currentTimeMillis()
              val isSelf = name == _farmProfile.value.currentUserName

              FarmMember(
                id = doc.id,
                name = name,
                contact = contact,
                role = role,
                joinedAt = joinedAt,
                isCurrentUser = isSelf
              )
            }

            if (memberList.isNotEmpty()) {
              _farmProfile.value = _farmProfile.value.copy(members = memberList)
            }
          }
        }
    } catch (e: Exception) {
      Log.e(TAG, "Error starting Firestore listener", e)
    }
  }

  fun syncMilkEntryToCloud(entry: MilkEntry) {
    val db = firestore ?: return
    val farmId = _farmProfile.value.farmId
    if (farmId.isBlank()) return
    val currentUserName = _farmProfile.value.currentUserName

    scope.launch(Dispatchers.IO) {
      try {
        val entryData = hashMapOf(
          "date" to entry.date,
          "cowName" to entry.cowName,
          "session" to entry.session.name,
          "liters" to entry.liters,
          "fat" to entry.fat,
          "snf" to entry.snf,
          "ratePerLiter" to entry.ratePerLiter,
          "totalAmount" to entry.totalAmount,
          "dairyCenterName" to entry.dairyCenterName,
          "createdBy" to (if (entry.createdBy.isNotBlank()) entry.createdBy else currentUserName),
          "createdAt" to entry.createdAt,
          "updatedAt" to System.currentTimeMillis()
        )

        val docId = if (entry.id > 0) "milk_${entry.id}" else "milk_${System.currentTimeMillis()}"
        db.collection("farms")
          .document(farmId)
          .collection("milk_entries")
          .document(docId)
          .set(entryData, SetOptions.merge())

        _farmProfile.value = _farmProfile.value.copy(
          lastSyncedTimestamp = System.currentTimeMillis()
        )
      } catch (e: Exception) {
        Log.e(TAG, "Failed to upload milk entry to cloud", e)
      }
    }
  }

  fun inviteMember(name: String, contact: String, role: MemberRole) {
    val newMember = FarmMember(
      id = "m_${System.currentTimeMillis()}",
      name = name.trim(),
      contact = contact.trim(),
      role = role,
      joinedAt = System.currentTimeMillis(),
      isCurrentUser = false
    )

    val updatedMembers = _farmProfile.value.members.toMutableList().apply {
      add(newMember)
    }

    _farmProfile.value = _farmProfile.value.copy(
      members = updatedMembers,
      lastSyncedTimestamp = System.currentTimeMillis()
    )

    firestore?.let { db ->
      val farmId = _farmProfile.value.farmId
      if (farmId.isNotBlank()) {
        scope.launch(Dispatchers.IO) {
          try {
            db.collection("farms")
              .document(farmId)
              .collection("members")
              .document(newMember.id)
              .set(
                mapOf(
                  "name" to newMember.name,
                  "contact" to newMember.contact,
                  "role" to newMember.role.name,
                  "joinedAt" to newMember.joinedAt
                )
              )
          } catch (e: Exception) {
            Log.e(TAG, "Error saving member to Firestore", e)
          }
        }
      }
    }
  }

  fun removeMember(memberId: String) {
    val updatedMembers = _farmProfile.value.members.filterNot { it.id == memberId }
    _farmProfile.value = _farmProfile.value.copy(
      members = updatedMembers,
      lastSyncedTimestamp = System.currentTimeMillis()
    )

    firestore?.let { db ->
      val farmId = _farmProfile.value.farmId
      if (farmId.isNotBlank()) {
        scope.launch(Dispatchers.IO) {
          try {
            db.collection("farms")
              .document(farmId)
              .collection("members")
              .document(memberId)
              .delete()
          } catch (e: Exception) {
            Log.e(TAG, "Error removing member from Firestore", e)
          }
        }
      }
    }
  }

  fun updateMemberRole(memberId: String, newRole: MemberRole) {
    val updatedMembers = _farmProfile.value.members.map {
      if (it.id == memberId) it.copy(role = newRole) else it
    }

    val currentMember = updatedMembers.find { it.isCurrentUser }
    val newCurrentRole = currentMember?.role ?: _farmProfile.value.currentUserRole

    _farmProfile.value = _farmProfile.value.copy(
      members = updatedMembers,
      currentUserRole = newCurrentRole,
      lastSyncedTimestamp = System.currentTimeMillis()
    )
    saveSessionToPrefs(_farmProfile.value)
  }

  fun switchActiveUser(member: FarmMember) {
    val updatedMembers = _farmProfile.value.members.map {
      it.copy(isCurrentUser = it.id == member.id)
    }

    _farmProfile.value = _farmProfile.value.copy(
      members = updatedMembers,
      currentUserName = member.name,
      currentUserContact = member.contact,
      currentUserRole = member.role,
      lastSyncedTimestamp = System.currentTimeMillis()
    )
    saveSessionToPrefs(_farmProfile.value)
  }

  fun joinOrSetFarmCode(farmCode: String, farmName: String = "SK Dairy Farm") {
    val cleanCode = farmCode.trim().uppercase()
    val cleanFarmId = if (cleanCode.startsWith("SK-")) cleanCode else "SK-$cleanCode"

    val updated = _farmProfile.value.copy(
      isSetupCompleted = true,
      farmId = cleanFarmId,
      farmName = farmName,
      inviteCode = cleanCode,
      lastSyncedTimestamp = System.currentTimeMillis()
    )
    _farmProfile.value = updated
    saveSessionToPrefs(updated)
    startRealtimeListeners(cleanFarmId)
  }
}

// Helper extension for Task awaiting without throwing immediately
private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitIfPossible(): T? {
  return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
      continuation.resume(result, null)
    }
    addOnFailureListener {
      continuation.resume(null, null)
    }
  }
}
