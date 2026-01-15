package it.attendance100.mybicocca.data.api

import io.ktor.http.*

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
