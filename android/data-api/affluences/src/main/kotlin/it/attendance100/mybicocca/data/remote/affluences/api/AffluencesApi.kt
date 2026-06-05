package it.attendance100.mybicocca.data.remote.affluences.api

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Main entry point for the Affluences API.
 *
 * Affluences (affluences.com) is the platform used by libraries, museums, and other public
 * venues to publish real-time occupancy, opening hours, and seat booking. The University of
 * Milano-Bicocca libraries use it for live occupancy and seat reservations.
 *
 * This class provides access to all Affluences functionality:
 * - Site pages: information, live occupancy, forecasts, timetables, details, related sites
 * - Search and discovery: free-text search, filtered search, map search, suggestions
 * - Reservations: resource availability and the full booking lifecycle
 *
 * No authentication is required: reads are public, and bookings are validated through a code
 * sent to the user email (or through the single sign-on of the site, when it requires one).
 *
 * @param language The language for localized strings, sent as the `Accept-Language` header
 * of every request.
 * @param httpClientConfig Optional configuration block for the underlying HTTP client.
 *
 * @see AffluencesSitesApi for site page operations
 * @see AffluencesSearchApi for search and discovery operations
 * @see AffluencesReservationApi for resource and booking operations
 */
class AffluencesApi(
    language: String = "it",
    httpClientConfig: HttpClientConfig<*>.() -> Unit = {}
) : AutoCloseable {

    companion object {
        /**
         * HTTP timeout in milliseconds.
         */
        private const val TIMEOUT = 30_000L

        /**
         * Browser-like user agent sent with every request.
         *
         * The app API runs device detection on the user agent and rejects requests from
         * unrecognized clients with an "Invalid device" error, so a browser identity is required.
         */
        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36"
    }

    /**
     * Json instance.
     */
    private val json: Json = Json {
        coerceInputValues = true
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    /**
     * Shared HTTP client for all API requests.
     *
     * Configured with:
     * - Extended timeouts
     * - JSON content negotiation
     * - A default `Accept-Language` header controlling the localization of server strings
     */
    private val client = HttpClient {
        httpClientConfig()
        install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT
            connectTimeoutMillis = TIMEOUT
            socketTimeoutMillis = TIMEOUT
        }
        install(ContentNegotiation) {
            json(json, contentType = ContentType.Application.Json)
        }
        install(DefaultRequest) {
            header(HttpHeaders.AcceptLanguage, language)
        }
        install(UserAgent) {
            agent = USER_AGENT
        }
        followRedirects = true
    }

    /**
     * API for site page operations.
     *
     * Provides access to:
     * - Site information (names, contacts, location, categories, pictures)
     * - Real-time occupancy and per-day forecasts
     * - Weekly opening hours
     * - Practical details and related sites
     */
    val sites = AffluencesSitesApi(client, json)

    /**
     * API for search and discovery operations.
     *
     * Provides access to:
     * - Free-text, filtered, and map-bounded site search
     * - Search filter metadata
     * - Home page suggestions
     */
    val search = AffluencesSearchApi(client, json)

    /**
     * API for resource and booking operations.
     *
     * Provides access to:
     * - Bookable resource types and their availability
     * - Booking constraints (agreements, email restrictions, single sign-on)
     * - The full reservation lifecycle: create, validate, confirm, cancel
     */
    val reservations = AffluencesReservationApi(client, json)

    /**
     * Closes the underlying HTTP client and releases resources.
     *
     * After calling this method, the API instance should not be used.
     * All API calls will fail after closing.
     *
     * This method is idempotent - calling it multiple times has no effect.
     */
    override fun close() {
        client.close()
    }
}
