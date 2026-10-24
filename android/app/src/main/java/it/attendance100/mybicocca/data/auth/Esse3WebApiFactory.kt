package it.attendance100.mybicocca.data.auth

import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.cookies.addCookie
import io.ktor.http.Cookie
import it.attendance100.mybicocca.data.observability.HttpMetrics
import it.attendance100.mybicocca.data.remote.esse3.api.Esse3Api
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds Esse3 clients for the web portal's pages, such as the self-declarations of
 * [Esse3Api.certificates]: they send the portal session cookies [Esse3WebAuthenticator] mints
 * instead of the REST API's HTTP Basic credentials.
 */
@Singleton
class Esse3WebApiFactory @Inject constructor() {

    fun create(sessionCookies: List<Cookie>): Esse3Api = Esse3Api {
        install(HttpCookies) {
            default {
                sessionCookies.forEach { cookie -> cookie.domain?.let { addCookie(it, cookie) } }
            }
        }
        install(HttpTimeout) {
            connectTimeoutMillis = TIMEOUT_MS
            socketTimeoutMillis = TIMEOUT_MS
            requestTimeoutMillis = TIMEOUT_MS
        }
        install(HttpMetrics)
    }

    private companion object {
        const val TIMEOUT_MS = 30_000L
    }
}
