package it.attendance100.mybicocca.core.search

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Behaviour of [stemItalian] (Lucene light-stemmer rules, six-char floor, the 'i'/'h'
 * double-drop) and [degeminate] (collapsing repeated characters).
 */
class ItalianFoldingTest {

    @Test
    fun `tokens shorter than six chars are returned unchanged`() {
        assertThat(stemItalian("casa")).isEqualTo("casa")
        assertThat(stemItalian("libri")).isEqualTo("libri")
    }

    @Test
    fun `exactly five chars is below the floor and unchanged`() {
        assertThat(stemItalian("gatto")).isEqualTo("gatto")
    }

    @Test
    fun `feminine singular and plural collapse onto the same stem`() {
        assertThat(stemItalian("matematica")).isEqualTo("matematic")
        assertThat(stemItalian("matematiche")).isEqualTo("matematic")
    }

    @Test
    fun `trailing e after h drops both characters`() {
        assertThat(stemItalian("matematiche")).isEqualTo("matematic")
    }

    @Test
    fun `trailing e after i drops both characters`() {
        assertThat(stemItalian("calorie")).isEqualTo("calor")
    }

    @Test
    fun `trailing e after a normal consonant drops only the e`() {
        assertThat(stemItalian("studente")).isEqualTo("student")
    }

    @Test
    fun `trailing i after h drops both characters`() {
        assertThat(stemItalian("antichi")).isEqualTo("antic")
    }

    @Test
    fun `trailing i after i drops both characters`() {
        assertThat(stemItalian("studii")).isEqualTo("stud")
    }

    @Test
    fun `trailing i after a normal consonant drops only the i`() {
        assertThat(stemItalian("algoritmi")).isEqualTo("algoritm")
    }

    @Test
    fun `trailing a after i drops both characters`() {
        assertThat(stemItalian("farmacia")).isEqualTo("farmac")
    }

    @Test
    fun `trailing a after a normal consonant drops only the a`() {
        assertThat(stemItalian("probabilita")).isEqualTo("probabilit")
    }

    @Test
    fun `trailing o after i drops both characters`() {
        assertThat(stemItalian("esercizio")).isEqualTo("eserciz")
    }

    @Test
    fun `trailing o after a normal consonant drops only the o`() {
        assertThat(stemItalian("calcolo")).isEqualTo("calcol")
    }

    @Test
    fun `a token ending in a consonant is left untouched`() {
        assertThat(stemItalian("computer")).isEqualTo("computer")
    }

    @Test
    fun `a token ending in u is left untouched`() {
        assertThat(stemItalian("tribu")).isEqualTo("tribu")
    }

    @Test
    fun `degeminate collapses doubled consonants`() {
        assertThat(degeminate("soprattutto")).isEqualTo("sopratuto")
    }

    @Test
    fun `degeminate collapses doubled letters anywhere`() {
        assertThat(degeminate("mamma")).isEqualTo("mama")
    }

    @Test
    fun `degeminate leaves a string with no repeats unchanged`() {
        assertThat(degeminate("casa")).isEqualTo("casa")
    }

    @Test
    fun `degeminate is idempotent`() {
        val once = degeminate("rette")
        assertThat(degeminate(once)).isEqualTo(once)
    }

    @Test
    fun `degeminate of a single char returns it unchanged`() {
        assertThat(degeminate("a")).isEqualTo("a")
    }

    @Test
    fun `degeminate of empty string returns empty`() {
        assertThat(degeminate("")).isEqualTo("")
    }

    @Test
    fun `degeminate collapses a long run to a single char`() {
        assertThat(degeminate("aaaa")).isEqualTo("a")
    }

    @Test
    fun `degeminated misspelling equals degeminated correct form`() {
        assertThat(degeminate("sopratutto")).isEqualTo(degeminate("soprattutto"))
    }
}
