package it.attendance100.mybicocca.data.model.quiz

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quizzes")
data class Quiz(
    @PrimaryKey val id: Int,
    val courseId: Int,
    val name: String,
    val description: String? = null,
    val timeLimit: Int? = null,
    val maxAttempts: Int? = null,
    val gradeMethod: Int? = null,
    val openDate: Long? = null,
    val closeDate: Long? = null,
)
