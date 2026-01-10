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
     * Gets the SSO authentication URL for user login.
     *
     * This URL should be opened in a browser. After successful authentication,
     * the browser will be redirected to a `moodlemobile://` URL containing
     * the web service token.
     *
     * ## Token Extraction
     *
     * The redirect URL format is:
     * `moodlemobile://token=BASE64_ENCODED_DATA`
     *
     * The BASE64 decoded data contains the token in the format:
     * `SITE_URL:::TOKEN:::OTHER_DATA`
     *
     * @return The authentication URL to open in a browser
     * @throws IllegalStateException If the public config cannot be retrieved
     */
    suspend fun getAuthUrl(): String {
        val moodleRequest = buildJsonArray {
            add(buildJsonObject {
                put("index", 0)
                put("methodname", "tool_mobile_get_public_config")
                put("args", buildJsonObject {
                    // Empty args
                })
            })
        }

        val httpResponse = client.post("$BASE_URL/lib/ajax/service.php") {
            contentType(ContentType.Application.Json)
            setBody(moodleRequest)
        }

        val responseArray = httpResponse.body<JsonArray>()
        if (responseArray.isEmpty()) throw IllegalStateException("Empty response from server")
        val responseJson = responseArray[0].jsonObject

        if (responseJson.containsKey("error")) {
            val errorElement = responseJson["error"]
            if (errorElement is JsonPrimitive && errorElement.contentOrNull != "false") {
                throw IllegalStateException("Cannot get auth url: ${errorElement.contentOrNull ?: "unknown error"}")
            }
        }

        val data = responseJson["data"]
            ?: throw IllegalStateException("Invalid response: 'data' field missing")
        val config = json.decodeFromJsonElement<ElearningGetPublicConfigResponse>(data)

        val baseUrl = config.launchUrl ?: throw IllegalStateException("No auth url found")
        return URLBuilder(baseUrl).apply {
            parameters.append("service", "moodle_mobile_app")
            parameters.append("passport", (Math.random() * 1000).toString())
        }.buildString()
    }

    /**
     * Gets site information and current user details.
     *
     * @param wsToken The web service token (32 characters)
     * @return Site and user information
     * @throws IllegalArgumentException If the token is invalid
     * @throws IllegalStateException If the request fails
     */
    suspend fun getSiteInfo(wsToken: String): ElearningGetSiteInfoResponse {
        return executeAuthenticatedRequest(wsToken, ElearningGetSiteInfoRequest())
    }
}
