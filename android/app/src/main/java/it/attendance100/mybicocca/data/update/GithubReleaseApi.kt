package it.attendance100.mybicocca.data.update

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import it.attendance100.mybicocca.data.observability.HttpMetrics
import it.attendance100.mybicocca.data.update.GithubReleaseApi.Companion.PAGE_SIZE
import kotlinx.serialization.json.Json

/**
 * Thin client over GitHub's public Releases REST API, unauthenticated. No token is shipped: a
 * leaked token would be a security problem and the per-IP rate limit (60 req/h) is anyway
 * untouched by one, while a once-a-day check sits far under it.
 */
class GithubReleaseApi(
    private val owner: String,
    private val repo: String,
) {

    private val client by lazy { buildClient() }

    private fun buildClient(): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = TIMEOUT_MS
            socketTimeoutMillis = TIMEOUT_MS
            requestTimeoutMillis = TIMEOUT_MS
        }
        install(HttpMetrics)
        defaultRequest {
            // GitHub rejects requests with no `User-Agent`, so one is always sent, along with the
            // documented `Accept` and API-version * headers.
            headers.append("Accept", "application/vnd.github+json")
            headers.append("X-GitHub-Api-Version", "2022-11-28")
            headers.append("User-Agent", USER_AGENT)
        }
    }

    /**
     * The latest published, non-draft, non-pre-release release, or null when the repository has
     * no such release yet (GitHub answers 404 in that case, which is a normal "nothing to
     * compare against" state rather than an error).
     */
    suspend fun getLatestRelease(): GithubReleaseDto? {
        val response: HttpResponse = client.get("$API_BASE/repos/$owner/$repo/releases/latest")
        return when {
            response.status == HttpStatusCode.NotFound -> null
            response.status.isSuccess() -> response.body<GithubReleaseDto>()
            else -> error("GitHub latest-release request failed: ${response.status}")
        }
    }

    /** Up to [PAGE_SIZE] published releases, newest first, for the "What's New" list. */
    suspend fun getReleases(): List<GithubReleaseDto> {
        val response: HttpResponse =
            client.get("$API_BASE/repos/$owner/$repo/releases?per_page=$PAGE_SIZE")
        if (!response.status.isSuccess()) {
            error("GitHub releases request failed: ${response.status}")
        }
        return response.body<List<GithubReleaseDto>>()
    }

    private companion object {
        const val API_BASE = "https://api.github.com"
        const val USER_AGENT = "MyBicocca-Android"
        const val TIMEOUT_MS = 20_000L
        const val PAGE_SIZE = 30
    }
}
