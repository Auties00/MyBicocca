package it.attendance100.mybicocca.core.search

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Covers the fzf-style abbreviation matcher [subsequenceMatch]: the documented worked
 * examples ("anmat" -> "ANalisi MATematica", "fisgen" -> "FISica GENerale"), the
 * minimum-query-length and plain-subsequence rejects, the word-start anchor filter, the
 * normalized-score floor, and the highlight positions contract.
 */
class SubsequenceTest {

    @Test
    fun `matches the anmat worked example from the KDoc`() {
        val match = subsequenceMatch("anmat", "analisi matematica")

        assertThat(match).isNotNull()
        assertThat(match!!.normalizedScore).isAtLeast(0.5)
        assertThat(match.normalizedScore).isAtMost(1.0)
    }

    @Test
    fun `matches the fisgen worked example from the KDoc`() {
        val match = subsequenceMatch("fisgen", "fisica generale")

        assertThat(match).isNotNull()
        assertThat(match!!.normalizedScore).isAtLeast(0.5)
    }

    @Test
    fun `returns positions that map one-to-one onto the matched characters`() {
        val match = subsequenceMatch("anmat", "analisi matematica")!!

        assertThat(match.positions).hasSize(5)
        match.positions.forEach { index ->
            assertThat(index).isAtLeast(0)
            assertThat(index).isLessThan("analisi matematica".length)
        }
        val matched = match.positions.map { "analisi matematica"[it] }.joinToString("")
        assertThat(matched).isEqualTo("anmat")
    }

    @Test
    fun `positions are strictly ascending`() {
        val positions = subsequenceMatch("fisgen", "fisica generale")!!.positions

        val sorted = positions.sorted()
        assertThat(positions).isEqualTo(sorted)
        assertThat(positions.toSet()).hasSize(positions.size)
    }

    @Test
    fun `rejects queries shorter than the minimum length`() {
        assertThat(subsequenceMatch("an", "analisi")).isNull()
        assertThat(subsequenceMatch("ana", "analisi matematica")).isNull()
    }

    @Test
    fun `rejects when the text is shorter than the query`() {
        assertThat(subsequenceMatch("anmat", "an")).isNull()
    }

    @Test
    fun `rejects when the query is not an in-order subsequence`() {
        assertThat(subsequenceMatch("xyzq", "analisi matematica")).isNull()
    }

    @Test
    fun `rejects a floating mid-word subsequence that does not anchor on a word start`() {
        assertThat(subsequenceMatch("nlsi", "analisi")).isNull()
    }

    @Test
    fun `anchors the first matched character on a word boundary`() {
        val match = subsequenceMatch("anmat", "analisi matematica")!!

        assertThat(match.positions.first()).isEqualTo(0)
    }
}
