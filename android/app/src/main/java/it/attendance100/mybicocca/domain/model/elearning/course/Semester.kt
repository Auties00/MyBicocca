package it.attendance100.mybicocca.domain.model.elearning.course

// activeMonthIndices indexes into the 12-month academic strip starting at Sep
// (0 = Sep, 11 = Aug), matching the strip the syllabus tile renders.
enum class Semester(
    val activeMonthIndices: Set<Int>,
    val title: String,
    val rangeLabel: String,
) {
    First(activeMonthIndices = (0..5).toSet(), title = "Primo semestre", rangeLabel = "settembre — febbraio"),
    Second(activeMonthIndices = (6..11).toSet(), title = "Secondo semestre", rangeLabel = "marzo — agosto"),
    FullYear(activeMonthIndices = (0..11).toSet(), title = "Annuale", rangeLabel = "settembre — agosto"),
}
