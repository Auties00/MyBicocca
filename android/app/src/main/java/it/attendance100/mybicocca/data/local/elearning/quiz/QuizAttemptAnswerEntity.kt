package it.attendance100.mybicocca.data.local.elearning.quiz

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "elearning_quiz_attempt_answers",
    primaryKeys = ["account_id", "attempt_id", "slot"],
)
data class QuizAttemptAnswerEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "attempt_id") val attemptId: Int,
    val slot: Int,
    // JSON-encoded Map<String, String>
    @ColumnInfo(name = "fields_json") val fieldsJson: String,
)
