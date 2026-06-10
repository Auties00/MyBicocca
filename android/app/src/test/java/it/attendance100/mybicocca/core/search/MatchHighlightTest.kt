package it.attendance100.mybicocca.core.search

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Span computation of [QueryHighlighter] / [matchHighlightRanges]: the index-preserving fold,
 * the tier order (direct substring, token prefix, acronym, subsequence, fuzzy), range merging,
 * and the null-as-no-match contract. Ranges are character offsets into the ORIGINAL text.
 */
class MatchHighlightTest {

    @Test
    fun `empty query never matches`() {
        assertThat(matchHighlightRanges("", "Analisi")).isNull()
    }

    @Test
    fun `blank query is empty after fold and never matches`() {
        assertThat(matchHighlightRanges("   ", "Analisi")).isNull()
    }

    @Test
    fun `empty text never matches`() {
        assertThat(matchHighlightRanges("anal", "")).isNull()
    }

    @Test
    fun `isEmpty reflects a blank query`() {
        assertThat(QueryHighlighter("   ").isEmpty).isTrue()
        assertThat(QueryHighlighter("anal").isEmpty).isFalse()
    }

    @Test
    fun `direct substring highlights exactly the matched run`() {
        assertThat(matchHighlightRanges("anal", "Analisi"))
            .containsExactly(0..3)
    }

    @Test
    fun `direct substring folds diacritics while preserving indexes`() {
        val ranges = matchHighlightRanges("probabilita", "Probabilità")
        assertThat(ranges).containsExactly(0..10)
    }

    @Test
    fun `direct substring can match mid-string`() {
        assertThat(matchHighlightRanges("mate", "Analisi Matematica"))
            .containsExactly(8..11)
    }

    @Test
    fun `direct substring takes precedence over later tiers`() {
        val ranges = matchHighlightRanges("ana", "Analisi Matematica")
        assertThat(ranges).containsExactly(0..2)
    }

    @Test
    fun `token prefix tier lights up only the typed prefix of each token`() {
        val ranges = matchHighlightRanges("algo strut", "Algoritmi Strutture")
        assertThat(ranges).containsExactly(0..3, 10..14).inOrder()
    }

    @Test
    fun `token prefix tier requires every query token to land`() {
        assertThat(matchHighlightRanges("algo zzz", "Algoritmi Strutture")).isNull()
    }

    @Test
    fun `token prefix tier matches across reordered tokens`() {
        val ranges = matchHighlightRanges("strut algo", "Algoritmi Strutture")
        assertThat(ranges).containsExactly(0..3, 10..14).inOrder()
    }

    @Test
    fun `stem prefix tier lights up the whole inflected token`() {
        val ranges = matchHighlightRanges("matematiche", "Matematica")
        assertThat(ranges).containsExactly(0..9)
    }

    @Test
    fun `acronym tier lights up the initial of each content word`() {
        val ranges = matchHighlightRanges("asd", "Algoritmi e Strutture Dati")
        assertThat(ranges).containsExactly(0..0, 12..12, 22..22).inOrder()
    }

    @Test
    fun `acronym tier tolerates spaced-out query letters`() {
        val ranges = matchHighlightRanges("a s d", "Algoritmi e Strutture Dati")
        assertThat(ranges).containsExactly(0..0, 12..12, 22..22).inOrder()
    }

    @Test
    fun `a partial acronym does not light up the initials`() {
        assertThat(matchHighlightRanges("as", "Algoritmi e Strutture Dati")).isNull()
    }

    @Test
    fun `subsequence tier matches an abbreviation and stays in bounds`() {
        val text = "Analisi Matematica"
        val ranges = matchHighlightRanges("anmat", text)
        assertThat(ranges).isNotNull()
        val flat = ranges!!.flatMap { it.toList() }
        assertThat(flat).isInStrictOrder()
        assertThat(flat.first()).isAtLeast(0)
        assertThat(flat.last()).isLessThan(text.length)
        assertThat(flat).hasSize("anmat".length)
    }

    @Test
    fun `a query that matches nothing returns null`() {
        assertThat(matchHighlightRanges("zzzzz", "Analisi Matematica")).isNull()
    }

    @Test
    fun `merged ranges are ascending and non-overlapping`() {
        val ranges = matchHighlightRanges("algo strut", "Algoritmi Strutture")!!
        var previousEnd = Int.MIN_VALUE
        for (range in ranges) {
            assertThat(range.first).isGreaterThan(previousEnd)
            previousEnd = range.last
        }
    }

    @Test
    fun `the convenience function agrees with a constructed highlighter`() {
        val viaClass = QueryHighlighter("anal").rangesIn("Analisi")
        val viaFunction = matchHighlightRanges("anal", "Analisi")
        assertThat(viaFunction).isEqualTo(viaClass)
    }

    @Test
    fun `one highlighter instance is reusable across texts`() {
        val highlighter = QueryHighlighter("anal")
        assertThat(highlighter.rangesIn("Analisi")).containsExactly(0..3)
        assertThat(highlighter.rangesIn("Banale analisi")).isNotNull()
        assertThat(highlighter.rangesIn("zzzzz")).isNull()
    }
}
