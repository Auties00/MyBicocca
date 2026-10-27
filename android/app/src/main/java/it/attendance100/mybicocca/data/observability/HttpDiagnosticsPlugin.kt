package it.attendance100.mybicocca.data.observability

import android.util.Log
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.encodedPath

private const val TAG = "HttpDiagnostics"

/**
 * Ktor client plugin that writes a logcat line for every response an anti-bot layer in front of
 * the university servers produced, so a sign-in that fails for a reason the app cannot see
 * (a Cloudflare challenge or a country block) can be told apart from bad credentials or a dead
 * network. Nothing else is logged: ordinary 4xx/5xx replies from the origin stay quiet.
 *
 * Only the method, host, path, status and a few response headers are written. The query string
 * (Moodle puts the wstoken there), request headers, cookies and bodies are never touched.
 */
val HttpDiagnostics = createClientPlugin("HttpDiagnostics") {
    onResponse { response ->
        val line = describeBlockedResponse(
            method = response.call.request.method,
            url = response.call.request.url,
            status = response.status,
            headers = response.headers,
        ) ?: return@onResponse
        Log.w(TAG, line)
    }
}

/**
 * One-line description of [status]/[headers] when they look like an anti-bot or WAF response,
 * or null for anything else. Cloudflare marks a challenge it served with `cf-mitigated`; a plain
 * block carries no such header, so a 403/429/503 from a `cloudflare` server counts as well.
 */
internal fun describeBlockedResponse(
    method: HttpMethod,
    url: Url,
    status: HttpStatusCode,
    headers: Headers,
): String? {
    val mitigated = headers["cf-mitigated"]
    val server = headers[HttpHeaders.Server]
    val viaCloudflare = server.equals("cloudflare", ignoreCase = true)
    val blocked = status.value in BLOCK_STATUSES
    if (mitigated == null && !(viaCloudflare && blocked)) return null

    return buildString {
        append(method.value).append(' ')
        append(url.protocol.name).append("://").append(url.host).append(url.encodedPath)
        append(" -> ").append(status.value)
        mitigated?.let { append(" cf-mitigated=").append(it) }
        server?.let { append(" server=").append(it) }
        headers["cf-ray"]?.let { append(" cf-ray=").append(it) }
        headers[HttpHeaders.ContentType]?.let { append(" content-type=").append(it) }
    }
}

private val BLOCK_STATUSES = setOf(403, 429, 503)
