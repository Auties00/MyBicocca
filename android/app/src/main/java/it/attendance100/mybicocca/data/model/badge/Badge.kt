package it.attendance100.mybicocca.data.model.badge

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "badges")
data class Badge(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val issuedDate: Long? = null,
    val expireDate: Long? = null,
    val courseId: Int? = null,
)
