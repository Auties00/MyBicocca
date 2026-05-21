package it.attendance100.mybicocca.data.local.elearning.quiz

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "elearning_quiz_best_grade",
    primaryKeys = ["account_id", "quiz_id"],
)
data class QuizBestGradeEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "quiz_id") val quizId: Int,
    val grade: Double?,
    @ColumnInfo(name = "max_grade") val maxGrade: Double?,
)
