package it.attendance100.mybicocca.domain.model.elearning.course

/**
 * Degree level a course belongs to, matched from the free-text degree-type fields of
 * the Moodle course sheet. Shown on the syllabus info tile.
 *
 * @property short Compact Italian degree-class abbreviation ("L", "LM", "D").
 * @property title Italian display name of the level.
 */
enum class CourseLevel(val short: String, val title: String) {
    Bachelor("L", "Triennale"),
    Master("LM", "Magistrale"),
    Doctorate("D", "Dottorato"),
}
