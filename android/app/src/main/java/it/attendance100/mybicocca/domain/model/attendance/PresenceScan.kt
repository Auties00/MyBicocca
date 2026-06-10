package it.attendance100.mybicocca.domain.model.attendance

/**
 * Classified payload of a scanned presence QR code, as handled by the registry "Presenze"
 * sub-screen's marking flow.
 */
sealed interface PresenceScan {

    /**
     * A Moodle mod_attendance link (attendance.php) carrying the session id and the optional
     * QR password.
     *
     * @property sessionId Value of the "sessid" query parameter.
     * @property password Value of the "qrpass" query parameter, when present.
     */
    data class SessionLink(val sessionId: String, val password: String?) : PresenceScan

    /**
     * An EasyStaff EasyBadge lesson code, used for the no-auth GPS-based certification.
     *
     * @property code The "codice_lezione" to certify against.
     */
    data class LessonCode(val code: String) : PresenceScan

    /**
     * Content that matches no known presence format; kept verbatim for error feedback.
     *
     * @property raw The scanned text as-is.
     */
    data class Unrecognized(val raw: String) : PresenceScan
}
