package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.HttpClient
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetActivitiesCompletionStatusRequest
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetActivitiesCompletionStatusResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningUpdateActivityCompletionRequest
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningUpdateActivityCompletionResponse
import kotlinx.serialization.json.Json

/**
 * API for completion-related operations.
 *
 * @param client The shared [HttpClient] instance
 * @param json The shared [Json] instance
 */
class ElearningCompletionApi(
    client: HttpClient,
    json: Json
) : ElearningAbstractApi(client, json) {

    /**
     * Gets completion statuses for all activities in a course.
     *
     * @param wsToken The web service token
     * @param courseId The course ID
     * @param userId The user ID (null for current user)
     * @return Activity completion statuses
     */
    suspend fun getActivitiesCompletionStatus(
        wsToken: String,
        courseId: Int,
        userId: Int? = null
    ): ElearningGetActivitiesCompletionStatusResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningGetActivitiesCompletionStatusRequest(courseId, userId)
        )
    }

    /**
     * Manually updates an activity's completion status.
     *
     * @param wsToken The web service token
     * @param cmId The course module ID
     * @param completed Whether to mark as completed
     * @return Update result
     */
    suspend fun updateActivityCompletion(
        wsToken: String,
        cmId: Int,
        completed: Boolean
    ): ElearningUpdateActivityCompletionResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningUpdateActivityCompletionRequest(cmId, completed)
        )
    }
}
