package it.attendance100.mybicocca.data.model.messaging

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val id: Int,
    val name: String? = null,
    val type: Int = 1,
    val memberCount: Int = 0,
    val unreadCount: Int = 0,
    val isFavourite: Boolean = false,
    val isMuted: Boolean = false,
    val lastMessageText: String? = null,
    val lastMessageDate: Long? = null,
)
