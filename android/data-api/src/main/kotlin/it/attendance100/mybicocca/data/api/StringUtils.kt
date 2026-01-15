package it.attendance100.mybicocca.data.api

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
         replace("\u200b", "")  // Remove zero-width spaces
        .replace("\u00a0", " ") // Normalize non-breaking spaces
        .trim()