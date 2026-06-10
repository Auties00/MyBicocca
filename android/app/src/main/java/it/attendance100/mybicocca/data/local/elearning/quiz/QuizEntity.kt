package it.attendance100.mybicocca.data.local.elearning.quiz

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * Cached quiz of an e-learning course, account-scoped.
 *
 * Keyed by (account_id, quiz_id) — the Moodle mod_quiz instance id — with an index on
 * (account_id, course_id) backing the per-course queries. Timestamps are epoch milliseconds;
 * the question behaviour and review bitmasks are stored raw as the platform reports them.
 */
@Entity(
    tableName = "elearning_quizzes",
    primaryKeys = ["account_id", "quiz_id"],
    indices = [Index("account_id", "course_id")],
)
data class QuizEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "quiz_id") val quizId: Int,
    @ColumnInfo(name = "course_id") val courseId: Int,
    @ColumnInfo(name = "cm_id") val cmId: Int?,
    val name: String,
    val intro: String?,
    @ColumnInfo(name = "time_open_ms") val timeOpenMs: Long?,
    @ColumnInfo(name = "time_close_ms") val timeCloseMs: Long?,
    @ColumnInfo(name = "time_limit_seconds") val timeLimitSeconds: Long?,
    @ColumnInfo(name = "grace_period_seconds") val gracePeriodSeconds: Long?,
    @ColumnInfo(name = "max_attempts") val maxAttempts: Int?,
    @ColumnInfo(name = "pass_grade") val passGrade: Double?,
    @ColumnInfo(name = "sum_grades") val sumGrades: Double?,
    @ColumnInfo(name = "max_grade") val maxGrade: Double?,
    @ColumnInfo(name = "preferred_behaviour") val preferredBehaviour: String?,
    @ColumnInfo(name = "review_before_bitmask") val reviewBeforeBitmask: Int?,
    @ColumnInfo(name = "review_after_bitmask") val reviewAfterBitmask: Int?,
)
