package it.attendance100.mybicocca.domain.model.elearning.course

import java.time.Instant

/**
 * A dated event attached to a course module (opening, due, closing), as shipped in the
 * course-contents web service.
 *
 * @property label Human caption for the date; server-localized Italian on this site
 * ("Data limite:", "Aperto:", "Chiuso:"), so unsuitable for classification.
 * @property instant When the event occurs.
 * @property dataId Stable machine key Moodle ships (e.g. "duedate", "timeclose",
 * "timeopen", "allowsubmissionsfromdate"); the only language-independent way to
 * classify the date.
 */
data class ModuleDate(
    val label: String?,
    val instant: Instant,
    val dataId: String?,
) {
    /** Whether this date closes the activity (submission deadline or quiz close). */
    val isDeadline: Boolean
        get() = dataId == "duedate" || dataId == "timeclose" || dataId == "close"

    /** Whether this date opens the activity for participation. */
    val isOpening: Boolean
        get() = dataId == "allowsubmissionsfromdate" || dataId == "timeopen" || dataId == "open"
}
