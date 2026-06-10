package it.attendance100.mybicocca.data.local.elearning.assignment

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * Cached assignment of an e-learning course, account-scoped.
 *
 * Keyed by (account_id, assignment_id) — the Moodle mod_assign instance id — with an index on
 * (account_id, course_id) backing the per-course queries. Timestamps are epoch milliseconds.
 * Structured values are packed into JSON columns: [introFilesJson] holds a list of attachment
 * references, [submissionStatusJson] the sealed submission-status envelope, and
 * [submissionConfigJson] the static submission settings (enabled plugins, file limits,
 * statement flag) parsed from the assignment's plugin configs — null until first synced.
 */
@Entity(
    tableName = "elearning_assignments",
    primaryKeys = ["account_id", "assignment_id"],
    indices = [Index("account_id", "course_id")],
)
data class AssignmentEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "assignment_id") val assignmentId: Int,
    @ColumnInfo(name = "course_id") val courseId: Int,
    @ColumnInfo(name = "cm_id") val cmId: Int?,
    val name: String,
    val intro: String?,
    @ColumnInfo(name = "intro_files_json") val introFilesJson: String?,
    @ColumnInfo(name = "due_date_ms") val dueDateMs: Long?,
    @ColumnInfo(name = "allow_submissions_from_ms") val allowSubmissionsFromMs: Long?,
    @ColumnInfo(name = "cutoff_date_ms") val cutoffDateMs: Long?,
    @ColumnInfo(name = "grading_due_date_ms") val gradingDueDateMs: Long?,
    @ColumnInfo(name = "max_attempts") val maxAttempts: Int?,
    @ColumnInfo(name = "allowed_extensions_csv") val allowedExtensionsCsv: String?,
    @ColumnInfo(name = "allow_drafts") val allowDrafts: Boolean,
    @ColumnInfo(name = "submission_status_json") val submissionStatusJson: String,
    @ColumnInfo(name = "submission_config_json") val submissionConfigJson: String? = null,
)
