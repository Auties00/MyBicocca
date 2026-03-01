package it.attendance100.mybicocca.data.common.util

import io.ktor.http.*
import java.net.URLEncoder.encode

/**
 * Builds a full URL from a path and query parameters.
 *
 * @param baseUrl The request base url
 * @param path The request path (can be relative or absolute)
 * @param queryParams Query parameters to append
 * @return The full URL string
 */
fun buildUrl(baseUrl: String, path: String, queryParams: Map<String, Any> = emptyMap()): String {
    val basePath = when {
        path.startsWith("http") -> path
        path.startsWith("/") -> "$baseUrl$path"
        else -> "$baseUrl/$path"
    }
    return if (queryParams.isEmpty()) {
        basePath
    } else {
        val query = queryParams.asSequence().flatMap { (key, value) ->
            when (value) {
                is Collection<*> -> value.map { "$key=${encode(it.toString(), Charsets.UTF_8)}" }
                else -> listOf("$key=${encode(value.toString(), Charsets.UTF_8)}")
            }
        }.joinToString("&")
        if (basePath.contains("?")) {
            "$basePath&$query"
        } else {
            "$basePath?$query"
        }
    }
}

/**
 * Extracts a query parameter value from a URL, path, or query string.
 *
 * This function handles multiple input formats:
 * - Full URLs: `https://example.com/path?param=value`
 * - Paths with query strings: `/path?param=value`
 * - Query strings only: `param=value`
 *
 * @param input The URL, path, or query string to extract from
 * @param paramNames The parameter names to look for (checked in order)
 * @return The first matching parameter value, or null if none found
 */
fun extractQueryParam(input: String, vararg paramNames: String): String? {
    // Extract the query string portion
    val queryString = if (input.contains("?")) {
        input.substringAfter("?", "")
    } else {
        // Assume it's already a query string
        input
    }

    if (queryString.isBlank()) return null

    val params = parseQueryString(queryString)
    return paramNames.firstNotNullOfOrNull { params[it] }
}

/**
 * Extracts a query parameter value and converts it to the specified type.
 *
 * @param input The URL, path, or query string to extract from
 * @param paramNames The parameter names to look for (checked in order)
 * @param transform The function to convert the string value
 * @return The first matching parameter value converted by transform, or null if none found
 */
inline fun <T> extractQueryParamAs(
    input: String,
    vararg paramNames: String,
    transform: (String) -> T?
): T? = extractQueryParam(input, *paramNames)?.let(transform)

/**
 * Extracts a query parameter value as a Long.
 *
 * @param input The URL, path, or query string to extract from
 * @param paramNames The parameter names to look for (checked in order)
 * @return The first matching parameter value as Long, or null if none found or not a valid Long
 */
fun extractQueryParamAsLong(input: String, vararg paramNames: String): Long? =
    extractQueryParam(input, *paramNames)?.toLongOrNull()

/**
 * Extracts a query parameter value as an Int.
 *
 * @param input The URL, path, or query string to extract from
 * @param paramNames The parameter names to look for (checked in order)
 * @return The first matching parameter value as Int, or null if none found or not a valid Int
 */
fun extractQueryParamAsInt(input: String, vararg paramNames: String): Int? =
    extractQueryParam(input, *paramNames)?.toIntOrNull()
