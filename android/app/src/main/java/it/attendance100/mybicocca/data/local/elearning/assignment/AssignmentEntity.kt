package it.attendance100.mybicocca.data.local.elearning.assignment

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

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
    // JSON-encoded SubmissionStatusJson (sealed)
    @ColumnInfo(name = "submission_status_json") val submissionStatusJson: String,
)
