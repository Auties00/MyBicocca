package it.attendance100.mybicocca.data.api.elearning

import io.ktor.client.HttpClient
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetCourseGradesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetCourseGradesResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetGradeItemsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetGradeItemsResponse
import kotlinx.serialization.json.Json

/**
 * API for grade-related operations.
 *
 * @param client The shared [HttpClient] instance
 * @param json The shared [Json] instance
 */
class ElearningGradeApi(
    client: HttpClient,
    json: Json
) : ElearningAbstractApi(client, json) {

    /**
     * Gets grade items for a course.
     *
     * @param wsToken The web service token
     * @param courseId The course ID
     * @param userId Optional user ID
     * @param groupId Optional group ID
     * @return Grade items with user grades
     */
    suspend fun getGradeItems(
        wsToken: String,
        courseId: Int,
        userId: Int? = null,
        groupId: Int? = null
    ): ElearningGetGradeItemsResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningGetGradeItemsRequest(courseId, userId, groupId)
        )
    }

    /**
     * Gets course grades overview for a user.
     *
     * @param wsToken The web service token
     * @param userId Optional user ID (null for current user)
     * @return Course grades overview
     */
    suspend fun getCourseGrades(
        wsToken: String,
        userId: Int? = null
    ): ElearningGetCourseGradesResponse {
        return executeAuthenticatedRequest(wsToken, ElearningGetCourseGradesRequest(userId))
    }
}
