package it.attendance100.mybicocca.domain.model.elearning.course

/**
 * The official course sheet ("scheda dell'insegnamento") attached to a Moodle course's
 * public page. The page publishes one tab per language; this carries the single tab
 * selected for the user. Rendered by the course detail screen's syllabus page.
 *
 * @property language Language code of the selected tab (e.g. "it", "en").
 * @property exportPdfUrl Link to the downloadable PDF export of the sheet, when offered.
 * @property fields The sheet's free-form sections in page order, as raw HTML.
 * @property info Structured facts (credits, hours, semester, programme, ...) extracted
 * from the sheet fields and the page's metadata sidebar.
 */
data class CourseSyllabusPointer(
    val language: String,
    val exportPdfUrl: String?,
    val fields: List<SyllabusField>,
    val info: SyllabusInfo,
) {
    /**
     * One titled section of the sheet.
     *
     * @property title Section heading as authored on the page (server-localized).
     * @property htmlContent The section body as raw HTML.
     */
    data class SyllabusField(val title: String, val htmlContent: String)
}
