package it.attendance100.mybicocca.data.observability

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.HttpMetric
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.contentLength
import io.ktor.http.contentType
import io.ktor.http.encodedPath

// Times every outgoing request and reports it to Firebase Performance Monitoring as an
// HttpMetric. Installed into each data-api client via its httpClientConfig lambda, so it
// covers Esse3 / Elearning / EasyStaff / Affluences uniformly without touching the libraries.
//
// Timing spans connect + send + time-to-first-byte (proceed() returns once response headers
// arrive, not after the body is fully read). For these small-JSON APIs that is effectively the
// request latency, which is what we want to track.
//
// Metrics never fail a real request: every Firebase call is guarded so a missing
// google-services.json or an uninitialized SDK degrades to a no-op.
val HttpMetrics = createClientPlugin("HttpMetrics") {
    on(Send) { request ->
        // Strip query + fragment: Moodle URLs carry wstoken in the query (must not leak to
        // Firebase) and per-call params would explode Firebase's 100-URL-pattern cap. Numeric
        // and UUID path segments are collapsed to {id}/{uuid} for the same cap reason — Esse3's
        // ID-heavy paths would otherwise mint a distinct pattern per resource.
        val path = normalizePath(request.url.encodedPath)
        val url = "${request.url.protocol.name}://${request.url.host}$path"
        val metric = runCatching {
            FirebasePerformance.getInstance()
                .newHttpMetric(url, firebaseMethod(request.method))
                .also(HttpMetric::start)
        }.getOrNull()

        try {
            val call = proceed(request)
            metric?.record(call.response)
            call
        } catch (cause: Throwable) {
            runCatching { metric?.stop() }
            throw cause
        }
    }
}

private fun HttpMetric.record(response: HttpResponse) {
    runCatching {
        setHttpResponseCode(response.status.value)
        response.contentLength()?.let(::setResponsePayloadSize)
        response.contentType()?.let { setResponseContentType(it.toString()) }
        stop()
    }
}

// Collapses high-cardinality path segments so Firebase aggregates by route, not by resource.
// Char-based on purpose: no top-level Regex val (ICU static-init crash risk in this codebase).
private fun normalizePath(encodedPath: String): String =
    encodedPath.split('/').joinToString("/") { segment ->
        when {
            segment.isEmpty() -> segment
            segment.all(Char::isDigit) -> "{id}"
            isUuid(segment) -> "{uuid}"
            else -> segment
        }
    }

// 8-4-4-4-12 hex, e.g. the Affluences library site UUIDs.
private fun isUuid(segment: String): Boolean {
    if (segment.length != 36) return false
    segment.forEachIndexed { i, c ->
        val hyphen = i == 8 || i == 13 || i == 18 || i == 23
        if (hyphen) {
            if (c != '-') return false
        } else if (!isHex(c)) {
            return false
        }
    }
    return true
}

private fun isHex(c: Char): Boolean =
    c in '0'..'9' || c in 'a'..'f' || c in 'A'..'F'

private fun firebaseMethod(method: HttpMethod): String = when (method) {
    HttpMethod.Get -> FirebasePerformance.HttpMethod.GET
    HttpMethod.Post -> FirebasePerformance.HttpMethod.POST
    HttpMethod.Put -> FirebasePerformance.HttpMethod.PUT
    HttpMethod.Delete -> FirebasePerformance.HttpMethod.DELETE
    HttpMethod.Head -> FirebasePerformance.HttpMethod.HEAD
    HttpMethod.Patch -> FirebasePerformance.HttpMethod.PATCH
    HttpMethod.Options -> FirebasePerformance.HttpMethod.OPTIONS
    else -> FirebasePerformance.HttpMethod.GET
}
