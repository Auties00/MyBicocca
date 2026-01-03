package it.attendance100.mybicocca.data.api.elearning

import io.ktor.client.HttpClient
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetAssignmentsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetAssignmentsResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetSubmissionStatusRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetSubmissionStatusResponse
import kotlinx.serialization.json.Json

/**
 * API for assignment-related operations.
 *
 * @param client The shared [HttpClient] instance
 * @param json The shared [Json] instance
 */
class ElearningAssignApi(
    client: HttpClient,
    json: Json
) : ElearningAbstractApi(client, json) {

    /**
     * Gets assignments for courses.
     */
    suspend fun getAssignments(
        wsToken: String,
        courseIds: List<Int>,
        capabilities: List<String>? = null,
        includeNotEnrolledCourses: Boolean = false
    ): ElearningGetAssignmentsResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningGetAssignmentsRequest(courseIds, capabilities, includeNotEnrolledCourses)
        )
    }

    /**
     * Gets assignments for a single course.
     */
    suspend fun getAssignmentsForCourse(wsToken: String, courseId: Int): ElearningGetAssignmentsResponse {
        return getAssignments(wsToken, listOf(courseId))
    }

    /**
     * Gets submission status for an assignment.
     */
    suspend fun getSubmissionStatus(
        wsToken: String,
        assignId: Int,
        userId: Int? = null,
        groupId: Int? = null
    ): ElearningGetSubmissionStatusResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningGetSubmissionStatusRequest(assignId, userId, groupId)
        )
    }
}
