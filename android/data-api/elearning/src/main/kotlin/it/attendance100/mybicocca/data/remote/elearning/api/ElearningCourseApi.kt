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
     * @param course The course to get contents for
     * @return Course contents with sections and modules wrapped in [ElearningGetCourseContentsResponse]
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the request fails
     */
    suspend fun getCourseContents(wsToken: String, course: ElearningCourse): ElearningGetCourseContentsResponse {
        return getCourseContents(wsToken, course.id)
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
     * Scrapes all course categories from the e-Learning homepage.
     *
     * @return List of course sections, each containing categories.
     */
    suspend fun getCoursesAreas(): List<ElearningCourseArea> {
        val response = client.get(BASE_URL) {
            attributes.put(ElearningAttributes.SkipCookies, Unit)
        }
        val doc = response.toHtml()
        return doc.select("div.frontpage-box").map { sectionElement ->
            val sectionName = sectionElement.selectFirst("h2.navigation-title")?.text()?.trim() ?: "Unknown Section"
            val categories = sectionElement.select("div.card .navigation-block").mapNotNull { item ->
                val link = item.selectFirst("a.card-img-overlay")
                if (link == null) {
                    null
                } else {
                    val catName = link.selectFirst("h5.card-title")?.text()?.trim() ?: "Unknown Category"
                    val catUrl = link.attr("abs:href")
                    ElearningCourseCategory(catName, catUrl)
                }
            }
            ElearningCourseArea(sectionName, categories)
        }
    }

    /**
     * Scrapes items (subcategories and courses) for a given course category.
     *
     * @param category the category of the course
     * @return Content of the category including subcategories and courses.
     */
    suspend fun getCourseCategoryContents(category: ElearningCourseCategory): ElearningCourseCategoryContents {
        val url = category.url
        val response = client.get(url)
        val doc = response.toHtml()

        val name = doc.selectFirst("h1")?.text()?.trim() ?: "Unknown Category"

        val subcategories = doc.select("div.subcategories .category > a.info").map { link ->
            val subName = link.selectFirst("h3.categoryname")?.let { nameTag ->
                nameTag.select(".sr-only").remove()
                nameTag.text().trim()
            } ?: "Unknown Category"
            val subUrl = link.attr("abs:href")
            ElearningCourseCategory(subName, subUrl)
        }

        val courses = doc.select("div.coursebox").mapNotNull { box ->
            val link = box.selectFirst("a.coursename")
            if (link == null) {
                null
            } else {
                val courseUrl = link.attr("abs:href")
                val titleDiv = box.selectFirst(".course-fullname")
                val title = titleDiv?.text()?.trim()
                    ?: link.attr("title").takeIf { it.isNotBlank() }
                    ?: link.text().trim()

                val code = box.selectFirst(".course-shortname")?.text()?.trim() ?: ""

                val courseId = box.attr("data-courseid")
                    .takeIf { it.isNotEmpty() }
                    ?.toIntOrNull()
                    ?: extractQueryParamAsInt(courseUrl, "id")
                    ?: return@mapNotNull null

                val hasPassword = doc.selectFirst("#fitem_id_nokey") == null

                ElearningCourse(title, code, courseUrl, courseId, hasPassword)
            }
        }

        return ElearningCourseCategoryContents(name, subcategories, courses)
    }

    /**
     * Enrolls the current user into a course using self-enrollment.
     *
     * @param wsToken The web service token (32 characters)
     * @param course The course to enroll into
     * @param password The enrollment password, if the course requires one
     * @param instanceId The self-enrollment instance ID, if multiple enrollment methods exist
     * @return Enrollment result wrapped in [ElearningEnrollIntoCourseResponse]
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the request fails or enrollment is not allowed
     */
    suspend fun enrollIntoCourse(
        wsToken: String,
        course: ElearningCourse,
        password: String? = null,
        instanceId: Int? = null
    ): ElearningEnrollIntoCourseResponse {
        return enrollIntoCourse(wsToken, course.id, password, instanceId)
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
     * Scrapes the public course info page (`/course/info.php?id={id}`) for a course.
     *
     * This endpoint is publicly accessible — it requires no token and no SSO login —
     * and returns the syllabus, staff, and "Scheda del corso" metadata for any course,
     * including ones the user is not enrolled in.
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
            metadata = parseCoursePublicMetadata(mainBox),
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

private fun parseCoursePublicMetadata(mainBox: Element): ElearningCoursePublicMetadata {
    val rows = mainBox.select(".course-metadata .metadata-data .row.no-gutters")
    val values = rows.mapNotNull { row ->
        val cols = row.select(".col-6")
        if (cols.size < 2) return@mapNotNull null
        cols[0].text().trim() to cols[1].text().trim()
    }.toMap()

    return ElearningCoursePublicMetadata(
        disciplinarySector = values["Settore disciplinare"],
        cfu = values["CFU"]?.toIntOrNull(),
        period = values["Periodo"],
        activityType = values["Tipo di attività"],
        hours = values["Ore"]?.toIntOrNull(),
        degreeType = values["Tipologia CdS"],
        language = values["Lingua"]
    )
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

