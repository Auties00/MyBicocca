package it.attendance100.mybicocca.data.local.transcript

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room cache of the career-level libretto aggregates (credits, averages, exam counts).
 * One row per career, keyed by `career_id`, upserted on each transcript sync alongside
 * the rows.
 */
@Entity(tableName = "transcript_stats")
data class TranscriptStatsEntity(
    @PrimaryKey @ColumnInfo(name = "career_id") val careerId: Long,
    @ColumnInfo(name = "passed_credits") val passedCredits: Float,
    @ColumnInfo(name = "total_credits_required") val totalCreditsRequired: Float,
    @ColumnInfo(name = "arithmetic_average") val arithmeticAverage: Float?,
    @ColumnInfo(name = "weighted_average") val weightedAverage: Float?,
    @ColumnInfo(name = "passed_exam_count") val passedExamCount: Int,
    @ColumnInfo(name = "planned_exam_count") val plannedExamCount: Int,
    @ColumnInfo(name = "max_grade") val maxGrade: Int,
    @ColumnInfo(name = "cum_laude_available") val cumLaudeAvailable: Boolean,
)
