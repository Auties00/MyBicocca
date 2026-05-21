package it.attendance100.mybicocca.domain.model.elearning.course

data class CourseSyllabusPointer(
    val language: String,
    val exportPdfUrl: String?,
    val fields: List<SyllabusField>,
    val info: SyllabusInfo,
) {
    data class SyllabusField(val title: String, val htmlContent: String)
}
