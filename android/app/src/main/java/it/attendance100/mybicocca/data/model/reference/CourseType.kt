package it.attendance100.mybicocca.data.model.reference

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "course_types")
data class CourseType(
    @PrimaryKey val code: String,
    val description: String,
)
