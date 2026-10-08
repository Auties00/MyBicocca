package it.attendance100.mybicocca.core.observability

import java.util.concurrent.ConcurrentHashMap

/** Receives one unknown-value sighting, already reduced to what is safe to send. */
fun interface UnknownValueSink {
    fun report(field: String, value: String)
}

/**
 * Where a mapper says "the backend sent a value I have no entry for", at the point it falls back
 * to a catch-all such as `OTHER` or `Unknown`. Without it those fallbacks are silent and a missing
 * code is only ever found by a user noticing something looks off.
 *
 * Only for fields the backend fills from a fixed vocabulary; never pass free text or anything
 * about the person. As a backstop, nothing leaves this object raw: [sanitizeUnknownValue] turns
 * the value into a short code or a placeholder first. With no [sink] installed (unit tests, the
 * crash process) a report is a no-op.
 */
object UnknownValues {

    @Volatile
    var sink: UnknownValueSink? = null

    private val seen = ConcurrentHashMap.newKeySet<String>()

    /**
     * Reports [raw] as an unrecognized value of [field], once per process for each pair. A null
     * or blank [raw] is ignored unless [reportMissing]: most fields are optional, so their
     * absence is only worth knowing where the app expects the backend to always send one.
     */
    fun report(field: String, raw: String?, reportMissing: Boolean = false) {
        val sink = sink ?: return
        if (raw.isNullOrBlank() && !reportMissing) return
        val value = sanitizeUnknownValue(raw)
        if (seen.add("$field=$value")) sink.report(field, value)
    }

    internal fun resetForTest() = seen.clear()
}

private const val MAX_CODE_LENGTH = 20
private const val MAX_CODE_DIGITS = 2
private val CODE = Regex("[A-Za-z0-9_]{1,$MAX_CODE_LENGTH}")

/**
 * Keeps a raw backend value only if it has the shape of an enum code: one short token of
 * letters, digits and underscores, with at most two digits (`S1`, `h5pactivity`, `2`). It is then
 * upper-cased. Anything else is replaced by its length, never trimmed down to fit.
 *
 * The shape is chosen to turn away what an identifier looks like: fiscal codes, student and phone
 * numbers carry many digits, and emails, dotted usernames and names carry `@`, `.`, `-` or
 * spaces. It cannot tell a code from a single plain word, so it is a backstop and not the
 * guarantee: that is the callers only ever passing fields the backend fills from a fixed
 * vocabulary, never free text or anything about the person.
 */
internal fun sanitizeUnknownValue(raw: String?): String {
    val trimmed = raw?.trim() ?: return "<null>"
    return when {
        trimmed.isEmpty() -> "<blank>"
        !CODE.matches(trimmed) || trimmed.count(Char::isDigit) > MAX_CODE_DIGITS ->
            "<not-a-code:${trimmed.length}>"

        else -> trimmed.uppercase()
    }
}
