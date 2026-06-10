package it.attendance100.mybicocca.core.search

import java.text.Normalizer

/**
 * Folds case and diacritics (à è é ì ò ù...) and collapses whitespace so "Probabilità"
 * matches "probabilita". The canonical normalization every search component applies before
 * comparing strings.
 */
fun normalizeForSearch(s: String): String {
    val decomposed = Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD)
    return CombiningMarks.replace(decomposed, "").replace(WhitespaceRun, " ").trim()
}

/**
 * Normalizes via [normalizeForSearch] and splits on the search separator family, dropping
 * blanks.
 */
fun tokenizeForSearch(s: String): List<String> =
    normalizeForSearch(s).split(TokenSeparator).filter { it.isNotBlank() }

private val CombiningMarks = Regex("\\p{Mn}+")

private val WhitespaceRun = Regex("\\s+")

/**
 * Separator family shared with the subject-name labeler in the calendar mapper: whitespace,
 * slashes, dashes, apostrophes. The apostrophe split turns "l'ingegneria" into "l" +
 * "ingegneria" so the elided article falls into the stop-word filter instead of polluting
 * token matching.
 */
private val TokenSeparator = Regex("""[\s/'’–—-]+""")
