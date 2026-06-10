package it.attendance100.mybicocca.domain.model.transcript

import it.attendance100.mybicocca.domain.model.career.CareerId

/**
 * Career-level aggregates over the libretto, sourced from Esse3's record-book stats and
 * averages endpoints, cached in Room, and rendered by the profile screen's progress and
 * averages cards.
 *
 * @property careerId Career the stats belong to.
 * @property passedCredits CFU already earned.
 * @property totalCreditsRequired CFU required by the course of study.
 * @property arithmeticAverage Exam-scale (base 30) arithmetic average; null until at
 *   least one graded exam exists.
 * @property weightedAverage Exam-scale (base 30) credit-weighted average; null until at
 *   least one graded exam exists.
 * @property passedExamCount Activities already passed.
 * @property plannedExamCount Activities in the study plan, i.e. the "exams to take"
 *   denominator.
 * @property maxGrade Top of the grading scale, normally 30.
 * @property cumLaudeAvailable True when the grading scale admits the lode.
 */
data class TranscriptStats(
    val careerId: CareerId,
    val passedCredits: Float,
    val totalCreditsRequired: Float,
    val arithmeticAverage: Float?,
    val weightedAverage: Float?,
    val passedExamCount: Int,
    val plannedExamCount: Int,
    val maxGrade: Int,
    val cumLaudeAvailable: Boolean,
)
