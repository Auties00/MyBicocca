package it.attendance100.mybicocca.data.common.util

import io.ktor.http.HttpStatusCode

/**
 * Checks if a given status code is a redirect code according to HTTP standards.
 */
fun HttpStatusCode.isRedirect(): Boolean = when (value) {
    HttpStatusCode.MovedPermanently.value,
    HttpStatusCode.Found.value,
    HttpStatusCode.TemporaryRedirect.value,
    HttpStatusCode.PermanentRedirect.value,
    HttpStatusCode.SeeOther.value -> true
    else -> false
}