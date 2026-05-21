package it.attendance100.mybicocca.data.local.transcript

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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
