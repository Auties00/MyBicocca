package it.attendance100.mybicocca.domain.model

import androidx.room.*
import java.time.*

@Entity(tableName = "messages")
data class Message(
	@PrimaryKey val id: Int,
	@ColumnInfo(name = "conversation_id") val conversationId: Int,
	@ColumnInfo(name = "sender_id") val senderId: Int,
	@ColumnInfo(name = "sender_name") val senderName: String?,
	@ColumnInfo(name = "text") val text: String,
	@ColumnInfo(name = "time_created") val timeCreated: Instant,
	@ColumnInfo(name = "is_read") val isRead: Boolean,
)
