package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import kotlinx.serialization.json.Json

/** How the generated [MoodleWebApi] facade fetches pages: without cookies, so as an anonymous visitor. */
internal object MoodleWebRuntime {
    val json = Json { ignoreUnknownKeys = true }

    fun createHttpClient(json: Json, config: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient {
        config()
        followRedirects = true
    }
}
