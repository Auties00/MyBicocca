package it.attendance100.mybicocca.data.auth

import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.Cookie
import it.attendance100.mybicocca.data.remote.esse3.scraper.api.Esse3Api as Esse3LegacyApi
import it.attendance100.mybicocca.data.remote.esse3.scraper.api.Esse3AuthApi as Esse3LegacyAuthApi
import javax.inject.Inject
import javax.inject.Singleton

// Builds the legacy Esse3 web-scrape client (the `esse3-scraper` module's Esse3Api,
// aliased to Esse3LegacyApi here to avoid colliding with the REST esse3 module's
// same-named Esse3Api). This is the cookie-session counterpart to ElearningApiFactory:
// `login` performs the Shibboleth SAML round-trip and yields the SP session cookies;
// `create` wraps those cookies in an authenticated scrape client.
@Singleton
class Esse3LegacyApiFactory @Inject constructor() {

    // Runs the full SAML web-login on a throwaway client and returns the
    // s3w.si.unimib.it session cookies (JSESSIONID + _shibsession_*). Throws
    // AuthenticationException on bad credentials / a broken chain.
    suspend fun login(username: String, password: String): List<Cookie> =
        Esse3LegacyAuthApi.login(username, password) {
            install(HttpTimeout) {
                connectTimeoutMillis = TIMEOUT_MS
                socketTimeoutMillis = TIMEOUT_MS
                requestTimeoutMillis = TIMEOUT_MS
            }
        }

    fun create(sessionCookies: List<Cookie>): Esse3LegacyApi =
        Esse3LegacyApi(sessionCookies = sessionCookies) {
            install(HttpTimeout) {
                connectTimeoutMillis = TIMEOUT_MS
                socketTimeoutMillis = TIMEOUT_MS
                requestTimeoutMillis = TIMEOUT_MS
            }
        }

    private companion object {
        const val TIMEOUT_MS = 30_000L
    }
}
