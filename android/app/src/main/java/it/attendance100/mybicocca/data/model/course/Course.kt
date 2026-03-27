package it.attendance100.mybicocca.data.model.course

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey val id: Int,
    val shortName: String,
    val fullName: String,
    val progress: Float? = null,
    val isFavourite: Boolean = false,
    val imageUrl: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
)
