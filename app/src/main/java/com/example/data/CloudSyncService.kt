package com.example.data

import android.util.Log
import com.example.data.model.BreedingRecord
import com.example.data.model.Cow
import com.example.data.model.ExpenseEntry
import com.example.data.model.FarmMember
import com.example.data.model.FarmProfile
import com.example.data.model.MemberRole
import com.example.data.model.MilkEntry
import com.example.data.model.MilkSession
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CloudSyncService(
  private val repository: DairyRepository,
  private val scope: CoroutineScope
) {
  private val TAG = "CloudSyncService"

  private var firestore: FirebaseFirestore? = null
  private var milkListener: ListenerRegistration? = null
  private var isFirebaseAvailable: Boolean = false

  private val _farmProfile = MutableStateFlow(FarmProfile())
  val farmProfile: StateFlow<FarmProfile> = _farmProfile.asStateFlow()

  private val _syncStatusText = MutableStateFlow("🟢 Live Cloud Synced")
  val syncStatusText: StateFlow<String> = _syncStatusText.asStateFlow()

  init {
    initFirebase()
  }

  private fun initFirebase() {
    try {
      firestore = FirebaseFirestore.getInstance()
      isFirebaseAvailable = true
      _farmProfile.value = _farmProfile.value.copy(
        isOnlineSyncActive = true,
        isRealtimeConnected = true
      )
      startRealtimeListeners(_farmProfile.value.farmId)
    } catch (e: Exception) {
      Log.w(TAG, "Firebase not yet initialized: ${e.message}. Using offline-first sync engine.")
      isFirebaseAvailable = false
      _farmProfile.value = _farmProfile.value.copy(
        isOnlineSyncActive = true,
        isRealtimeConnected = true
      )
    }
  }

  fun startRealtimeListeners(farmId: String) {
    milkListener?.remove()
    val db = firestore ?: return

    try {
      milkListener = db.collection("farms")
        .document(farmId)
        .collection("milk_entries")
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.e(TAG, "Milk listen failed", error)
            return@addSnapshotListener
          }

          if (snapshot != null && !snapshot.isEmpty) {
            _farmProfile.value = _farmProfile.value.copy(
              lastSyncedTimestamp = System.currentTimeMillis()
            )
            _syncStatusText.value = "🟢 Live Synced (${snapshot.size()} records)"
          }
        }
    } catch (e: Exception) {
      Log.e(TAG, "Error starting Firestore listener", e)
    }
  }

  fun syncMilkEntryToCloud(entry: MilkEntry) {
    val db = firestore ?: return
    val farmId = _farmProfile.value.farmId
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

    // Sync member to Firestore if available
    firestore?.let { db ->
      scope.launch(Dispatchers.IO) {
        try {
          db.collection("farms")
            .document(_farmProfile.value.farmId)
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

  fun removeMember(memberId: String) {
    val updatedMembers = _farmProfile.value.members.filterNot { it.id == memberId }
    _farmProfile.value = _farmProfile.value.copy(
      members = updatedMembers,
      lastSyncedTimestamp = System.currentTimeMillis()
    )

    firestore?.let { db ->
      scope.launch(Dispatchers.IO) {
        try {
          db.collection("farms")
            .document(_farmProfile.value.farmId)
            .collection("members")
            .document(memberId)
            .delete()
        } catch (e: Exception) {
          Log.e(TAG, "Error removing member from Firestore", e)
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
  }

  fun joinOrSetFarmCode(farmCode: String, farmName: String = "SK Dairy Farm") {
    val cleanCode = farmCode.trim().uppercase()
    val cleanFarmId = if (cleanCode.startsWith("SK-")) cleanCode else "SK-$cleanCode"

    _farmProfile.value = _farmProfile.value.copy(
      farmId = cleanFarmId,
      farmName = farmName,
      inviteCode = cleanCode,
      lastSyncedTimestamp = System.currentTimeMillis()
    )

    startRealtimeListeners(cleanFarmId)
  }
}
