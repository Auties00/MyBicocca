package it.attendance100.mybicocca.data.local.elearning.message

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "elearning_messages",
    primaryKeys = ["account_id", "message_id"],
    indices = [Index("account_id", "conversation_id", "sent_at_ms")],
)
data class MessageEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "message_id") val messageId: Int,
    @ColumnInfo(name = "conversation_id") val conversationId: Int,
    @ColumnInfo(name = "sender_user_id") val senderUserId: Int,
    val text: String,
    @ColumnInfo(name = "sent_at_ms") val sentAtMs: Long?,
    @ColumnInfo(name = "is_read") val isRead: Boolean,
)
