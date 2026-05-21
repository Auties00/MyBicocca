package it.attendance100.mybicocca.data.local.elearning.message

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "elearning_conversation_members",
    primaryKeys = ["account_id", "conversation_id", "user_id"],
)
data class ConversationMemberEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "conversation_id") val conversationId: Int,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "profile_image_url") val profileImageUrl: String?,
    @ColumnInfo(name = "is_online") val isOnline: Boolean,
    @ColumnInfo(name = "is_blocked") val isBlocked: Boolean,
)
