package it.attendance100.mybicocca.core.search

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Covers the two string-distance primitives behind the unified-search typo tier:
 * [damerauLevenshtein] (budget-capped optimal-string-alignment edit distance, `-1` on cutoff)
 * and [jaroWinkler] (prefix-weighted similarity in 0..1). Known character pairs are pinned to
 * exact values; transposition, length-difference and per-row cutoffs are exercised directly.
 */
class SimilarityTest {

    @Test
    fun `damerauLevenshtein returns 0 for identical strings`() {
        assertThat(damerauLevenshtein("analisi", "analisi", 2)).isEqualTo(0)
    }

    @Test
    fun `damerauLevenshtein returns 0 for equal strings even with a non-positive budget`() {
        assertThat(damerauLevenshtein("abc", "abc", 0)).isEqualTo(0)
        assertThat(damerauLevenshtein("abc", "abc", -1)).isEqualTo(0)
    }

    @Test
    fun `damerauLevenshtein returns -1 when budget is non-positive and strings differ`() {
        assertThat(damerauLevenshtein("abc", "abd", 0)).isEqualTo(-1)
    }

    @Test
    fun `damerauLevenshtein counts a single substitution as distance 1`() {
        assertThat(damerauLevenshtein("abc", "abd", 5)).isEqualTo(1)
    }

    @Test
    fun `damerauLevenshtein counts an adjacent transposition as a single edit`() {
        assertThat(damerauLevenshtein("hte", "the", 2)).isEqualTo(1)
    }

    @Test
    fun `damerauLevenshtein matches the classic kitten sitting distance of 3`() {
        assertThat(damerauLevenshtein("kitten", "sitting", 5)).isEqualTo(3)
    }

    @Test
    fun `damerauLevenshtein rejects via the length-difference pre-check`() {
        assertThat(damerauLevenshtein("abc", "abcdef", 2)).isEqualTo(-1)
    }

    @Test
    fun `damerauLevenshtein rejects via the per-row minimum cutoff`() {
        assertThat(damerauLevenshtein("dato", "gatto", 1)).isEqualTo(-1)
    }

    @Test
    fun `damerauLevenshtein returns the exact distance when it sits at the budget`() {
        assertThat(damerauLevenshtein("dato", "gatto", 2)).isEqualTo(2)
    }

    @Test
    fun `damerauLevenshtein returns -1 when the true distance exceeds the budget`() {
        assertThat(damerauLevenshtein("analisi", "geometria", 2)).isEqualTo(-1)
    }

    @Test
    fun `jaroWinkler is 1 for identical strings`() {
        assertThat(jaroWinkler("matematica", "matematica")).isEqualTo(1.0)
    }

    @Test
    fun `jaroWinkler is 0 when either string is empty`() {
        assertThat(jaroWinkler("", "x")).isEqualTo(0.0)
        assertThat(jaroWinkler("x", "")).isEqualTo(0.0)
    }

    @Test
    fun `jaroWinkler is 0 when no characters match`() {
        assertThat(jaroWinkler("abc", "xyz")).isEqualTo(0.0)
    }

    @Test
    fun `jaroWinkler matches the classic martha marhta value`() {
        assertThat(jaroWinkler("martha", "marhta")).isWithin(1e-4).of(0.9611)
    }

    @Test
    fun `jaroWinkler rewards a shared leading prefix`() {
        val sharedPrefix = jaroWinkler("analisi", "analist")
        val noSharedPrefix = jaroWinkler("xnalisi", "analist")

        assertThat(sharedPrefix).isGreaterThan(noSharedPrefix)
    }

    @Test
    fun `jaroWinkler stays within the unit interval`() {
        val score = jaroWinkler("informatica", "informatika")

        assertThat(score).isAtLeast(0.0)
        assertThat(score).isAtMost(1.0)
    }
}
