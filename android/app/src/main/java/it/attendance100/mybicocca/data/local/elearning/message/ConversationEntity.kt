package it.attendance100.mybicocca.data.local.elearning.message

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "elearning_conversations",
    primaryKeys = ["account_id", "conversation_id"],
)
data class ConversationEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "conversation_id") val conversationId: Int,
    @ColumnInfo(name = "type_code") val typeCode: Int,
    val name: String?,
    @ColumnInfo(name = "sub_name") val subName: String?,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "unread_count") val unreadCount: Int,
    @ColumnInfo(name = "is_muted") val isMuted: Boolean,
    @ColumnInfo(name = "is_favourite") val isFavourite: Boolean,
    @ColumnInfo(name = "last_message_id") val lastMessageId: Int?,
    @ColumnInfo(name = "last_message_text") val lastMessageText: String?,
    @ColumnInfo(name = "last_message_sender_id") val lastMessageSenderId: Int?,
    @ColumnInfo(name = "last_message_at_ms") val lastMessageAtMs: Long?,
    @ColumnInfo(name = "sort_at_ms") val sortAtMs: Long,
)
