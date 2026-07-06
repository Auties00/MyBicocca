package it.attendance100.mybicocca.domain.model.elearning.course

/**
 * Degree level a course belongs to, matched from the free-text degree-type fields of
 * the Moodle course sheet. Shown on the syllabus info tile. Both the compact pip code and
 * the full display name are resolved to localized string resources at the UI layer, so this
 * domain enum carries no display text.
 */
enum class CourseLevel {
    Bachelor,
    Master,
    Doctorate,
}
