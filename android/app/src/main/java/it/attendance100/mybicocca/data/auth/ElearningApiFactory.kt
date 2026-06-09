package it.attendance100.mybicocca.data.auth

import io.ktor.client.plugins.HttpTimeout
import it.attendance100.mybicocca.data.observability.HttpMetrics
import it.attendance100.mybicocca.data.remote.elearning.api.ElearningApi
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElearningApiFactory @Inject constructor() {

    fun create(moodleSessionCookie: String? = null): ElearningApi =
        ElearningApi(
            moodleSessionCookie = moodleSessionCookie,
            language = moodleLanguage(),
        ) {
            install(HttpTimeout) {
                connectTimeoutMillis = HTTP_TIMEOUT_MS
                socketTimeoutMillis = HTTP_TIMEOUT_MS
                requestTimeoutMillis = HTTP_TIMEOUT_MS
            }
            install(HttpMetrics)
        }

    // Maps the app locale onto the langpacks elearning.unimib.it actually ships;
    // anything that isn't Italian falls back to English.
    private fun moodleLanguage(): String =
        if (Locale.getDefault().language == "it") "it" else "en"

    private companion object {
        const val HTTP_TIMEOUT_MS = 30_000L
    }
}
