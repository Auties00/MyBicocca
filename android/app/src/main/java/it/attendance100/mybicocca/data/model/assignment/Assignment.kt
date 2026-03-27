package it.attendance100.mybicocca.data.model.assignment

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assignments")
data class Assignment(
    @PrimaryKey val id: Int,
    val courseId: Int,
    val name: String,
    val description: String? = null,
    val dueDate: Long? = null,
    val cutOffDate: Long? = null,
    val maxGrade: Float? = null,
)
