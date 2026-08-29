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
  val farmId: String = "SK-DAIRY-FARM-01",
  val farmName: String = "SK Dairy Farm",
  val inviteCode: String = "SK-7890",
  val ownerName: String = "Akshay Karande",
  val ownerContact: String = "akshaykarande575@gmail.com",
  val currentUserRole: MemberRole = MemberRole.ADMIN,
  val currentUserName: String = "Akshay Karande (Owner)",
  val currentUserContact: String = "akshaykarande575@gmail.com",
  val isOnlineSyncActive: Boolean = true,
  val isRealtimeConnected: Boolean = true,
  val lastSyncedTimestamp: Long = System.currentTimeMillis(),
  val members: List<FarmMember> = listOf(
    FarmMember(
      id = "m1",
      name = "अक्षय करांडे (Akshay)",
      contact = "akshaykarande575@gmail.com",
      role = MemberRole.ADMIN,
      isCurrentUser = true
    ),
    FarmMember(
      id = "m2",
      name = "बाबा / वडील (Father)",
      contact = "+91 98223 XXXXX",
      role = MemberRole.EDITOR,
      isCurrentUser = false
    ),
    FarmMember(
      id = "m3",
      name = "गोठा सहाय्यक (Farm Staff)",
      contact = "+91 97654 XXXXX",
      role = MemberRole.EDITOR,
      isCurrentUser = false
    ),
    FarmMember(
      id = "m4",
      name = "कुटुंब सदस्य (Family Member)",
      contact = "family@skdairy.com",
      role = MemberRole.VIEWER,
      isCurrentUser = false
    )
  )
)
