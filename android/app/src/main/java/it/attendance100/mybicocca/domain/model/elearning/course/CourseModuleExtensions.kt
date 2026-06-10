package it.attendance100.mybicocca.domain.model.elearning.course

/**
 * Resolves the course-module id of the Kaltura video behind this module, or null when
 * the module is not video-backed. Direct kalvidres modules answer with their own cmId;
 * link-style modules (URL resources pointing at a kalvidres view page) are recognized
 * by inspecting their target and content URLs.
 */
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
