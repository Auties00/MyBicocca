package it.attendance100.mybicocca.domain.model.elearning.course

import java.time.Instant

data class CompletionState(
    val cmId: Int,
    val isCompleted: Boolean,
    val completedAt: Instant?,
    val isManual: Boolean,
    val isAutomatic: Boolean,
    val isTracked: Boolean,
)
