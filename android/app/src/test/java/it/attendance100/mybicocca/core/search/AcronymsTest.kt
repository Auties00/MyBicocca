package it.attendance100.mybicocca.core.search

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Behaviour of [acronymOf]: first-letter acronyms of the content words, with the Italian and
 * English function-word set dropped, and the two-content-word floor.
 */
class AcronymsTest {

    @Test
    fun `acronym of the worked example drops the conjunction`() {
        assertThat(acronymOf("algoritmi e strutture dati")).isEqualTo("asd")
    }

    @Test
    fun `acronym takes the first letter of each content word`() {
        assertThat(acronymOf("analisi matematica")).isEqualTo("am")
    }

    @Test
    fun `articles and prepositions are dropped before forming the acronym`() {
        assertThat(acronymOf("storia della filosofia")).isEqualTo("sf")
    }

    @Test
    fun `English function words are dropped`() {
        assertThat(acronymOf("introduction to the computer")).isEqualTo("ic")
    }

    @Test
    fun `a single content word is not an acronym`() {
        assertThat(acronymOf("matematica")).isNull()
    }

    @Test
    fun `a content word plus only stop-words is not an acronym`() {
        assertThat(acronymOf("storia della")).isNull()
    }

    @Test
    fun `empty input is not an acronym`() {
        assertThat(acronymOf("")).isNull()
    }

    @Test
    fun `only stop-words yields no acronym`() {
        assertThat(acronymOf("di e la")).isNull()
    }

    @Test
    fun `elided article token l is treated as a stop-word`() {
        assertThat(acronymOf("l ingegneria del software")).isEqualTo("is")
    }

    @Test
    fun `four content words yield a four-letter acronym`() {
        assertThat(acronymOf("fisica generale e applicata")).isEqualTo("fga")
    }

    @Test
    fun `stop-word set contains the elided article and core prepositions`() {
        assertThat(SearchStopWords).containsAtLeast("l", "di", "e", "della", "the", "and")
    }
}
