package it.attendance100.mybicocca.data.mapper.common

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Esse3 (e3rest) serves dates as "dd/MM/yyyy" strings, frequently with a trailing " HH:mm:ss"
 * time component (often a filler 00:00:00 or 23:59:59) and occasionally as an ISO-8601 local
 * date. These shared formatters and parse helpers replace the per-mapper copies; parsing is
 * tolerant by contract — blank or unparseable input yields null, never an exception.
 */

/** The canonical Esse3 date shape, also used to re-format parsed dates back to display strings. */
internal val Esse3DateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private val Esse3DateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

/** Tried in order: the canonical dd/MM/yyyy first, then the occasional ISO local date. */
private val Esse3DatePatterns = listOf(
    Esse3DateFormat,
    DateTimeFormatter.ISO_LOCAL_DATE,
)

/**
 * Parses the date part of an Esse3 "dd/MM/yyyy" value, stripping any time component after a
 * space or a 'T'. Returns null when the receiver is null, blank or unparseable.
 */
internal fun String?.parseEsse3Date(): LocalDate? {
    val datePart = this?.trim()?.takeIf { it.isNotEmpty() }
        ?.substringBefore('T')?.substringBefore(' ') ?: return null
    return runCatching { LocalDate.parse(datePart, Esse3DateFormat) }.getOrNull()
}

/**
 * Like [parseEsse3Date] but also accepts ISO local dates, which some Esse3 endpoints emit;
 * the patterns are tried in [Esse3DatePatterns] order. Returns null when nothing matches.
 */
internal fun String?.parseEsse3DateOrIso(): LocalDate? {
    val raw = this?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val datePart = raw.substringBefore('T').substringBefore(' ')
    for (formatter in Esse3DatePatterns) {
        runCatching { return LocalDate.parse(datePart, formatter) }
    }
    return null
}

/** Parses a full Esse3 "dd/MM/yyyy HH:mm:ss" value. Returns null when blank or unparseable. */
internal fun String?.parseEsse3DateTime(): LocalDateTime? =
    this?.trim()?.takeIf { it.isNotBlank() }
        ?.let { runCatching { LocalDateTime.parse(it, Esse3DateTimeFormat) }.getOrNull() }
