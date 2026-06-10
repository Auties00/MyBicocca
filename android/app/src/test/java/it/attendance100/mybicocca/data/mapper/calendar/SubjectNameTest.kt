package it.attendance100.mybicocca.data.mapper.calendar

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Covers [normalizeSubjectName] (trim / inner-whitespace collapse / uppercase) and
 * [shortLabelFor], whose KDoc carries a worked-example table — every row of that table is
 * asserted here, plus the documented sentinels (blank input, parenthetical stripping,
 * stop-word dropping, Roman-numeral and alphanumeric-code suffixes).
 */
class SubjectNameTest {

    @Test
    fun `normalize trims surrounding whitespace`() {
        assertThat(normalizeSubjectName("  Analisi  ")).isEqualTo("ANALISI")
    }

    @Test
    fun `normalize collapses inner whitespace runs to single spaces`() {
        assertThat(normalizeSubjectName("Calcolo   delle    Probabilita"))
            .isEqualTo("CALCOLO DELLE PROBABILITA")
    }

    @Test
    fun `normalize collapses tabs and newlines as whitespace`() {
        assertThat(normalizeSubjectName("Analisi\tMatematica\nI"))
            .isEqualTo("ANALISI MATEMATICA I")
    }

    @Test
    fun `normalize uppercases the whole name`() {
        assertThat(normalizeSubjectName("Fondamenti di Informatica"))
            .isEqualTo("FONDAMENTI DI INFORMATICA")
    }

    @Test
    fun `normalize of empty string is empty`() {
        assertThat(normalizeSubjectName("")).isEqualTo("")
    }

    @Test
    fun `normalize of whitespace-only string is empty`() {
        assertThat(normalizeSubjectName("   \t \n ")).isEqualTo("")
    }

    @Test
    fun `two spellings of the same course normalize to the same key`() {
        assertThat(normalizeSubjectName("  algoritmi   e Strutture dati "))
            .isEqualTo(normalizeSubjectName("Algoritmi e Strutture Dati"))
    }

    @Test
    fun `shortLabel - Algoritmi e Strutture Dati drops the conjunction`() {
        assertThat(shortLabelFor("Algoritmi e Strutture Dati")).isEqualTo("ASD")
    }

    @Test
    fun `shortLabel - Calcolo delle Probabilita drops the articulated preposition`() {
        assertThat(shortLabelFor("Calcolo delle Probabilità")).isEqualTo("CP")
    }

    @Test
    fun `shortLabel - Fondamenti di Informatica`() {
        assertThat(shortLabelFor("Fondamenti di Informatica")).isEqualTo("FI")
    }

    @Test
    fun `shortLabel - Analisi Matematica I maps trailing Roman numeral to digit`() {
        assertThat(shortLabelFor("Analisi Matematica I")).isEqualTo("AM1")
    }

    @Test
    fun `shortLabel - Programmazione 1 is single content word plus digit`() {
        assertThat(shortLabelFor("Programmazione 1")).isEqualTo("Prog1")
    }

    @Test
    fun `shortLabel - Statistica is a 4-char title-cased prefix`() {
        assertThat(shortLabelFor("Statistica")).isEqualTo("Stat")
    }

    @Test
    fun `shortLabel - Lingua Inglese B2 preserves the trailing alphanumeric code`() {
        assertThat(shortLabelFor("Lingua Inglese B2")).isEqualTo("LIB2")
    }

    @Test
    fun `shortLabel - Analisi I modulo A strips parenthetical and converts Roman`() {
        assertThat(shortLabelFor("Analisi I (modulo A)")).isEqualTo("Anal1")
    }

    @Test
    fun `shortLabel - Metodi Matematici per l'Ingegneria splits elided article`() {
        assertThat(shortLabelFor("Metodi Matematici per l'Ingegneria")).isEqualTo("MMI")
    }

    @Test
    fun `shortLabel of blank name is null`() {
        assertThat(shortLabelFor("   ")).isNull()
    }

    @Test
    fun `shortLabel of empty string is null`() {
        assertThat(shortLabelFor("")).isNull()
    }

    @Test
    fun `shortLabel of only a parenthetical is null`() {
        assertThat(shortLabelFor("(solo modulo A)")).isNull()
    }

    @Test
    fun `shortLabel of only stop-words falls back to the first word prefix`() {
        assertThat(shortLabelFor("di e la")).isEqualTo("Di")
    }

    @Test
    fun `shortLabel of bare number keeps it as the suffix with no content`() {
        assertThat(shortLabelFor("2")).isEqualTo("2")
    }

    @Test
    fun `shortLabel takes at most five initials for long titles`() {
        assertThat(shortLabelFor("Alpha Beta Gamma Delta Epsilon Zeta")).isEqualTo("ABGDE")
    }

    @Test
    fun `shortLabel keeps a trailing two-digit number as suffix`() {
        assertThat(shortLabelFor("Storia III")).isEqualTo("Stor3")
    }

    @Test
    fun `shortLabel converts Roman X to ten`() {
        assertThat(shortLabelFor("Modulo X")).isEqualTo("Modu10")
    }

    @Test
    fun `shortLabel treats lowercase i as a function word, not a Roman numeral`() {
        assertThat(shortLabelFor("Reti i")).isEqualTo("Reti")
    }

    @Test
    fun `shortLabel splits on slash separator`() {
        assertThat(shortLabelFor("Analisi/Algebra")).isEqualTo("AA")
    }

    @Test
    fun `shortLabel strips surrounding punctuation from tokens`() {
        assertThat(shortLabelFor("Fisica, Generale")).isEqualTo("FG")
    }

    @Test
    fun `shortLabel keeps short alphanumeric module code suffix uppercased`() {
        assertThat(shortLabelFor("Laboratorio 1a")).isEqualTo("Labo1A")
    }
}
