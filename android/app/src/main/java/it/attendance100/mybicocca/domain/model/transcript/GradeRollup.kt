package it.attendance100.mybicocca.domain.model.transcript

// Pre-aggregated running sums over passed/graded transcript rows. Stored on the
// repository side so the hypothetical-grade calculator can recompute averages in
// O(1) per keystroke instead of summing the full transcript on every input change.
data class GradeRollup(
    val gradedExamCount: Int,
    val gradeSum: Long,
    val weightedGradeSum: Double,
    val gradedCreditsSum: Float,
)
