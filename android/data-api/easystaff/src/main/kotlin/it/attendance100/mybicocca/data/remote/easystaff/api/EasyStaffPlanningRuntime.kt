package it.attendance100.mybicocca.data.remote.easystaff.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** How the generated [EasyStaffPlanningApi] facade talks to the Portale Planning backend. */
internal object EasyStaffPlanningRuntime {
    private const val TIMEOUT_MILLIS = 30_000L

    /** Header carrying the locale of the backend's strings. */
    private const val LOCALE_HEADER = "X-APP-LOCALE"

    private const val LOCALE = "it"

    /**
     * Origin of the official web front-end. Booking writes (`/entry/store`) are refused with
     * "Unauthorized origin" unless the request carries it as `Origin` (or a `Referer` on the
     * same host).
     */
    private const val PORTAL_ORIGIN = "https://gestioneorari.didattica.unimib.it"

    /**
     * Lenient on input, since responses add fields over time; optional fields left at their
     * defaults aren't sent, so deleting without `force` sends `{}` as the web front-end does.
     */
    val json = Json {
        coerceInputValues = true
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    /** The client keeps cookies, which carry the guest token (`jwt_public`). */
    fun createHttpClient(json: Json, config: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient {
        config()
        install(HttpCookies)
        install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT_MILLIS
            connectTimeoutMillis = TIMEOUT_MILLIS
            socketTimeoutMillis = TIMEOUT_MILLIS
        }
        install(ContentNegotiation) {
            json(json, contentType = ContentType.Application.Json)
        }
        install(DefaultRequest) {
            header(LOCALE_HEADER, LOCALE)
            header(HttpHeaders.Accept, ContentType.Application.Json)
            header(HttpHeaders.Origin, PORTAL_ORIGIN)
        }
        followRedirects = true
    }
}
