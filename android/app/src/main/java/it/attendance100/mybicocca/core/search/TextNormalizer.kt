package it.attendance100.mybicocca.core.search

import java.text.Normalizer

// Folds case and diacritics (à è é ì ò ù...) so "Probabilità" matches "probabilita".
fun normalizeForSearch(s: String): String {
    val decomposed = Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD)
    return CombiningMarks.replace(decomposed, "").replace(WhitespaceRun, " ").trim()
}

fun tokenizeForSearch(s: String): List<String> =
    normalizeForSearch(s).split(TokenSeparator).filter { it.isNotBlank() }

private val CombiningMarks = Regex("\\p{Mn}+")

private val WhitespaceRun = Regex("\\s+")

// Same separator family as SubjectName's labeler: whitespace, slashes, dashes, apostrophes.
// The apostrophe split turns "l'ingegneria" into ["l", "ingegneria"] so the elided article
// falls into the stop-word filter instead of polluting token matching.
private val TokenSeparator = Regex("""[\s/'’–—-]+""")
