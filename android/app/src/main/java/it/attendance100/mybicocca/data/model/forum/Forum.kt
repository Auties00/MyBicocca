package it.attendance100.mybicocca.data.model.forum

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forums")
data class Forum(
    @PrimaryKey val id: Int,
    val courseId: Int,
    val name: String,
    val description: String? = null,
    val discussionCount: Int = 0,
    val type: String? = null,
)
