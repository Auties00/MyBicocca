package it.attendance100.mybicocca.data.remote.easystaff.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import kotlinx.serialization.json.Json

/** How the generated [EasyStaffApi] facade talks to Agenda Web and EasyBadge. */
internal object EasyStaffRuntime {
    private const val TIMEOUT_MILLIS = 30_000L

    /** Lenient on input, since responses add fields over time and PHP mixes scalar types. */
    val json = Json {
        coerceInputValues = true
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    /** The university's servers are slow, so requests get generous timeouts. */
    fun createHttpClient(json: Json, config: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient {
        config()
        install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT_MILLIS
            connectTimeoutMillis = TIMEOUT_MILLIS
            socketTimeoutMillis = TIMEOUT_MILLIS
        }
        followRedirects = true
    }
}
