package it.attendance100.mybicocca.data.api.elearning

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.ParametersBuilder
import it.attendance100.mybicocca.data.dto.elearning.ElearningRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive

/**
 * Abstract base class for all Elearning API implementations.
 *
 * This class provides the common infrastructure for making authenticated requests
 * to the Moodle web services API. All concrete API implementations should extend
 * this class and use [executeAuthenticatedRequest] to make their API calls.
 *
 * @param client The shared [HttpClient] instance for making HTTP requests
 * @param json The shared [Json] instance for serialization/deserialization
 */
abstract class ElearningAbstractApi(
    protected val client: HttpClient,
    protected val json: Json
) {
    companion object {
        /**
         * Base URL for all Moodle web service requests.
         */
        const val BASE_URL = "https://elearning.unimib.it/"

        /**
         * Expected length of a valid Moodle web service token.
         */
        const val WS_TOKEN_LENGTH = 32
    }

    /**
     * Executes an authenticated request to the Moodle web services API.
     *
     * @param T The expected response type, must implement [ElearningResponse]
     * @param wsToken The web service token for authentication (must be 32 characters)
     * @param requestArgs The request object containing the function name and parameters
     * @return The parsed response of type [T]
     * @throws IllegalArgumentException If the token is not 32 characters
     * @throws IllegalStateException If the HTTP request fails or Moodle returns an error
     */
    protected suspend inline fun <reified T : ElearningResponse> executeAuthenticatedRequest(
        wsToken: String,
        requestArgs: ElearningRequest<T>
    ): T {
        if (wsToken.length != WS_TOKEN_LENGTH) {
            throw IllegalArgumentException("Invalid wsToken '$wsToken': expected a $WS_TOKEN_LENGTH-character string")
        }

        val formData = ParametersBuilder().apply {
            append("wsfunction", requestArgs.functionName)
            append("wstoken", wsToken)
            append("moodlewssettingfilter", "true")
            append("moodlewssettingfileurl", "true")
            append("moodlewssettinglang", "en_us")
            requestArgs.writeAdditionalData(this)
        }.build()

        val response = client.post("$BASE_URL/webservice/rest/server.php") {
            url {
                parameters.append("moodlewsrestformat", "json")
                parameters.append("wsfunction", requestArgs.functionName)
            }
            setBody(FormDataContent(formData))
            accept(ContentType.Application.Json)
        }

        if (response.status != HttpStatusCode.OK) {
            throw IllegalStateException("Invalid response status: ${response.status}")
        }

        when (val element = response.body<JsonElement>()) {
            is JsonArray -> {
                val wrappedElement = buildJsonObject {
                    put("items", element)
                }
                return json.decodeFromJsonElement<T>(wrappedElement)
            }

            is JsonObject -> {
                val isError = element["error"]?.jsonPrimitive?.booleanOrNull == true
                if (isError) {
                    val exceptionData = element["exception"]?.jsonPrimitive?.contentOrNull
                    if (exceptionData == null) {
                        val errorCode = element["errorcode"]?.jsonPrimitive?.contentOrNull
                        val message = element["message"]?.jsonPrimitive?.contentOrNull
                        throw IllegalStateException("Moodle error: ${errorCode ?: "unknown"} - ${message ?: "unknown error"}")
                    } else {
                        val exception = json.decodeFromString<JsonObject>(exceptionData)
                        val errorCode =
                            exception["errorcode"]?.jsonPrimitive?.contentOrNull ?: "unknown"
                        val errorModule = exception["module"]?.jsonPrimitive?.contentOrNull ?: "unknown"
                        throw IllegalStateException("Moodle error: $errorCode in module $errorModule")
                    }
                } else {
                    val errorCode = element["errorcode"]?.jsonPrimitive?.contentOrNull
                    if (errorCode != null) {
                        val message = element["message"]?.jsonPrimitive?.contentOrNull ?: "unknown error"
                        throw IllegalStateException("Moodle error: $errorCode - $message")
                    } else {
                        return json.decodeFromJsonElement<T>(element)
                    }
                }
            }

            else -> throw IllegalStateException("Invalid response type: $element")
        }
    }
}
