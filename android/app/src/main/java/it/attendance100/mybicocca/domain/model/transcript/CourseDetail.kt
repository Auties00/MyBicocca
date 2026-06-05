package it.attendance100.mybicocca.domain.model.transcript

import java.time.LocalDate

// Everything we can surface for a single libretto activity beyond the summary row:
// the attempt history ("prove") and the propedeuticità outcome. Both sub-resources
// are best-effort — a course may have neither, and /prop is never queried for an
// already-passed activity (the endpoint 422s on those).
data class CourseDetail(
    val attempts: List<CourseAttempt>,
    val prerequisites: PrerequisiteStatus,
    val bookableCallsCount: Int,
)

// One registration/attempt for the activity. An attempt carries up to three outcome
// facets (written, partial, final); the final one is what lands in the libretto.
data class CourseAttempt(
    val id: Long,
    val callDate: LocalDate?,
    val sessionDescription: String?,
    val statusDescription: String?,
    val finalOutcome: AttemptOutcome?,
    val writtenOutcome: AttemptOutcome?,
    val partialOutcome: AttemptOutcome?,
) {
    val bestOutcome: AttemptOutcome?
        get() = finalOutcome ?: writtenOutcome ?: partialOutcome
}

data class AttemptOutcome(
    val passed: Boolean,
    val grade: Float?,
    val cumLaude: Boolean,
    val judgment: String?,
    val date: LocalDate?,
)
