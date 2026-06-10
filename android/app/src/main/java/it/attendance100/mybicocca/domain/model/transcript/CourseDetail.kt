package it.attendance100.mybicocca.domain.model.transcript

import java.time.LocalDate

/**
 * Everything surfaced for a single libretto activity beyond the summary row: the Esse3
 * attempt history ("prove") and the propedeuticità outcome. Fetched live when the
 * student opens a course from the profile screen's course-detail sub-screen; both
 * sub-resources are best-effort — a course may have neither, and the prerequisite
 * endpoint is never queried for an already-passed activity (it responds 422 on those).
 *
 * @property attempts Registrations/attempts for the activity, most recent first.
 * @property prerequisites Outcome of the propedeuticità check; [PrerequisiteStatus.Unknown]
 *   when the check was skipped or failed.
 * @property bookableCallsCount How many exam calls can be booked for the activity at the
 *   time of the fetch.
 */
data class CourseDetail(
    val attempts: List<CourseAttempt>,
    val prerequisites: PrerequisiteStatus,
    val bookableCallsCount: Int,
)

/**
 * One registration/attempt for a libretto activity. An attempt carries up to three
 * outcome facets (written, partial, final); the final one is what lands in the libretto.
 *
 * @property id Esse3 id of the attempt registration.
 * @property callDate Day of the call the attempt belongs to.
 * @property sessionDescription Name of the exam session (sessione) the attempt sits in.
 * @property statusDescription Human description of the attempt's administrative state.
 * @property finalOutcome The verbalised outcome facet, when present.
 * @property writtenOutcome The written-part outcome facet, when present.
 * @property partialOutcome The partial-test outcome facet, when present.
 */
data class CourseAttempt(
    val id: Long,
    val callDate: LocalDate?,
    val sessionDescription: String?,
    val statusDescription: String?,
    val finalOutcome: AttemptOutcome?,
    val writtenOutcome: AttemptOutcome?,
    val partialOutcome: AttemptOutcome?,
) {
    /** The most authoritative facet available: final, then written, then partial. */
    val bestOutcome: AttemptOutcome?
        get() = finalOutcome ?: writtenOutcome ?: partialOutcome
}

/**
 * One outcome facet of an exam attempt as recorded by Esse3.
 *
 * @property passed True when Esse3 marks the facet as passed.
 * @property grade Numeric grade; null for judgment-only outcomes or unmarked facets.
 * @property cumLaude True when the grade carries the lode.
 * @property judgment Textual judgment (e.g. idoneità outcomes) when no grade applies.
 * @property date Day the outcome was recorded.
 */
data class AttemptOutcome(
    val passed: Boolean,
    val grade: Float?,
    val cumLaude: Boolean,
    val judgment: String?,
    val date: LocalDate?,
)
