package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.*
import io.ktor.client.request.*
import it.attendance100.mybicocca.data.remote.common.util.extractQueryParamAsInt
import it.attendance100.mybicocca.data.remote.common.util.toHtml
import it.attendance100.mybicocca.data.remote.elearning.dto.*
import kotlinx.serialization.json.Json
import org.jsoup.nodes.Element

/**
 * The public course pages, which no web service exposes: each course's info page.
 *
 * @param client The shared [HttpClient] instance
 * @param json The shared [Json] instance
 */
class ElearningCourseApi(
    client: HttpClient,
    json: Json
) : ElearningAbstractApi(client, json) {

    /**
     * Scrapes the public course info page (`/course/info.php?id={id}`) for a course.
     *
     * This endpoint is publicly accessible — it requires no token and no SSO login —
     * and returns the syllabus and staff for any course, including ones the user is not
     * enrolled in. The "Scheda del corso" metadata on the same page comes from
     * the course's custom fields instead (`core_course_get_courses_by_field`).
     *
     * @param courseId The course ID to fetch.
     * @return The parsed [ElearningCoursePublicInfo].
     */
    suspend fun getCoursePublicInfo(courseId: Int): ElearningCoursePublicInfo {
        val response = client.get("${BASE_URL}course/info.php") {
            url.parameters.append("id", courseId.toString())
            attributes.put(ElearningAttributes.SkipCookies, Unit)
        }
        val doc = response.toHtml()

        val mainBox = doc.selectFirst(".coursebox.has-info.has-metadata")
            ?: error("course info page for id=$courseId is missing the main coursebox")
        val resolvedId = mainBox.attr("data-courseid").toIntOrNull() ?: courseId

        val header = doc.selectFirst("#course-header .coursename") ?: doc
        val name = header.selectFirst(".course-fullname")?.text()?.trim().orEmpty()
        val code = header.selectFirst(".course-shortname")?.text()?.trim().orEmpty()
        val viewUrl = mainBox.selectFirst(".course-access a[href]")?.attr("abs:href").orEmpty()

        return ElearningCoursePublicInfo(
            id = resolvedId,
            name = name,
            code = code,
            viewUrl = viewUrl,
            syllabus = parseCourseSyllabus(mainBox),
            staff = parseCourseStaff(mainBox),
            enrolmentMethods = parseCourseEnrolmentMethods(mainBox),
            studentOpinionUrl = mainBox.selectFirst(".course-opinion .opinion-data a[href]")
                ?.attr("abs:href")?.takeIf { it.isNotBlank() },
            bibliographyUrl = mainBox.selectFirst(".course-biblio .biblio-data a[href]")
                ?.attr("abs:href")?.takeIf { it.isNotBlank() }
        )
    }
}

private fun parseCourseSyllabus(mainBox: Element): List<ElearningCourseSyllabus> {
    return mainBox.select(".tab-content > .tab-pane").map { tab ->
        val language = tab.id().ifBlank { "unknown" }
        val exportUrl = tab.selectFirst(".syllabus-actions a[href*=syllabus/export.php]")
            ?.attr("abs:href")
            ?.takeIf { it.isNotBlank() }
        val fields = tab.select(".field").map { field ->
            ElearningCourseSyllabusField(
                title = field.selectFirst(".field-title")?.text()?.trim().orEmpty(),
                htmlContent = field.selectFirst(".field-content")?.html()?.trim().orEmpty()
            )
        }
        ElearningCourseSyllabus(language = language, exportPdfUrl = exportUrl, fields = fields)
    }
}

private fun parseCourseStaff(mainBox: Element): List<ElearningCourseStaffGroup> {
    val list = mainBox.selectFirst(".course-contacts ul.summary-content.teachers") ?: return emptyList()
    val groups = mutableListOf<ElearningCourseStaffGroup>()
    var currentRole: String? = null
    val currentMembers = mutableListOf<ElearningCourseStaffMember>()

    fun flush() {
        val role = currentRole
        if (role != null) {
            groups.add(ElearningCourseStaffGroup(role = role, members = currentMembers.toList()))
        }
        currentMembers.clear()
    }

    for (child in list.children()) {
        when {
            child.tagName() == "h4" && child.hasClass("contact-role") -> {
                flush()
                currentRole = child.text().trim()
            }
            child.tagName() == "li" && child.hasClass("contact") -> {
                val profileUrl = child.selectFirst(".contact-picture a[href]")?.attr("abs:href").orEmpty()
                val rowId = child.id()
                val email = rowId.removePrefix("contact-").takeIf { it.contains('@') }
                currentMembers.add(
                    ElearningCourseStaffMember(
                        name = child.selectFirst(".contact-name")?.text()?.trim().orEmpty(),
                        profileUrl = profileUrl,
                        userId = extractQueryParamAsInt(profileUrl, "id"),
                        initials = child.selectFirst(".userinitials")?.text()?.trim(),
                        email = email
                    )
                )
            }
        }
    }
    flush()
    return groups
}

private fun parseCourseEnrolmentMethods(mainBox: Element): List<String> {
    return mainBox.select(".course-enrolments .enrols-data .row.no-gutters .col-6.font-weight-bold")
        .map { it.text().trim() }
        .filter { it.isNotEmpty() }
}

