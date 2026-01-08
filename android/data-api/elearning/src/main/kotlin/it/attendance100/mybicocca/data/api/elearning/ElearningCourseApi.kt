package it.attendance100.mybicocca.data.api.elearning

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import it.attendance100.mybicocca.data.dto.elearning.ElearningCourse
import it.attendance100.mybicocca.data.dto.elearning.ElearningCourseArea
import it.attendance100.mybicocca.data.dto.elearning.ElearningCourseCategory
import it.attendance100.mybicocca.data.dto.elearning.ElearningCourseCategoryContents
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetCourseContentsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetCourseContentsResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetUserCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetUserCoursesResponse
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
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
        val html = response.bodyAsText()
        val doc = Jsoup.parse(html, BASE_URL)
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
        val html = response.bodyAsText()
        val doc = Jsoup.parse(html, url)

        val name = doc.selectFirst("h1")?.text()?.trim() ?: "Unknown Category"

        val subcategories = doc.select("div.subcategories .category > a.info").map { link ->
            val subName = link.selectFirst("h3.categoryname")?.let { nameTag ->
                nameTag.select(".sr-only").remove()
                nameTag.text().trim()
            } ?: "Unknown Category"
            val subUrl = link.attr("abs:href")
            ElearningCourseCategory(subName, subUrl)
        }

        val courses =  doc.select("div.coursebox").mapNotNull { box ->
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

                var courseId = box.attr("data-courseid")
                if (courseId.isBlank()) {
                    val uri = URI(courseUrl)
                    val query = uri.query
                    if (query != null) {
                        val params = query.split("&").associate {
                            val parts = it.split("=")
                            if (parts.size == 2) parts[0] to parts[1] else parts[0] to ""
                        }
                        courseId = params.getOrDefault("id", "")
                    }
                }

                ElearningCourse(title, code, courseUrl, courseId)
            }
        }

        return ElearningCourseCategoryContents(name, subcategories, courses)
    }
}

