package it.attendance100.mybicocca.data.api.elearning

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetPublicConfigResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetSiteInfoRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetSiteInfoResponse
import kotlinx.serialization.json.*

/**
 * API for site-level operations and authentication.
 *
 * @param client The shared [HttpClient] instance
 * @param json The shared [Json] instance
 */
class ElearningSiteApi(
    client: HttpClient,
    json: Json
) : ElearningAbstractApi(client, json) {
    /**
     * Gets site information and current user details.
     *
     * @param wsToken The web service token (32 characters)
     * @return Site and user information
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the request fails
     */
    suspend fun getSiteInfo(wsToken: String): ElearningGetSiteInfoResponse {
        return executeAuthenticatedRequest(wsToken, ElearningGetSiteInfoRequest())
    }
}
