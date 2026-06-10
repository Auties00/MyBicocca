package it.attendance100.mybicocca.core.search

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Behaviour of [normalizeForSearch] and [tokenizeForSearch]: Italian diacritic folding,
 * case folding, whitespace collapsing, and the apostrophe/slash/dash separator family.
 */
class TextNormalizerTest {

    @Test
    fun `lowercases and strips diacritics`() {
        assertThat(normalizeForSearch("Probabilità")).isEqualTo("probabilita")
    }

    @Test
    fun `folds the full Italian accented vowel set`() {
        assertThat(normalizeForSearch("àèéìòù ÀÈÉÌÒÙ")).isEqualTo("aeeiou aeeiou")
    }

    @Test
    fun `collapses internal whitespace runs to a single space`() {
        assertThat(normalizeForSearch("analisi    matematica")).isEqualTo("analisi matematica")
    }

    @Test
    fun `collapses tabs and newlines as whitespace`() {
        assertThat(normalizeForSearch("a\t\nb")).isEqualTo("a b")
    }

    @Test
    fun `trims leading and trailing whitespace`() {
        assertThat(normalizeForSearch("   ciao  ")).isEqualTo("ciao")
    }

    @Test
    fun `empty input normalizes to empty`() {
        assertThat(normalizeForSearch("")).isEqualTo("")
    }

    @Test
    fun `blank input normalizes to empty after trim`() {
        assertThat(normalizeForSearch("   ")).isEqualTo("")
    }

    @Test
    fun `tokenizes on whitespace`() {
        assertThat(tokenizeForSearch("analisi matematica"))
            .containsExactly("analisi", "matematica")
            .inOrder()
    }

    @Test
    fun `tokenizes on slash dash and apostrophe`() {
        assertThat(tokenizeForSearch("a/b-c'd"))
            .containsExactly("a", "b", "c", "d")
            .inOrder()
    }

    @Test
    fun `splits the elided article off via the apostrophe separator`() {
        assertThat(tokenizeForSearch("l'ingegneria"))
            .containsExactly("l", "ingegneria")
            .inOrder()
    }

    @Test
    fun `splits on the typographic right single quote`() {
        assertThat(tokenizeForSearch("dell’universita"))
            .containsExactly("dell", "universita")
            .inOrder()
    }

    @Test
    fun `splits on en dash and em dash`() {
        assertThat(tokenizeForSearch("a–b—c"))
            .containsExactly("a", "b", "c")
            .inOrder()
    }

    @Test
    fun `drops blank tokens from repeated separators`() {
        assertThat(tokenizeForSearch("a -- b")).containsExactly("a", "b").inOrder()
    }

    @Test
    fun `tokenizing folds diacritics through normalization`() {
        assertThat(tokenizeForSearch("Università degli Studi"))
            .containsExactly("universita", "degli", "studi")
            .inOrder()
    }

    @Test
    fun `empty string yields no tokens`() {
        assertThat(tokenizeForSearch("")).isEmpty()
    }

    @Test
    fun `blank string yields no tokens`() {
        assertThat(tokenizeForSearch("   /// ")).isEmpty()
    }
}
