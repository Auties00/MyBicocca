package it.attendance100.mybicocca.core.search

// A candidate to score: the primary display text plus optional aliases (Italian synonyms,
// codes, alternative names) that should also hit.
data class MatchInput(
    val text: String,
    val aliases: List<String> = emptyList(),
)

// Tiered relevance scoring. Higher tiers win outright; only the fuzzy tier carries a
// continuous component. Null means "no match" — callers drop the candidate.
object SearchMatcher {

    // Per-token Jaro-Winkler floor for the typo-tolerance tier.
    private const val FuzzyThreshold = 0.86

    fun score(query: String, candidate: MatchInput): Double? {
        val queryNorm = normalizeForSearch(query)
        if (queryNorm.isEmpty()) return null
        val queryTokens = tokenizeForSearch(query)
        if (queryTokens.isEmpty()) return null

        val textNorm = normalizeForSearch(candidate.text)
        if (textNorm == queryNorm) return 1.0

        val textTokens = tokenizeForSearch(candidate.text)
        if (allTokensArePrefixes(queryTokens, textTokens)) return 0.85

        // "asd" -> "algoritmi e strutture dati". Spaces dropped so "a s d" also hits.
        val acronym = acronymOf(textNorm)
        if (acronym != null && queryNorm.replace(" ", "") == acronym) return 0.8

        for (alias in candidate.aliases) {
            if (normalizeForSearch(alias) == queryNorm) return 0.75
            if (allTokensArePrefixes(queryTokens, tokenizeForSearch(alias))) return 0.7
        }

        return fuzzyScore(queryTokens, textTokens)
    }

    private fun allTokensArePrefixes(queryTokens: List<String>, textTokens: List<String>): Boolean =
        textTokens.isNotEmpty() && queryTokens.all { q -> textTokens.any { it.startsWith(q) } }

    // Every query token must fuzzily land on some candidate token, else multi-word queries
    // would match on a single stray word. Tokens under 4 chars skip this tier entirely —
    // Jaro-Winkler on 2-3 chars is mostly noise.
    private fun fuzzyScore(queryTokens: List<String>, textTokens: List<String>): Double? {
        if (textTokens.isEmpty()) return null
        var total = 0.0
        for (q in queryTokens) {
            if (q.length < 4) return null
            val best = textTokens.maxOf { jaroWinkler(q, it) }
            if (best < FuzzyThreshold) return null
            total += best
        }
        val average = total / queryTokens.size
        return 0.5 + (average - FuzzyThreshold) / (1 - FuzzyThreshold) * 0.2
    }
}
