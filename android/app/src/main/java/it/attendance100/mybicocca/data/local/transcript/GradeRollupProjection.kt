package it.attendance100.mybicocca.data.local.transcript

import androidx.room.ColumnInfo

// Aggregate query result. SQLite's SUM() returns NULL for empty sets, so all
// columns are nullable; the mapper folds nulls into the empty-rollup case.
data class GradeRollupProjection(
    @ColumnInfo(name = "graded_exam_count") val gradedExamCount: Int,
    @ColumnInfo(name = "grade_sum") val gradeSum: Long?,
    @ColumnInfo(name = "weighted_grade_sum") val weightedGradeSum: Double?,
    @ColumnInfo(name = "graded_credits_sum") val gradedCreditsSum: Float?,
)
