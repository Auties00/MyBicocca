package it.attendance100.mybicocca.data.remote.esse3.scraper.api

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.http.*


/**
 * Main entry point for the Esse3 API.
 *
 * @param sessionCookies The session cookies obtained from authentication.
 * @param httpClientConfig Optional configuration block for the underlying HTTP client.
 * @see Esse3AuthApi for authentication operations
 */
class Esse3Api(
    sessionCookies: List<Cookie>,
    httpClientConfig: HttpClientConfig<*>.() -> Unit = {}
) : AutoCloseable {
    companion object {
        /**
         * HTTP timeout in milliseconds.
         */
        private const val TIMEOUT = 30_000L
    }

    /**
     * Shared HTTP client for all API requests.
     *
     * Configured with:
     * - Cookie storage for session management
     * - Extended timeouts for slow university servers
     */
    private val client = HttpClient {
        httpClientConfig()
        install(HttpCookies) {
            default {
                sessionCookies.forEach { cookie ->
                    cookie.domain?.let { domain ->
                        addCookie(domain, cookie)
                    }
                }
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT
            connectTimeoutMillis = TIMEOUT
            socketTimeoutMillis = TIMEOUT
        }
        followRedirects = true
    }

    /**
     * API for authentication operations.
     *
     * Handles Shibboleth SSO login flow and session management.
     */
    val auth: Esse3AuthApi = Esse3AuthApi(client)

    /**
     * API for the legacy-only student flows that have no REST counterpart and are
     * reachable solely by scraping the Struts (`.do`) pages.
     *
     * Provides access to:
     * - Autocertificazioni / certificati (self-declaration PDFs)
     */
    val legacy: Esse3LegacyApi = Esse3LegacyApi(client)

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
