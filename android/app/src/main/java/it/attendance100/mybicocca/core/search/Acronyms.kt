package it.attendance100.mybicocca.core.search

/**
 * First letters of the content words: "algoritmi e strutture dati" -> "asd". Input must be
 * pre-normalized via [normalizeForSearch]. Null when fewer than two content words remain —
 * a single-word title doesn't form a meaningful acronym.
 */
fun acronymOf(normalized: String): String? {
    val content = normalized.split(' ').filter { it.isNotBlank() && it !in SearchStopWords }
    if (content.size < 2) return null
    return buildString { content.forEach { append(it.first()) } }
}

/**
 * Italian and English function words that carry no weight in acronyms or alias matching.
 * Mirrors the labeler's set in data/mapper/calendar/SubjectName.kt, which can't be imported
 * here because core stays free of data-layer dependencies.
 */
val SearchStopWords = setOf(
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
