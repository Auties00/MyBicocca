package it.attendance100.mybicocca.data.local.transcript

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

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
    // ISO-8601 (yyyy-MM-dd) so lexicographic ORDER BY = chronological.
    @ColumnInfo(name = "exam_date") val examDate: String?,
    @ColumnInfo(name = "academic_year") val academicYear: Int?,
    // false for supernumerary (sovrannumerarie) activities not counted in the study plan.
    @ColumnInfo(name = "in_study_plan") val inStudyPlan: Boolean,
)
