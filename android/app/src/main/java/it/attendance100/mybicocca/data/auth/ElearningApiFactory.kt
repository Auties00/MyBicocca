package it.attendance100.mybicocca.data.auth

import io.ktor.client.plugins.HttpTimeout
import it.attendance100.mybicocca.data.remote.elearning.api.ElearningApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElearningApiFactory @Inject constructor() {

    fun create(moodleSessionCookie: String? = null): ElearningApi =
        ElearningApi(moodleSessionCookie = moodleSessionCookie) {
            install(HttpTimeout) {
                connectTimeoutMillis = HTTP_TIMEOUT_MS
                socketTimeoutMillis = HTTP_TIMEOUT_MS
                requestTimeoutMillis = HTTP_TIMEOUT_MS
            }
        }

    private companion object {
        const val HTTP_TIMEOUT_MS = 30_000L
    }
}
