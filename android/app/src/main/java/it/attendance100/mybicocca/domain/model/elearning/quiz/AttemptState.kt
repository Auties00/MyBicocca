package it.attendance100.mybicocca.domain.model.elearning.quiz

/**
 * Lifecycle state of a quiz attempt as reported by Moodle's mod_quiz web services. The quiz
 * detail sheet keys its primary action on this: an in-progress attempt becomes the resume CTA.
 *
 * @property raw Wire value used by the web services.
 */
enum class AttemptState(val raw: String) {
    /** The attempt is open and can be resumed. */
    InProgress("inprogress"),

    /** The time limit expired and the attempt sits in the grace period before being closed. */
    Overdue("overdue"),

    /** The attempt was submitted. */
    Finished("finished"),

    /** The attempt was never submitted and was closed by the platform. */
    Abandoned("abandoned"),

    /** Sentinel for wire values this app does not recognize. */
    Unknown("unknown");

    companion object {
        /** Resolves a wire value case-insensitively, falling back to [Unknown]. */
        fun fromRaw(raw: String?): AttemptState =
            entries.firstOrNull { it.raw.equals(raw, ignoreCase = true) } ?: Unknown
    }
}
