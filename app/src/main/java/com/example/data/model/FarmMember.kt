package com.example.data.model

enum class MemberRole {
  ADMIN,   // मालक / ॲडमिन - Full Access (Add/Edit/Delete, Manage Team & Farm Code)
  EDITOR,  // सहाय्यक / कामगार - Editor Access (Add/Edit Milk, Cows, Expenses, Breeding)
  VIEWER   // कुटुंब / वाचक - View-Only Access (View Live Milk, Alerts, Reports)
}

data class FarmMember(
  val id: String = "",
  val name: String = "",
  val contact: String = "", // Mobile number or Email
  val role: MemberRole = MemberRole.EDITOR,
  val joinedAt: Long = System.currentTimeMillis(),
  val isCurrentUser: Boolean = false
)

data class FarmProfile(
  val isSetupCompleted: Boolean = false,
  val farmId: String = "",
  val farmName: String = "",
  val inviteCode: String = "",
  val ownerName: String = "",
  val ownerContact: String = "",
  val currentUserRole: MemberRole = MemberRole.ADMIN,
  val currentUserName: String = "",
  val currentUserContact: String = "",
  val isOnlineSyncActive: Boolean = true,
  val isRealtimeConnected: Boolean = false,
  val lastSyncedTimestamp: Long = 0L,
  val members: List<FarmMember> = emptyList()
)
