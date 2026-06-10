package it.attendance100.mybicocca.data.local.elearning.quiz

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Locally persisted draft answers for one question slot of a quiz attempt, letting an
 * interrupted attempt resume with its answers intact. Keyed by (account_id, attempt_id, slot);
 * [fieldsJson] is a JSON-encoded map of raw form-field name to submitted value.
 */
@Entity(
    tableName = "elearning_quiz_attempt_answers",
    primaryKeys = ["account_id", "attempt_id", "slot"],
)
data class QuizAttemptAnswerEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "attempt_id") val attemptId: Int,
    val slot: Int,
    @ColumnInfo(name = "fields_json") val fieldsJson: String,
)
