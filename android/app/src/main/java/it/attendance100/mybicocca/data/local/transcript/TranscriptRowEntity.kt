package it.attendance100.mybicocca.data.local.transcript

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * Room cache of one libretto row, the local source of truth behind the profile screen's
 * exam list. Keyed by (`id`, `career_id`): `id` is the Esse3 `adsceId` of the row, which
 * is unique per career but not across careers, so the career id completes the key.
 * Rows are career-scoped and replaced wholesale on each transcript sync.
 *
 * @property id Esse3 `adsceId` of the libretto row.
 * @property careerId Owning career; rows for an account's careers are purged together
 *   on sign-out.
 * @property state Stored as the `TranscriptRowState` enum name.
 * @property examDate ISO-8601 (yyyy-MM-dd) so lexicographic ORDER BY is chronological.
 * @property inStudyPlan False for supernumerary (sovrannumerarie) activities not counted
 *   in the study plan.
 * @property bookableCallsCount Bookable calls at the last sync; defaults to 0 for rows
 *   written before the column existed.
 */
@Entity(
    tableName = "transcript_rows",
    primaryKeys = ["id", "career_id"],
    indices = [
        Index("career_id", "state"),
        Index("career_id", "exam_date"),
    ],
)
data class TranscriptRowEntity(
    val id: Long,
    @ColumnInfo(name = "career_id") val careerId: Long,
    @ColumnInfo(name = "activity_code") val activityCode: String?,
    @ColumnInfo(name = "activity_name") val activityName: String,
    @ColumnInfo(name = "course_year") val courseYear: Int,
    val credits: Float,
    val state: String,
    val grade: Int?,
    @ColumnInfo(name = "cum_laude") val cumLaude: Boolean,
    @ColumnInfo(name = "exam_date") val examDate: String?,
    @ColumnInfo(name = "academic_year") val academicYear: Int?,
    @ColumnInfo(name = "in_study_plan") val inStudyPlan: Boolean,
    @ColumnInfo(name = "exam_type") val examType: String? = null,
    @ColumnInfo(name = "bookable_calls_count", defaultValue = "0") val bookableCallsCount: Int = 0,
)
