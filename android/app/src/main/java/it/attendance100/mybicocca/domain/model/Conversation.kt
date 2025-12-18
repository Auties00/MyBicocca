package it.attendance100.mybicocca.domain.model

import androidx.room.*
import java.time.*

@Entity(tableName = "conversations")
data class Conversation(
	@PrimaryKey val id: Int,
	@ColumnInfo(name = "name") val name: String?, // Often null for 1-on-1, use otherUserName
	@ColumnInfo(name = "other_user_id") val otherUserId: Int?,
	@ColumnInfo(name = "other_user_name") val otherUserName: String?,
	@ColumnInfo(name = "other_user_avatar_url") val otherUserAvatarUrl: String?,
	@ColumnInfo(name = "last_message") val lastMessage: String?,
	@ColumnInfo(name = "last_message_date") val lastMessageDate: Instant?,
	@ColumnInfo(name = "unread_count") val unreadCount: Int,
	@ColumnInfo(name = "is_favorite") val isFavorite: Boolean,
)
