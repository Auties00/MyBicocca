package it.attendance100.mybicocca.core.search

import java.text.Normalizer

/**
 * Computes where a query matched inside a display string, as character ranges over the
 * original text, for marker-style highlighting in search result rows. It mirrors
 * [SearchMatcher]'s tiers — whole-query substring, token prefixes, acronym, subsequence,
 * per-token fuzzy — but tracks positions, which the scorer deliberately doesn't. A null
 * result means "no match", so the highlighter doubles as a filter predicate.
 *
 * Folding here is per-character (lowercase + first char of the NFD decomposition, see
 * [foldChar]) so every folded index maps 1:1 back onto the original string — the property
 * the whole file exists to preserve. Characters whose lowercase form expands (ß -> ss)
 * keep a single char instead; irrelevant for Italian.
 *
 * The folded query and its token spans are computed once at construction, so one instance
 * can be reused across all the texts of a result list.
 */
class QueryHighlighter(query: String) {

    private val foldedQuery = foldPreservingIndexes(query).trim().replace(WhitespaceRun, " ")
    private val queryTokens = tokenSpans(foldedQuery)

    val isEmpty: Boolean get() = foldedQuery.isEmpty()

    /**
     * Ranges of [text] to highlight, ascending and non-overlapping, or null when the query
     * doesn't match. Tiers are tried in order: whole-query substring (covers codes, single
     * words and exact phrases), token prefixes, acronym, subsequence, fuzzy.
     */
    fun rangesIn(text: String): List<IntRange>? {
        if (foldedQuery.isEmpty() || text.isEmpty()) return null
        val foldedText = foldPreservingIndexes(text)

        val direct = foldedText.indexOf(foldedQuery)
        if (direct >= 0) return listOf(direct until direct + foldedQuery.length)

        val textTokens = tokenSpans(foldedText)
        if (textTokens.isEmpty() || queryTokens.isEmpty()) return null

        prefixRanges(queryTokens, textTokens)?.let { return it }
        acronymRanges(foldedQuery, textTokens)?.let { return it }
        subsequenceRanges(foldedQuery, foldedText)?.let { return it }
        return fuzzyRanges(queryTokens, textTokens)
    }
}

/** One-shot convenience over [QueryHighlighter] for a single query/text pair. */
fun matchHighlightRanges(query: String, text: String): List<IntRange>? =
    QueryHighlighter(query).rangesIn(text)

private class TokenSpan(val start: Int, val text: String)

/**
 * Token-prefix tier: every query token must be a prefix of its own (distinct) text token,
 * or match it through the light Italian stem, mirroring the scorer. A prefix match lights
 * up just the matched prefix; a stem match lights up the whole token — the inflected tail
 * differs, so a sub-span would lie.
 */
private fun prefixRanges(queryTokens: List<TokenSpan>, textTokens: List<TokenSpan>): List<IntRange>? {
    val claimed = BooleanArray(textTokens.size)
    val ranges = ArrayList<IntRange>(queryTokens.size)
    for (query in queryTokens) {
        val index = textTokens.indices.firstOrNull { i ->
            !claimed[i] && SearchMatcher.tokenPrefixOrStem(query.text, textTokens[i].text)
        } ?: return null
        claimed[index] = true
        val token = textTokens[index]
        val highlightLength =
            if (token.text.startsWith(query.text)) query.text.length else token.text.length
        ranges += token.start until token.start + highlightLength
    }
    return ranges.merged()
}

/**
 * Abbreviation tier ("anmat"): exactly the characters the subsequence landed on light up.
 */
private fun subsequenceRanges(foldedQuery: String, foldedText: String): List<IntRange>? {
    val compactQuery = foldedQuery.replace(" ", "")
    val match = subsequenceMatch(compactQuery, foldedText) ?: return null
    return match.positions.map { it..it }.merged()
}

/**
 * Acronym tier: "asd" against "Algoritmi e Strutture Dati" lights up the initial of each
 * content word.
 */
private fun acronymRanges(foldedQuery: String, textTokens: List<TokenSpan>): List<IntRange>? {
    val content = textTokens.filter { it.text !in SearchStopWords }
    if (content.size < 2) return null
    val acronym = buildString(content.size) { content.forEach { append(it.text.first()) } }
    if (foldedQuery.replace(" ", "") != acronym) return null
    return content.map { it.start..it.start }
}

/**
 * Fuzzy tier: each significant query token lands on a distinct text token within the same
 * typo budget the scorer uses. The whole landing token lights up — the match is
 * approximate, so highlighting a sub-span of it would lie about which characters hit.
 */
private fun fuzzyRanges(queryTokens: List<TokenSpan>, textTokens: List<TokenSpan>): List<IntRange>? {
    val claimed = BooleanArray(textTokens.size)
    val ranges = ArrayList<IntRange>(queryTokens.size)
    val significant = queryTokens.filterNot { it.text in SearchStopWords }.ifEmpty { queryTokens }
    for (query in significant) {
        val budget = SearchMatcher.typoBudget(query.text.length)
        if (budget == 0) return null
        var best = -1
        var bestScore = -1.0
        for (i in textTokens.indices) {
            if (claimed[i]) continue
            val score = SearchMatcher.typoTokenScore(query.text, textTokens[i].text, budget) ?: continue
            if (score > bestScore) {
                bestScore = score
                best = i
            }
        }
        if (best < 0) return null
        claimed[best] = true
        val token = textTokens[best]
        ranges += token.start until token.start + token.text.length
    }
    return ranges.merged()
}

/** Sorts by start and coalesces overlapping or adjacent ranges into maximal runs. */
private fun List<IntRange>.merged(): List<IntRange> {
    if (size <= 1) return this
    val sorted = sortedBy { it.first }
    val out = ArrayList<IntRange>(sorted.size)
    var current = sorted.first()
    for (range in sorted.drop(1)) {
        current = if (range.first <= current.last + 1) {
            current.first..maxOf(current.last, range.last)
        } else {
            out += current
            range
        }
    }
    out += current
    return out
}

/** Folds every character via [foldChar]; the output has the same length as the input. */
private fun foldPreservingIndexes(s: String): String {
    val builder = StringBuilder(s.length)
    for (c in s) builder.append(foldChar(c))
    return builder.toString()
}

/**
 * Lowercases and strips diacritics from a single character while staying one char long:
 * the first non-combining character of the NFD decomposition, falling back to the
 * lowercase char itself.
 */
private fun foldChar(c: Char): Char {
    val lower = c.lowercaseChar()
    if (lower.code < 0x80) return lower
    val decomposed = Normalizer.normalize(lower.toString(), Normalizer.Form.NFD)
    return decomposed.firstOrNull { Character.getType(it) != Character.NON_SPACING_MARK.toInt() } ?: lower
}

private val WhitespaceRun = Regex("\\s+")

/**
 * Same separator family as [tokenizeForSearch], expressed as a char predicate so
 * [tokenSpans] can scan manually and record each token's offset.
 */
private fun Char.isTokenSeparator(): Boolean =
    isWhitespace() || this == '/' || this == '\'' || this == '’' || this == '–' || this == '—' || this == '-'

/** Splits folded text into tokens that remember their start offset in the string. */
private fun tokenSpans(folded: String): List<TokenSpan> {
    val out = ArrayList<TokenSpan>()
    var i = 0
    while (i < folded.length) {
        if (folded[i].isTokenSeparator()) {
            i++
            continue
        }
        val start = i
        while (i < folded.length && !folded[i].isTokenSeparator()) i++
        out += TokenSpan(start, folded.substring(start, i))
    }
    return out
}
