package it.attendance100.mybicocca.data.model.course

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "course_grades")
data class CourseGrade(
    @PrimaryKey val courseId: Int,
    val courseName: String,
    val grade: String? = null,
    val rawGrade: String? = null,
    val rank: Int? = null,
)
