package it.attendance100.mybicocca.domain.model.elearning.message

import java.time.Instant

data class Message(
    val id: MessageId,
    val conversationId: ConversationId,
    val senderUserId: Int,
    val text: String,
    val sentAt: Instant?,
    val isRead: Boolean,
)
