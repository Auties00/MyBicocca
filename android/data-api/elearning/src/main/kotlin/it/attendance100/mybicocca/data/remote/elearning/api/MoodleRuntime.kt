package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** How the generated [MoodleApi] facade talks to the university's Moodle. */
object MoodleRuntime {
    const val BASE_URL = "https://elearning.unimib.it"

    /** The langpack Moodle formats strings in when the caller names none. */
    const val DEFAULT_LANGUAGE = "en"

    private const val TIMEOUT_MILLIS = 30_000L

    /**
     * Lenient on input, since plugins add fields over time; optional fields left at their
     * defaults aren't sent.
     */
    val json = Json {
        coerceInputValues = true
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    fun createHttpClient(json: Json, config: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT_MILLIS
            connectTimeoutMillis = TIMEOUT_MILLIS
            socketTimeoutMillis = TIMEOUT_MILLIS
        }
        install(ContentNegotiation) {
            json(json)
        }
        install(DefaultRequest) {
            accept(ContentType.Application.Json)
        }
        followRedirects = true
        config()
    }

    /**
     * Installs a plugin sending `moodlewssettinglang`, the langpack Moodle formats strings in,
     * with every call: the value [language] returns at the time of the call.
     */
    fun HttpClientConfig<*>.language(language: () -> String) {
        install(createClientPlugin("MoodleLanguage") {
            onRequest { request, _ -> request.url.parameters["moodlewssettinglang"] = language() }
        })
    }
}
