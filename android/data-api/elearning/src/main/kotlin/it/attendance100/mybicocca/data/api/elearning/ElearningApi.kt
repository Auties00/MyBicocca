package it.attendance100.mybicocca.data.api.elearning

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetPublicConfigRequestArgs
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetPublicConfigResponseData
import it.attendance100.mybicocca.data.dto.elearning.ElearningRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningRequestArgs
import it.attendance100.mybicocca.data.dto.elearning.ElearningResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningResponseData
import kotlinx.serialization.json.Json

class ElearningApi : AutoCloseable {
    companion object {
        private const val BASE_URL = "https://elearning.unimib.it/"
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun getAuthUrl(): String {
        return when (val response = this(ElearningGetPublicConfigRequestArgs())) {
            is ElearningResponse.Error -> throw RuntimeException("Error getting auth url: ${response.message}")
            is ElearningResponse.Success<ElearningGetPublicConfigResponseData> -> {
                val baseUrl = response.data.launchurl ?: throw RuntimeException("No auth url found")
                URLBuilder(baseUrl).apply {
                    parameters.append(
                        "service",
                        "moodle_mobile_app"
                    )

                    // https://github.com/moodlehq/moodleapp/blob/ef7fdd6a8df0a63ec8380ec013260f3d9cbdce9a/src/core/features/login/services/login-helper.ts#L792
                    parameters.append(
                        "passport",
                        (Math.random() * 1000).toString()
                    )
                }.buildString()
            }
        }
    }

    private suspend inline operator fun <reified REQUEST_ARGS : ElearningRequestArgs<RESPONSE_DATA>, reified RESPONSE_DATA : ElearningResponseData> invoke(
        requestArgs: REQUEST_ARGS
    ): ElearningResponse<RESPONSE_DATA> {
        val request: ElearningRequest<REQUEST_ARGS> =
            ElearningRequest(0, requestArgs.methodName, requestArgs)
        val response: HttpResponse = client.post("$BASE_URL/lib/ajax/service.php") {
            contentType(ContentType.Application.Json)
            setBody(listOf(request))
        }
        return if (response.status != HttpStatusCode.OK) {
            ElearningResponse.Error("Invalid response status: ${response.status}")
        } else {
            val body: List<ElearningResponse<RESPONSE_DATA>> = response.body()
            body.firstOrNull() ?: ElearningResponse.Error("No response found")
        }
    }

    override fun close() {
        client.close()
    }
}