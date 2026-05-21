package it.attendance100.mybicocca.domain.model.transcript

import it.attendance100.mybicocca.domain.model.career.CareerId

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
