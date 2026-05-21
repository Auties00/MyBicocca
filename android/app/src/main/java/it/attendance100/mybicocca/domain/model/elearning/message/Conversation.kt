package it.attendance100.mybicocca.domain.model.elearning.message

data class Conversation(
    val id: ConversationId,
    val type: ConversationType,
    val name: String?,
    val subName: String?,
    val imageUrl: String?,
    val unreadCount: Int,
    val isMuted: Boolean,
    val isFavourite: Boolean,
    val members: List<ConversationMember>,
    val lastMessage: Message?,
)
