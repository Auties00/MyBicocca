package it.attendance100.mybicocca.data.local.transcript

import androidx.room.ColumnInfo

/**
 * Aggregate query result over a career's passed, numerically graded transcript rows.
 * SQLite's SUM() returns NULL for empty sets, so the sum columns are nullable; the
 * mapper folds nulls into the empty-rollup case.
 *
 * @property gradedExamCount Count of passed rows carrying a numeric grade.
 * @property gradeSum Plain sum of those grades; null when no row matches.
 * @property weightedGradeSum Sum of grade × credits; null when no row matches.
 * @property gradedCreditsSum Total credits of the graded rows; null when no row matches.
 */
data class GradeRollupProjection(
    @ColumnInfo(name = "graded_exam_count") val gradedExamCount: Int,
    @ColumnInfo(name = "grade_sum") val gradeSum: Long?,
    @ColumnInfo(name = "weighted_grade_sum") val weightedGradeSum: Double?,
    @ColumnInfo(name = "graded_credits_sum") val gradedCreditsSum: Float?,
)
