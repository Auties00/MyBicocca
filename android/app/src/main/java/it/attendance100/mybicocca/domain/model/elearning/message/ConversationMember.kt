package it.attendance100.mybicocca.domain.model.elearning.message

data class ConversationMember(
    val userId: Int,
    val fullName: String,
    val profileImageUrl: String?,
    val isOnline: Boolean,
    val isBlocked: Boolean,
)
