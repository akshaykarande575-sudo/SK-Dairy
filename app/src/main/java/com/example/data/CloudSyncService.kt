package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.BreedingRecord
import com.example.data.model.BreedingStatus
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
import kotlinx.coroutines.flow.firstOrNull
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
  private var cowsListener: ListenerRegistration? = null
  private var breedingListener: ListenerRegistration? = null
  private var expensesListener: ListenerRegistration? = null
  private var membersListener: ListenerRegistration? = null

  private val _farmProfile = MutableStateFlow(FarmProfile())
  val farmProfile: StateFlow<FarmProfile> = _farmProfile.asStateFlow()

  private val _syncStatusText = MutableStateFlow("🟢 Cloud Synced (क्लाऊड सिंक सुरू आहे)")
  val syncStatusText: StateFlow<String> = _syncStatusText.asStateFlow()

  // Realtime live cloud streams for instant UI observation
  private val _realtimeMilkEntries = MutableStateFlow<List<MilkEntry>>(emptyList())
  val realtimeMilkEntries: StateFlow<List<MilkEntry>> = _realtimeMilkEntries.asStateFlow()

  private val _realtimeCows = MutableStateFlow<List<Cow>>(emptyList())
  val realtimeCows: StateFlow<List<Cow>> = _realtimeCows.asStateFlow()

  private val _realtimeBreedingRecords = MutableStateFlow<List<BreedingRecord>>(emptyList())
  val realtimeBreedingRecords: StateFlow<List<BreedingRecord>> = _realtimeBreedingRecords.asStateFlow()

  private val _realtimeExpenses = MutableStateFlow<List<ExpenseEntry>>(emptyList())
  val realtimeExpenses: StateFlow<List<ExpenseEntry>> = _realtimeExpenses.asStateFlow()

  init {
    loadSavedSession()
    initFirebase()
  }

  private fun loadSavedSession() {
    val isSetup = prefs.getBoolean("key_setup_completed", false)
    if (isSetup) {
      val farmId = prefs.getString("key_farm_id", "") ?: ""
      val farmName = prefs.getString("key_farm_name", "SK Dairy") ?: "SK Dairy"
      val inviteCode = prefs.getString("key_invite_code", farmId) ?: farmId
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
        farmId = farmId.ifBlank { inviteCode },
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
        val settings = FirebaseFirestoreSettings.Builder()
          .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
          .build()
        db.firestoreSettings = settings
      } catch (e: Exception) {
        Log.d(TAG, "Firestore settings initialized: ${e.message}")
      }
      firestore = db

      val currentFarmId = _farmProfile.value.farmId
      if (_farmProfile.value.isSetupCompleted && currentFarmId.isNotBlank()) {
        startRealtimeListeners(currentFarmId)
      }
    } catch (e: Exception) {
      Log.w(TAG, "Firebase initialization warning: ${e.message}")
    }
  }

  fun createNewFarm(farmName: String, ownerName: String, ownerContact: String = "") {
    val cleanFarmName = farmName.trim().ifBlank { "SK Dairy Farm" }
    val cleanOwnerName = ownerName.trim().ifBlank { "गोठा मालक" }
    val randomNum = (1000..9999).random()
    val cleanCode = "SK-$randomNum"
    // Use cleanCode directly as farmId so both Owner and Friends joining SK-XXXX share the exact path!
    val newFarmId = cleanCode

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

    // Save Farm document to Firestore
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
    val trimmed = farmCode.trim().uppercase()
    val cleanCode = if (trimmed.startsWith("SK-")) trimmed else if (trimmed.matches(Regex("^[0-9]+$"))) "SK-$trimmed" else trimmed
    val cleanUserName = userName.trim().ifBlank { "गोठा सहाय्यक" }

    if (cleanCode.isBlank()) {
      onComplete(false, "कृपया वैध गोठा कोड प्रविष्ट करा (Please enter Farm Code)")
      return
    }

    val resolvedFarmId = cleanCode
    var resolvedFarmName = "SK Dairy Farm ($cleanCode)"
    var resolvedOwnerName = "गोठा मालक (Admin)"

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

    _farmProfile.value = profile
    saveSessionToPrefs(profile)

    // Save joining member & fetch farm info
    firestore?.let { db ->
      scope.launch(Dispatchers.IO) {
        try {
          // Fetch existing farm name if available
          val doc = db.collection("farms").document(resolvedFarmId).get().awaitIfPossible()
          if (doc != null && doc.exists()) {
            val name = doc.getString("farmName") ?: resolvedFarmName
            val owner = doc.getString("ownerName") ?: resolvedOwnerName
            withContext(Dispatchers.Main) {
              _farmProfile.value = _farmProfile.value.copy(
                farmName = name,
                ownerName = owner
              )
              saveSessionToPrefs(_farmProfile.value)
            }
          }

          db.collection("farms")
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
          Log.e(TAG, "Error joining farm in Firestore", e)
        }
      }
    }

    startRealtimeListeners(resolvedFarmId)
    onComplete(true, "यशस्वीरित्या गोठा कोडशी जोडले गेले! (Connected to Farm $cleanCode)")
  }

  fun switchFarmOrLogout() {
    detachAllListeners()

    prefs.edit().clear().apply()

    _realtimeMilkEntries.value = emptyList()
    _realtimeCows.value = emptyList()
    _realtimeBreedingRecords.value = emptyList()
    _realtimeExpenses.value = emptyList()

    scope.launch(Dispatchers.IO) {
      repository.clearAllLocalData()
    }

    _farmProfile.value = FarmProfile(isSetupCompleted = false)
    _syncStatusText.value = "Disconnected"
  }

  private fun detachAllListeners() {
    milkListener?.remove()
    milkListener = null
    cowsListener?.remove()
    cowsListener = null
    breedingListener?.remove()
    breedingListener = null
    expensesListener?.remove()
    expensesListener = null
    membersListener?.remove()
    membersListener = null
  }

  fun startRealtimeListeners(farmId: String) {
    if (farmId.isBlank()) return
    detachAllListeners()

    val db = firestore ?: return
    val cleanFarmId = farmId.trim().uppercase()

    Log.d(TAG, "Starting Cloud Snapshot Listeners for Farm ID: $cleanFarmId")

    try {
      // 1. Snapshot Listener on farms/{farmId}/milk_records
      milkListener = db.collection("farms")
        .document(cleanFarmId)
        .collection("milk_records")
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.e(TAG, "Milk listener error", error)
            return@addSnapshotListener
          }

          if (snapshot != null) {
            val list = snapshot.documents.mapNotNull { doc ->
              try {
                val idLong = doc.getLong("id") ?: (doc.id.removePrefix("milk_").toLongOrNull() ?: 0L)
                val date = doc.getLong("date") ?: System.currentTimeMillis()
                val cowId = doc.getLong("cowId")
                val cowName = doc.getString("cowName") ?: "गोठा एकूण दूध"
                val sessionStr = doc.getString("session") ?: MilkSession.MORNING.name
                val session = try { MilkSession.valueOf(sessionStr) } catch (e: Exception) { MilkSession.MORNING }
                val liters = doc.getDouble("liters") ?: 0.0
                val fat = doc.getDouble("fat") ?: 0.0
                val snf = doc.getDouble("snf") ?: 0.0
                val ratePerLiter = doc.getDouble("ratePerLiter") ?: 0.0
                val totalAmount = doc.getDouble("totalAmount") ?: (liters * ratePerLiter)
                val dairyCenterName = doc.getString("dairyCenterName") ?: ""
                val createdBy = doc.getString("createdBy") ?: "Akshay (Admin)"
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                if (liters > 0) {
                  MilkEntry(
                    id = idLong,
                    date = date,
                    cowId = cowId,
                    cowName = cowName,
                    session = session,
                    liters = liters,
                    fat = fat,
                    snf = snf,
                    ratePerLiter = ratePerLiter,
                    totalAmount = totalAmount,
                    dairyCenterName = dairyCenterName,
                    createdBy = createdBy,
                    createdAt = createdAt
                  )
                } else null
              } catch (e: Exception) {
                Log.e(TAG, "Error parsing milk doc: ${doc.id}", e)
                null
              }
            }.sortedWith(compareByDescending<MilkEntry> { it.date }.thenByDescending { it.createdAt })

            // Direct instant reactive update to StateFlow -> Triggers instant UI recomposition!
            _realtimeMilkEntries.value = list
            _farmProfile.value = _farmProfile.value.copy(
              lastSyncedTimestamp = System.currentTimeMillis(),
              isRealtimeConnected = true
            )
            _syncStatusText.value = "🟢 Cloud Synced (क्लाऊड सिंक सुरू आहे)"

            // Also keep local Room SQLite updated for seamless offline caching
            scope.launch(Dispatchers.IO) {
              list.forEach { entry ->
                try {
                  repository.insertMilkEntry(entry)
                } catch (e: Exception) {
                  Log.d(TAG, "Local db cache insert: ${e.message}")
                }
              }
            }
          }
        }

      // 2. Snapshot Listener on farms/{farmId}/cows
      cowsListener = db.collection("farms")
        .document(cleanFarmId)
        .collection("cows")
        .addSnapshotListener { snapshot, error ->
          if (error == null && snapshot != null) {
            val cowList = snapshot.documents.mapNotNull { doc ->
              try {
                val idLong = doc.getLong("id") ?: (doc.id.removePrefix("cow_").toLongOrNull() ?: 0L)
                val tag = doc.getString("tagNumber") ?: "COW-${doc.id}"
                val name = doc.getString("name") ?: "गाय"
                val breed = doc.getString("breed") ?: "HF"
                val statusStr = doc.getString("status") ?: CowStatus.MILKING.name
                val status = try { CowStatus.valueOf(statusStr) } catch (e: Exception) { CowStatus.MILKING }
                val avgMilk = doc.getDouble("dailyAvgMilk") ?: 0.0
                val notes = doc.getString("notes") ?: ""
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                Cow(
                  id = idLong,
                  tagNumber = tag,
                  name = name,
                  breed = breed,
                  status = status,
                  dailyAvgMilk = avgMilk,
                  notes = notes,
                  createdAt = createdAt
                )
              } catch (e: Exception) {
                null
              }
            }.sortedByDescending { it.createdAt }

            _realtimeCows.value = cowList

            scope.launch(Dispatchers.IO) {
              cowList.forEach { cow ->
                try { repository.insertCow(cow) } catch (e: Exception) {}
              }
            }
          }
        }

      // 3. Snapshot Listener on farms/{farmId}/members
      membersListener = db.collection("farms")
        .document(cleanFarmId)
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

      // 4. Automatic migration/upload of existing local offline records to Firestore
      scope.launch(Dispatchers.IO) {
        migrateLocalRecordsToCloud(cleanFarmId)
      }

    } catch (e: Exception) {
      Log.e(TAG, "Error starting Firestore listeners", e)
    }
  }

  private suspend fun migrateLocalRecordsToCloud(farmId: String) {
    val db = firestore ?: return
    try {
      // Migrate local milk entries
      val localEntries = repository.allMilkEntries.firstOrNull() ?: emptyList()
      localEntries.forEach { entry ->
        val docId = if (entry.id > 0) "milk_${entry.id}" else "milk_${entry.date}_${entry.session.name}"
        val entryData = hashMapOf(
          "id" to (if (entry.id > 0) entry.id else System.currentTimeMillis()),
          "date" to entry.date,
          "cowId" to entry.cowId,
          "cowName" to entry.cowName,
          "session" to entry.session.name,
          "liters" to entry.liters,
          "fat" to entry.fat,
          "snf" to entry.snf,
          "ratePerLiter" to entry.ratePerLiter,
          "totalAmount" to entry.totalAmount,
          "dairyCenterName" to entry.dairyCenterName,
          "createdBy" to entry.createdBy,
          "createdAt" to entry.createdAt,
          "updatedAt" to System.currentTimeMillis()
        )
        db.collection("farms")
          .document(farmId)
          .collection("milk_records")
          .document(docId)
          .set(entryData, SetOptions.merge())
      }

      // Migrate local cows
      val localCows = repository.allCows.firstOrNull() ?: emptyList()
      localCows.forEach { cow ->
        val docId = if (cow.id > 0) "cow_${cow.id}" else "cow_${cow.tagNumber}"
        val cowData = hashMapOf(
          "id" to (if (cow.id > 0) cow.id else System.currentTimeMillis()),
          "tagNumber" to cow.tagNumber,
          "name" to cow.name,
          "breed" to cow.breed,
          "status" to cow.status.name,
          "dailyAvgMilk" to cow.dailyAvgMilk,
          "notes" to cow.notes,
          "createdAt" to cow.createdAt
        )
        db.collection("farms")
          .document(farmId)
          .collection("cows")
          .document(docId)
          .set(cowData, SetOptions.merge())
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error in background cloud migration: ${e.message}")
    }
  }

  fun syncMilkEntryToCloud(entry: MilkEntry) {
    val db = firestore ?: return
    val farmId = _farmProfile.value.farmId.ifBlank { _farmProfile.value.inviteCode }
    if (farmId.isBlank()) return
    val currentUserName = _farmProfile.value.currentUserName

    scope.launch(Dispatchers.IO) {
      try {
        val entryData = hashMapOf(
          "id" to entry.id,
          "date" to entry.date,
          "cowId" to entry.cowId,
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
          .collection("milk_records")
          .document(docId)
          .set(entryData, SetOptions.merge())

        _farmProfile.value = _farmProfile.value.copy(
          lastSyncedTimestamp = System.currentTimeMillis(),
          isRealtimeConnected = true
        )
      } catch (e: Exception) {
        Log.e(TAG, "Failed to upload milk record to cloud", e)
      }
    }
  }

  fun deleteMilkEntryFromCloud(entry: MilkEntry) {
    val db = firestore ?: return
    val farmId = _farmProfile.value.farmId.ifBlank { _farmProfile.value.inviteCode }
    if (farmId.isBlank()) return

    scope.launch(Dispatchers.IO) {
      try {
        val docId = if (entry.id > 0) "milk_${entry.id}" else ""
        if (docId.isNotBlank()) {
          db.collection("farms")
            .document(farmId)
            .collection("milk_records")
            .document(docId)
            .delete()
        }

        // Also query by date & liters if id wasn't matching
        db.collection("farms")
          .document(farmId)
          .collection("milk_records")
          .whereEqualTo("date", entry.date)
          .whereEqualTo("session", entry.session.name)
          .whereEqualTo("liters", entry.liters)
          .get()
          .addOnSuccessListener { querySnapshot ->
            querySnapshot.documents.forEach { doc ->
              doc.reference.delete()
            }
          }
      } catch (e: Exception) {
        Log.e(TAG, "Error deleting milk record from cloud", e)
      }
    }
  }

  fun syncCowToCloud(cow: Cow) {
    val db = firestore ?: return
    val farmId = _farmProfile.value.farmId.ifBlank { _farmProfile.value.inviteCode }
    if (farmId.isBlank()) return

    scope.launch(Dispatchers.IO) {
      try {
        val cowData = hashMapOf(
          "id" to cow.id,
          "tagNumber" to cow.tagNumber,
          "name" to cow.name,
          "breed" to cow.breed,
          "status" to cow.status.name,
          "dailyAvgMilk" to cow.dailyAvgMilk,
          "notes" to cow.notes,
          "createdAt" to cow.createdAt
        )
        val docId = if (cow.id > 0) "cow_${cow.id}" else "cow_${System.currentTimeMillis()}"
        db.collection("farms")
          .document(farmId)
          .collection("cows")
          .document(docId)
          .set(cowData, SetOptions.merge())
      } catch (e: Exception) {
        Log.e(TAG, "Error syncing cow to cloud", e)
      }
    }
  }

  fun deleteCowFromCloud(cow: Cow) {
    val db = firestore ?: return
    val farmId = _farmProfile.value.farmId.ifBlank { _farmProfile.value.inviteCode }
    if (farmId.isBlank()) return

    scope.launch(Dispatchers.IO) {
      try {
        val docId = if (cow.id > 0) "cow_${cow.id}" else "cow_${cow.tagNumber}"
        db.collection("farms")
          .document(farmId)
          .collection("cows")
          .document(docId)
          .delete()
      } catch (e: Exception) {
        Log.e(TAG, "Error deleting cow from cloud", e)
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
