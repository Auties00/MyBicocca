package it.attendance100.mybicocca.data.local.elearning.quiz

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * Cached attempt of the student at a quiz, account-scoped.
 *
 * Keyed by (account_id, attempt_id) with an index on (account_id, quiz_id) backing the
 * per-quiz queries. The state is stored as the raw wire string, the layout as the raw
 * slot-layout string with ",0" page-break markers, and timestamps as epoch milliseconds.
 */
@Entity(
    tableName = "elearning_quiz_attempts",
    primaryKeys = ["account_id", "attempt_id"],
    indices = [Index("account_id", "quiz_id")],
)
data class QuizAttemptEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "attempt_id") val attemptId: Int,
    @ColumnInfo(name = "quiz_id") val quizId: Int,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "attempt_number") val attemptNumber: Int,
    @ColumnInfo(name = "state_raw") val stateRaw: String,
    @ColumnInfo(name = "sum_grades") val sumGrades: Double?,
    @ColumnInfo(name = "time_start_ms") val timeStartMs: Long?,
    @ColumnInfo(name = "time_finish_ms") val timeFinishMs: Long?,
    @ColumnInfo(name = "time_modified_ms") val timeModifiedMs: Long?,
    val layout: String?,
    @ColumnInfo(name = "preview_mode") val previewMode: Boolean,
)
