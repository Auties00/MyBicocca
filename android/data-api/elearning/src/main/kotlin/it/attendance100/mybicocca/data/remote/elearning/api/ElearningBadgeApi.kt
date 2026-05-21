package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.HttpClient
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetUserBadgesRequest
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetUserBadgesResponse
import kotlinx.serialization.json.Json

/**
 * API for badge-related operations.
 *
 * @param client The shared [HttpClient] instance
 * @param json The shared [Json] instance
 */
class ElearningBadgeApi(
    client: HttpClient,
    json: Json
) : ElearningAbstractApi(client, json) {

    /**
     * Gets badges for a user.
     *
     * @param wsToken The web service token
     * @param userId The user ID (null for current user)
     * @param courseId Optional course filter
     * @param page Page number for pagination
     * @param perPage Items per page
     * @param search Optional search filter
     * @param onlyPublic Only return public badges
     * @return User badges
     */
    suspend fun getUserBadges(
        wsToken: String,
        userId: Int? = null,
        courseId: Int? = null,
        page: Int = 0,
        perPage: Int = 0,
        search: String? = null,
        onlyPublic: Boolean = false
    ): ElearningGetUserBadgesResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningGetUserBadgesRequest(userId, courseId, page, perPage, search, onlyPublic)
        )
    }
}
