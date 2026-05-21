package it.attendance100.mybicocca.domain.model.elearning.course

fun CourseModule.kalvidresCmIdOrNull(): Int? {
    if (type == ModuleType.Kalvidres) return cmId
    extractKalvidresCmId(url)?.let { return it }
    for (content in contents) {
        extractKalvidresCmId(content.fileUrl)?.let { return it }
    }
    return null
}

private val KALVIDRES_URL_REGEX = Regex(
    """/mod/kalvidres/view\.php\?(?:[^#]*&)?id=(\d+)""",
    RegexOption.IGNORE_CASE,
)

private fun extractKalvidresCmId(url: String?): Int? {
    if (url.isNullOrBlank()) return null
    return KALVIDRES_URL_REGEX.find(url)?.groupValues?.get(1)?.toIntOrNull()
}
