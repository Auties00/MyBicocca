package it.attendance100.mybicocca.core.search

/**
 * A candidate to score against a search query.
 *
 * @property text The primary display text.
 * @property aliases Alternative strings that should also hit: Italian synonyms, codes,
 *   alternative names. Alias matches score below matches on [text].
 */
data class MatchInput(
    val text: String,
    val aliases: List<String> = emptyList(),
)

/**
 * Tiered relevance scorer at the heart of the unified search. Every candidate — action,
 * destination, course, event, room... — goes through [score]; the returned value drives the
 * global ranking, and null means "no match" so callers drop the candidate.
 *
 * Tier bands, strongest to weakest:
 * - exact normalized match: 1.0
 * - every query token a prefix (or light-stem equal) of a text token: 0.85
 * - acronym of the content words ("asd" -> "algoritmi e strutture dati"): 0.8
 * - exact alias match: 0.75; all-token-prefix alias match: 0.7
 * - in-order subsequence (abbreviations like "anmat"): 0.55..0.72
 * - typo correction under a bounded Damerau-Levenshtein budget: 0.5..0.7
 *
 * Higher tiers win outright; the subsequence and typo tiers carry a continuous component
 * and their bands overlap on purpose — a strong typo correction can outrank a weak
 * abbreviation.
 */
object SearchMatcher {

    /**
     * Scores [candidate] against [query], trying each tier in order and returning the first
     * hit. The acronym comparison drops spaces from the query so "a s d" hits like "asd".
     * When both the subsequence and typo tiers match, the larger score wins.
     */
    fun score(query: String, candidate: MatchInput): Double? {
        val queryNorm = normalizeForSearch(query)
        if (queryNorm.isEmpty()) return null
        val queryTokens = tokenizeForSearch(query)
        if (queryTokens.isEmpty()) return null

        val textNorm = normalizeForSearch(candidate.text)
        if (textNorm == queryNorm) return 1.0

        val textTokens = tokenizeForSearch(candidate.text)
        if (allTokensArePrefixes(queryTokens, textTokens)) return 0.85

        val acronym = acronymOf(textNorm)
        if (acronym != null && queryNorm.replace(" ", "") == acronym) return 0.8

        for (alias in candidate.aliases) {
            if (normalizeForSearch(alias) == queryNorm) return 0.75
            if (allTokensArePrefixes(queryTokens, tokenizeForSearch(alias))) return 0.7
        }

        val abbreviation = subsequenceScore(queryNorm, textNorm)
        val typo = typoScore(queryTokens, textTokens)
        return when {
            abbreviation == null -> typo
            typo == null -> abbreviation
            else -> maxOf(abbreviation, typo)
        }
    }

    /** True when every query token hits some text token via [tokenPrefixOrStem]. */
    private fun allTokensArePrefixes(queryTokens: List<String>, textTokens: List<String>): Boolean =
        textTokens.isNotEmpty() && queryTokens.all { q -> textTokens.any { tokenPrefixOrStem(q, it) } }

    /**
     * Stem-tolerant prefix test: a query token hits a text token when it is a prefix of it,
     * or — for queries of six or more characters — when their light Italian stems agree
     * ("matematiche" -> "matematica").
     */
    fun tokenPrefixOrStem(query: String, token: String): Boolean =
        token.startsWith(query) || (query.length >= 6 && stemItalian(query) == stemItalian(token))

    /**
     * Abbreviation tier: in-order subsequence over the whole text ("anmat" -> "ANalisi
     * MATematica"), single-token queries only — multi-word queries go through the token
     * tiers. The subsequence strength above its acceptance floor maps linearly onto the
     * 0.55..0.72 band.
     */
    private fun subsequenceScore(queryNorm: String, textNorm: String): Double? {
        if (' ' in queryNorm) return null
        val match = subsequenceMatch(queryNorm, textNorm) ?: return null
        val strength = (match.normalizedScore - 0.5) / 0.5
        return 0.55 + 0.17 * strength.coerceIn(0.0, 1.0)
    }

    /**
     * Typo tier. Admission is a Damerau-Levenshtein budget keyed on token length (the
     * Algolia/Meilisearch convention, see [typoBudget]) — an integer budget behaves
     * predictably across lengths where a single similarity floor doesn't. Two refinements:
     * equal degeminated forms ("sopratutto" ~ "soprattutto") count as a free hit, and a
     * first-letter mismatch costs one extra budget point in ranking, since users usually
     * get the first character right. Jaro-Winkler is the in-tier ranking signal.
     *
     * Function words in the query ("analisi di...") are excluded so they cannot block the
     * tier just by being under the budget threshold. Every remaining token must land on
     * some text token within budget; the per-token scores average into the 0.5..0.7 band.
     */
    private fun typoScore(queryTokens: List<String>, textTokens: List<String>): Double? {
        if (textTokens.isEmpty()) return null
        val significant = queryTokens.filterNot { it in SearchStopWords }.ifEmpty { queryTokens }
        var total = 0.0
        for (q in significant) {
            val budget = typoBudget(q.length)
            if (budget == 0) return null
            var best = -1.0
            for (t in textTokens) {
                val score = typoTokenScore(q, t, budget) ?: continue
                if (score > best) best = score
            }
            if (best < 0) return null
            total += best
        }
        val average = total / significant.size
        return 0.5 + average * 0.2
    }

    /**
     * Scores one query token against one text token within the typo tier: 0..1 relative to
     * the tier, or null when the token is out of budget or below [MinTypoSimilarity].
     */
    fun typoTokenScore(query: String, token: String, budget: Int): Double? {
        val degeminatedHit = degeminate(query) == degeminate(token)
        val distance = if (degeminatedHit) 0 else damerauLevenshtein(query, token, budget)
        if (distance < 0) return null
        val firstLetterPenalty = if (query.first() != token.first()) 1 else 0
        val similarity = jaroWinkler(query, token)
        if (similarity < MinTypoSimilarity) return null
        val rank = (distance + firstLetterPenalty).coerceAtMost(MaxRankedTypos)
        return (similarity - MinTypoSimilarity) / (1 - MinTypoSimilarity) *
            (1.0 - rank * RankStepDown)
    }

    /** Edit budget by token length: 0 typos under 4 chars, 1 up to 7, 2 from 8. */
    fun typoBudget(length: Int): Int = when {
        length < 4 -> 0
        length < 8 -> 1
        else -> 2
    }

    /**
     * Safety net under the edit budget: keeps 1-edit hits on very short tokens from
     * matching near-arbitrary strings ("dato" vs "gatto" style noise).
     */
    private const val MinTypoSimilarity = 0.72
    private const val MaxRankedTypos = 3
    private const val RankStepDown = 0.18
}
