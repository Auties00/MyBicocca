package it.attendance100.mybicocca.data.remote.common.util

/**
 * Cleans a string by removing zero-width spaces, normalizing non-breaking
 * spaces to regular spaces, and trimming leading/trailing whitespace.
 *
 * This is useful for processing text extracted from HTML elements, which
 * often contains invisible characters that can cause string comparison
 * and parsing issues.
 *
 * @return The cleaned string.
 */
fun String.cleanText(): String =
         replace("\u200b", "")
        .replace("\u00a0", " ")
        .trim()