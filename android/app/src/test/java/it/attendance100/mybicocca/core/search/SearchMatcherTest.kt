package it.attendance100.mybicocca.core.search

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Covers the tiered relevance scorer [SearchMatcher.score] band by band — exact (1.0),
 * all-token-prefix (0.85), acronym (0.8), exact alias (0.75), all-prefix alias (0.7),
 * subsequence (0.55..0.72) and typo (0.5..0.7) — plus the empty-query null contract, the
 * `tokenPrefixOrStem` stem tolerance, the `typoBudget` length thresholds, the
 * degemination free hit, and the first-letter ranking penalty.
 */
class SearchMatcherTest {

    @Test
    fun `empty or blank query returns null`() {
        assertThat(SearchMatcher.score("", MatchInput("Analisi"))).isNull()
        assertThat(SearchMatcher.score("   ", MatchInput("Analisi"))).isNull()
    }

    @Test
    fun `exact normalized match scores 1`() {
        val score = SearchMatcher.score("analisi", MatchInput("Analisi"))

        assertThat(score).isEqualTo(1.0)
    }

    @Test
    fun `exact match folds case and diacritics`() {
        val score = SearchMatcher.score("probabilita", MatchInput("Probabilità"))

        assertThat(score).isEqualTo(1.0)
    }

    @Test
    fun `every query token a prefix of a text token scores 0,85`() {
        val score = SearchMatcher.score("anal mat", MatchInput("Analisi Matematica"))

        assertThat(score).isEqualTo(0.85)
    }

    @Test
    fun `a single token that is a prefix of one text token scores 0,85`() {
        val score = SearchMatcher.score("anal", MatchInput("Analisi Matematica"))

        assertThat(score).isEqualTo(0.85)
    }

    @Test
    fun `acronym of the content words scores 0,8`() {
        val score = SearchMatcher.score("asd", MatchInput("Algoritmi e Strutture Dati"))

        assertThat(score).isEqualTo(0.8)
    }

    @Test
    fun `exact alias match scores 0,75`() {
        val score = SearchMatcher.score(
            "info",
            MatchInput("Programmazione", aliases = listOf("info")),
        )

        assertThat(score).isEqualTo(0.75)
    }

    @Test
    fun `all-token-prefix alias match scores 0,7`() {
        val score = SearchMatcher.score(
            "info",
            MatchInput("Programmazione", aliases = listOf("informatica")),
        )

        assertThat(score).isEqualTo(0.7)
    }

    @Test
    fun `subsequence abbreviation scores in the 0,55 to 0,72 band`() {
        val score = SearchMatcher.score("anmat", MatchInput("Analisi Matematica"))

        assertThat(score).isNotNull()
        assertThat(score!!).isAtLeast(0.55)
        assertThat(score).isAtMost(0.72)
    }

    @Test
    fun `typo correction scores in the 0,5 to 0,7 band`() {
        val score = SearchMatcher.score("analisy", MatchInput("Analisi"))

        assertThat(score).isNotNull()
        assertThat(score!!).isAtLeast(0.5)
        assertThat(score).isAtMost(0.7)
    }

    @Test
    fun `a query that matches nothing returns null`() {
        val score = SearchMatcher.score("geometria", MatchInput("Chimica"))

        assertThat(score).isNull()
    }

    @Test
    fun `higher tiers win outright over weaker ones`() {
        val exact = SearchMatcher.score("analisi", MatchInput("Analisi"))!!
        val prefix = SearchMatcher.score("anal", MatchInput("Analisi"))!!
        val typo = SearchMatcher.score("analisy", MatchInput("Analisi"))!!

        assertThat(exact).isGreaterThan(prefix)
        assertThat(prefix).isGreaterThan(typo)
    }

    @Test
    fun `tokenPrefixOrStem accepts a plain prefix`() {
        assertThat(SearchMatcher.tokenPrefixOrStem("anal", "analisi")).isTrue()
    }

    @Test
    fun `tokenPrefixOrStem accepts equal light Italian stems for long queries`() {
        assertThat(SearchMatcher.tokenPrefixOrStem("matematiche", "matematica")).isTrue()
    }

    @Test
    fun `tokenPrefixOrStem does not stem queries shorter than six characters`() {
        assertThat(SearchMatcher.tokenPrefixOrStem("rossa", "rosso")).isFalse()
    }

    @Test
    fun `tokenPrefixOrStem rejects unrelated tokens`() {
        assertThat(SearchMatcher.tokenPrefixOrStem("gatto", "cane")).isFalse()
    }

    @Test
    fun `typoBudget allows no typos under four characters`() {
        assertThat(SearchMatcher.typoBudget(0)).isEqualTo(0)
        assertThat(SearchMatcher.typoBudget(3)).isEqualTo(0)
    }

    @Test
    fun `typoBudget allows one typo from four to seven characters`() {
        assertThat(SearchMatcher.typoBudget(4)).isEqualTo(1)
        assertThat(SearchMatcher.typoBudget(7)).isEqualTo(1)
    }

    @Test
    fun `typoBudget allows two typos from eight characters`() {
        assertThat(SearchMatcher.typoBudget(8)).isEqualTo(2)
        assertThat(SearchMatcher.typoBudget(12)).isEqualTo(2)
    }

    @Test
    fun `typoTokenScore returns null for an out-of-budget pair`() {
        val score = SearchMatcher.typoTokenScore("dato", "gatto", 1)

        assertThat(score).isNull()
    }

    @Test
    fun `typoTokenScore scores an in-budget single substitution`() {
        val score = SearchMatcher.typoTokenScore("analisy", "analisi", 1)

        assertThat(score).isNotNull()
        assertThat(score!!).isAtLeast(0.0)
        assertThat(score).isAtMost(1.0)
    }

    @Test
    fun `typoTokenScore treats equal degeminated forms as a free hit even with a zero budget`() {
        val score = SearchMatcher.typoTokenScore("sopratutto", "soprattutto", 0)

        assertThat(score).isNotNull()
    }

    @Test
    fun `typoTokenScore penalizes a first-letter mismatch relative to a tail typo`() {
        val firstLetterWrong = SearchMatcher.typoTokenScore("xnalisi", "analisi", 2)!!
        val tailLetterWrong = SearchMatcher.typoTokenScore("analisx", "analisi", 2)!!

        assertThat(firstLetterWrong).isLessThan(tailLetterWrong)
    }
}
