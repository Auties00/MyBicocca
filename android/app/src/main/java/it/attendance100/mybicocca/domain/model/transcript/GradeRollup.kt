package it.attendance100.mybicocca.domain.model.transcript

/**
 * Pre-aggregated running sums over passed, numerically graded transcript rows, computed
 * by the Room layer. Lets the profile screen's hypothetical-grade calculator recompute
 * averages in O(1) per keystroke, with no full-transcript scan on every input change.
 *
 * @property gradedExamCount How many passed exams carry a numeric grade.
 * @property gradeSum Plain sum of those grades, for the arithmetic average.
 * @property weightedGradeSum Sum of grade × credits, for the weighted average.
 * @property gradedCreditsSum Total credits of the graded exams, the weighted-average
 *   denominator.
 */
data class GradeRollup(
    val gradedExamCount: Int,
    val gradeSum: Long,
    val weightedGradeSum: Double,
    val gradedCreditsSum: Float,
)
