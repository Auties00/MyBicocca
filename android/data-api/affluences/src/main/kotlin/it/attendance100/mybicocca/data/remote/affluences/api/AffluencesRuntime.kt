package it.attendance100.mybicocca.data.remote.affluences.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** How the generated [AffluencesApi] facade talks to Affluences. */
internal object AffluencesRuntime {
    private const val TIMEOUT_MILLIS = 30_000L

    /** Language of the server's strings, such as error messages. */
    private const val LANGUAGE = "it"

    /**
     * The app API runs device detection on the user agent and rejects unrecognised clients
     * with "Invalid device", so requests carry a browser's.
     */
    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36"

    /**
     * Lenient on input, since responses add fields over time; optional fields left at their
     * defaults aren't sent, which the search filters rely on.
     */
    val json = Json {
        coerceInputValues = true
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    fun createHttpClient(json: Json, config: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient {
        config()
        install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT_MILLIS
            connectTimeoutMillis = TIMEOUT_MILLIS
            socketTimeoutMillis = TIMEOUT_MILLIS
        }
        install(ContentNegotiation) {
            json(json, contentType = ContentType.Application.Json)
        }
        install(DefaultRequest) {
            header(HttpHeaders.AcceptLanguage, LANGUAGE)
        }
        install(UserAgent) {
            agent = USER_AGENT
        }
        followRedirects = true
    }
}
