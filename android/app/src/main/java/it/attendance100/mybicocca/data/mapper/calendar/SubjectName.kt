package it.attendance100.mybicocca.data.mapper.calendar

/**
 * Canonical form of a course name for cross-system joins (trimmed, inner whitespace
 * collapsed, uppercased). Esse3, EasyStaff and the transcript spell the same course with
 * different casing and spacing; comparing normalized names is what links them.
 */
fun normalizeSubjectName(name: String): String =
    name.trim().replace("\\s+".toRegex(), " ").uppercase()

/**
 * Builds a compact label (3-5 chars typical) for narrow calendar cells, or null when the
 * name yields nothing usable.
 *
 * Tuned for Italian university course titles seen in EasyStaff feeds:
 * ```
 * "Algoritmi e Strutture Dati"         -> "ASD"   (conjunction dropped)
 * "Calcolo delle Probabilità"          -> "CP"    (articulated preposition dropped)
 * "Fondamenti di Informatica"          -> "FI"
 * "Analisi Matematica I"               -> "AM1"   (trailing Roman numeral -> digit)
 * "Programmazione 1"                   -> "Prog1" (single content word + digit)
 * "Statistica"                         -> "Stat"  (single word -> 4-char Title-cased prefix)
 * "Lingua Inglese B2"                  -> "LIB2"  (trailing alphanumeric code preserved)
 * "Analisi I (modulo A)"               -> "Anal1" (parenthetical stripped, Roman -> digit)
 * "Metodi Matematici per l'Ingegneria" -> "MMI"   (elided article split + dropped)
 * ```
 */
fun shortLabelFor(name: String): String? {
    val cleaned = name.replace(BracketRegex, " ").trim()
    if (cleaned.isBlank()) return null

    val tokens = cleaned.split(TokenSeparator)
        .map { it.trim(*PunctuationTrim) }
        .filter { it.isNotBlank() }
    if (tokens.isEmpty()) return null

    val trailingSuffix = tokens.last().asTrailingSuffix()
    val body = if (trailingSuffix != null) tokens.dropLast(1) else tokens
    val content = body.filter { it.lowercase() !in StopWords }

    val suffix = trailingSuffix.orEmpty()
    return when {
        content.isEmpty() -> suffix.ifBlank { (body.firstOrNull() ?: tokens.last()).titleCasePrefix(4) }
        content.size == 1 -> content.first().titleCasePrefix(4) + suffix
        else -> content.take(5).joinToString("") { it.first().uppercaseChar().toString() } + suffix
    }
}

private val BracketRegex = Regex("""\([^)]*\)|\[[^]]*]""")

/**
 * Splits on whitespace, slashes, dashes (incl. en/em), and apostrophes. The apostrophe split
 * is what turns "l'Ingegneria" into ["l", "Ingegneria"], so the elided article falls into
 * the stop-word filter instead of contributing a stray "L".
 */
private val TokenSeparator = Regex("""[\s/'’–—-]+""")

private val PunctuationTrim = charArrayOf('.', ',', ';', ':', '·', '"', '«', '»')

/**
 * Course numbering uses Roman numerals up to ~X in practice (Analisi I/II, Storia III, ...).
 * Matches uppercase only — lowercase "i" / "v" / "x" are Italian function words.
 */
private val RomanNumeralRegex = Regex("^(I|II|III|IV|V|VI|VII|VIII|IX|X)$")

private val RomanToDigit = mapOf(
    "I" to "1", "II" to "2", "III" to "3", "IV" to "4", "V" to "5",
    "VI" to "6", "VII" to "7", "VIII" to "8", "IX" to "9", "X" to "10",
)

/**
 * The token as a label suffix when it is one: a plain number, a Roman numeral (converted to
 * its digit form), or a short alphanumeric code like "B2" / "C1" / "1A" (CEFR levels, module
 * letters with year). Null for ordinary words.
 */
private fun String.asTrailingSuffix(): String? = when {
    isEmpty() -> null
    all { it.isDigit() } -> this
    this == uppercase() && RomanNumeralRegex.matches(this) -> RomanToDigit[this]
    length in 2..4 && any { it.isDigit() } && any { it.isLetter() } -> uppercase()
    else -> null
}

private fun String.titleCasePrefix(max: Int): String =
    take(max).lowercase().replaceFirstChar { it.uppercaseChar() }

/** Italian (plus common English) function words that should never carry weight in an acronym. */
private val StopWords = setOf(
    "il", "lo", "la", "i", "gli", "le", "l",
    "del", "dello", "della", "dei", "degli", "delle",
    "al", "allo", "alla", "ai", "agli", "alle",
    "dal", "dallo", "dalla", "dai", "dagli", "dalle",
    "nel", "nello", "nella", "nei", "negli", "nelle",
    "sul", "sullo", "sulla", "sui", "sugli", "sulle",
    "col", "coi",
    "di", "a", "ad", "da", "in", "con", "su", "per", "tra", "fra",
    "e", "ed", "o", "od",
    "un", "uno", "una",
    "che", "non",
    "of", "the", "and", "for", "on", "to", "an", "or", "by", "with",
)
