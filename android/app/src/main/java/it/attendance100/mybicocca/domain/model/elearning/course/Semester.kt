package it.attendance100.mybicocca.domain.model.elearning.course

/**
 * Teaching period of a course, matched from the free-text "periodo" fields of the
 * Moodle course sheet. Backs the semester strip on the syllabus info tile.
 *
 * @property activeMonthIndices Indices into the 12-month academic strip starting at
 * September (0 = Sep, 11 = Aug) that the period covers, matching the strip the
 * syllabus tile renders.
 * @property title Italian display name of the period.
 * @property rangeLabel Italian month-range caption shown under the title.
 */
enum class Semester(
    val activeMonthIndices: Set<Int>,
    val title: String,
    val rangeLabel: String,
) {
    First(activeMonthIndices = (0..5).toSet(), title = "Primo semestre", rangeLabel = "settembre — febbraio"),
    Second(activeMonthIndices = (6..11).toSet(), title = "Secondo semestre", rangeLabel = "marzo — agosto"),
    FullYear(activeMonthIndices = (0..11).toSet(), title = "Annuale", rangeLabel = "settembre — agosto"),
}
