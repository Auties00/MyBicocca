package it.attendance100.mybicocca.domain.model.attendance

import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.text.UiText

/**
 * Outcome of a presence self-marking attempt, against either a Moodle mod_attendance session
 * or an EasyStaff EasyBadge lesson code. Rendered as result feedback by the registry
 * "Presenze" sub-screen.
 */
sealed interface PresenceMarkOutcome {

    /** Localized message to show the student. */
    val message: UiText

    /**
     * The presence was recorded.
     *
     * @property statusDescription Description of the attendance status that was assigned
     *   (e.g. "Presente"), when the backend reports one.
     */
    data class Recorded(
        override val message: UiText = UiText.StringResource(R.string.attendance_msg_recorded),
        val statusDescription: String? = null,
    ) : PresenceMarkOutcome

    /** A presence for the session is already on record. */
    data class AlreadyRecorded(
        override val message: UiText = UiText.StringResource(R.string.attendance_msg_already_recorded),
    ) : PresenceMarkOutcome

    /** The supplied session password or lesson code was rejected. */
    data class WrongCredential(
        override val message: UiText = UiText.StringResource(R.string.attendance_msg_wrong_credential),
    ) : PresenceMarkOutcome

    /** The session is not open for self-marking (closed, network-restricted, or not found). */
    data class NotOpen(
        override val message: UiText = UiText.StringResource(R.string.attendance_msg_session_closed),
    ) : PresenceMarkOutcome

    /** The session is not open because of a network restriction. */
    data class NetworkRestricted(
        override val message: UiText = UiText.StringResource(R.string.attendance_msg_network_wrong),
    ) : PresenceMarkOutcome

    /** The session is not open because device is already used. */
    data class DeviceAlreadyUsed(
        override val message: UiText = UiText.StringResource(R.string.attendance_msg_network_prevent_shared),
    ) : PresenceMarkOutcome

    /** The marking attempt failed for any other reason. */
    data class Failed(
        override val message: UiText = UiText.StringResource(R.string.attendance_msg_failed),
        val backendMessage: String? = null,
    ) : PresenceMarkOutcome
}
