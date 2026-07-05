package it.attendance100.mybicocca.domain.model.elearning.course

/**
 * Teaching period of a course, matched from the free-text "periodo" fields of the
 * Moodle course sheet. Backs the semester strip on the syllabus info tile. The display
 * name and month-range caption are resolved to localized string resources at the UI
 * layer, so this domain enum carries only the strip geometry.
 *
 * @property activeMonthIndices Indices into the 12-month academic strip starting at
 * September (0 = Sep, 11 = Aug) that the period covers, matching the strip the
 * syllabus tile renders.
 */
enum class Semester(
    val activeMonthIndices: Set<Int>,
) {
    First(activeMonthIndices = (0..5).toSet()),
    Second(activeMonthIndices = (6..11).toSet()),
    FullYear(activeMonthIndices = (0..11).toSet()),
}
