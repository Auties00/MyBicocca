package it.attendance100.mybicocca.data.api.elearning

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.statement.*
import io.ktor.http.Parameters
import io.ktor.http.parseQueryString
import io.ktor.utils.io.jvm.javaio.*
import it.attendance100.mybicocca.data.dto.elearning.*
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.nodes.FormElement
import java.net.URI

/**
 * API for course-related operations.
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
     * @throws IllegalStateException If the request fails
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
     * @throws IllegalStateException If the request fails
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
     * @throws IllegalStateException If the request fails
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
        val response = client.get(BASE_URL)
        val doc = Jsoup.parse(response.bodyAsChannel().toInputStream(), "UTF-8", BASE_URL)
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
        val doc = Jsoup.parse(response.bodyAsChannel().toInputStream(), "UTF-8", BASE_URL)

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
                    ?: URI(courseUrl).query.let { parseQueryString(it)["id"]?.toIntOrNull() }
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
     * @throws IllegalStateException If the request fails or enrollment is not allowed
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
     * @throws IllegalStateException If the request fails or enrollment is not allowed
     */
    suspend fun enrollIntoCourse(
        wsToken: String,
        courseId: Int,
        password: String? = null,
        instanceId: Int? = null
    ): ElearningEnrollIntoCourseResponse {
        return executeAuthenticatedRequest(wsToken, ElearningEnrollIntoCourseRequest(courseId, password, instanceId))
    }

    // There is no method to unenroll from a course
    // https://moodle.atlassian.net/browse/MDL-30063
}

