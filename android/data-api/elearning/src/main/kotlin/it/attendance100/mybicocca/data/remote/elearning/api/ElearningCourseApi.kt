package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.*
import io.ktor.client.request.*
import it.attendance100.mybicocca.data.remote.common.util.extractQueryParamAsInt
import it.attendance100.mybicocca.data.remote.common.util.toHtml
import it.attendance100.mybicocca.data.remote.elearning.dto.*
import kotlinx.serialization.json.Json
import org.jsoup.nodes.Element

/**
 * API for course-related operations.
 *
 * Enrollment is one-way: Moodle exposes no web service to unenroll from a course
 * (see [MDL-30063](https://moodle.atlassian.net/browse/MDL-30063)).
 *
 * @param client The shared [HttpClient] instance
 * @param json The shared [Json] instance
 */
class ElearningCourseApi(
    client: HttpClient,
    json: Json
) : ElearningAbstractApi(client, json) {

    /**
     * Gets all courses the user is enrolled in.
     *
     * @param wsToken The web service token (32 characters)
     * @param userId The user ID to get courses for (from site info)
     * @return List of enrolled courses wrapped in [ElearningGetUserCoursesResponse]
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the request fails
     */
    suspend fun getUserCourses(wsToken: String, userId: Int): ElearningGetUserCoursesResponse {
        return executeAuthenticatedRequest(wsToken, ElearningGetUserCoursesRequest(userId))
    }

    /**
     * Gets the contents of a course (sections and modules).
     *
     * @param wsToken The web service token (32 characters)
     * @param courseId The course ID to get contents for
     * @return Course contents with sections and modules wrapped in [ElearningGetCourseContentsResponse]
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the request fails
     */
    suspend fun getCourseContents(wsToken: String, courseId: Int): ElearningGetCourseContentsResponse {
        return executeAuthenticatedRequest(wsToken, ElearningGetCourseContentsRequest(courseId))
    }

    /**
     * Enrolls the current user into a course using self-enrollment.
     *
     * @param wsToken The web service token (32 characters)
     * @param courseId The course ID to enroll into
     * @param password The enrollment password, if the course requires one
     * @param instanceId The self-enrollment instance ID, if multiple enrollment methods exist
     * @return Enrollment result wrapped in [ElearningEnrollIntoCourseResponse]
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the request fails or enrollment is not allowed
     */
    suspend fun enrollIntoCourse(
        wsToken: String,
        courseId: Int,
        password: String? = null,
        instanceId: Int? = null
    ): ElearningEnrollIntoCourseResponse {
        return executeAuthenticatedRequest(wsToken, ElearningEnrollIntoCourseRequest(courseId, password, instanceId))
    }

    /**
     * Gets the "Scheda del corso" metadata of a course from its custom fields, for any course
     * the token's user can see, including ones they are not enrolled in.
     *
     * Categorical values come back localized in the client's language
     * (see [ElearningApi.language]).
     *
     * @param wsToken The web service token (32 characters)
     * @param courseId The course ID to fetch.
     * @return The course's [ElearningCourseMetadata], or null if no visible course has that id.
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the request fails
     */
    suspend fun getCourseMetadata(wsToken: String, courseId: Int): ElearningCourseMetadata? {
        val response = executeAuthenticatedRequest(wsToken, ElearningGetCoursesByFieldRequest("id", courseId.toString()))
        val values = response.courses.firstOrNull()?.customFields
            ?.associate { it.shortName to it.value?.trim()?.takeIf(String::isNotEmpty) }
            ?: return null
        return ElearningCourseMetadata(
            disciplinarySector = values["settore"],
            cfu = values["cfu"]?.toIntOrNull(),
            period = values["periodo"],
            activityType = values["tipo"],
            hours = values["ore"]?.toIntOrNull()?.takeIf { it > 0 },
            degreeType = values["tipologiacds"],
            deliveryMode = values["erogazione"],
            language = values["lingua"]
        )
    }

    /**
     * Scrapes the public course info page (`/course/info.php?id={id}`) for a course.
     *
     * This endpoint is publicly accessible — it requires no token and no SSO login —
     * and returns the syllabus and staff for any course, including ones the user is not
     * enrolled in. The "Scheda del corso" metadata on the same page comes from
     * [getCourseMetadata] instead.
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

