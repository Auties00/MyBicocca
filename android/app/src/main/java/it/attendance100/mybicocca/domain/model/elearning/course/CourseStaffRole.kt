package it.attendance100.mybicocca.domain.model.elearning.course

/**
 * Role group a staff member is listed under on a Moodle course's public page. The site
 * labels these groups in Italian; unrecognized labels collapse to [Other].
 *
 * @property raw The role label as it appears on the course page.
 */
enum class CourseStaffRole(val raw: String) {
    Docente("Docente"),
    Tutor("Tutor"),
    Esercitatore("Esercitatore"),
    Other("Other");

    companion object {
        fun fromRaw(raw: String?): CourseStaffRole = when (raw?.trim()?.lowercase()) {
            null, "" -> Other
            "docente" -> Docente
            "tutor" -> Tutor
            "esercitatore" -> Esercitatore
            else -> Other
        }
    }
}
