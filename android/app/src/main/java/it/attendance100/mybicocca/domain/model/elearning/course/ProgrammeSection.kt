package it.attendance100.mybicocca.domain.model.elearning.course

/**
 * One topic block of a course's extended programme ("programma esteso"), parsed out of
 * the free-form HTML teachers author in the Moodle course sheet. Rendered as a titled
 * list on the syllabus page.
 *
 * @property title The topic heading; empty for a headerless run of items.
 * @property items The topic's bullet points, one display row each; may be empty for a
 * title-only section.
 */
data class ProgrammeSection(
    val title: String,
    val items: List<String>,
)
