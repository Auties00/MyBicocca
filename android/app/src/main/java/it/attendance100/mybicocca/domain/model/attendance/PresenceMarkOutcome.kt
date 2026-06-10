package it.attendance100.mybicocca.domain.model.attendance

/**
 * Outcome of a presence self-marking attempt, against either a Moodle mod_attendance session
 * or an EasyStaff EasyBadge lesson code. Rendered as result feedback by the registry
 * "Presenze" sub-screen.
 */
sealed interface PresenceMarkOutcome {

    /** Localized message to show the student. */
    val message: String

    /**
     * The presence was recorded.
     *
     * @property statusDescription Description of the attendance status that was assigned
     *   (e.g. "Presente"), when the backend reports one.
     */
    data class Recorded(
        override val message: String,
        val statusDescription: String? = null,
    ) : PresenceMarkOutcome

    /** A presence for the session is already on record. */
    data class AlreadyRecorded(override val message: String) : PresenceMarkOutcome

    /** The supplied session password or lesson code was rejected. */
    data class WrongCredential(override val message: String) : PresenceMarkOutcome

    /** The session is not open for self-marking (closed, network-restricted, or not found). */
    data class NotOpen(override val message: String) : PresenceMarkOutcome

    /** The marking attempt failed for any other reason. */
    data class Failed(override val message: String) : PresenceMarkOutcome
}
