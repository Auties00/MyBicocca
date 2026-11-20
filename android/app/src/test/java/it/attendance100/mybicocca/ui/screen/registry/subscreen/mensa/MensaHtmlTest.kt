package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MensaHtmlTest {

    @Test
    fun `tags are dropped and bold ranges kept`() {
        val rich = MensaHtml.parse("Patate fiocchi per purè (<strong>latte</strong>)")

        assertThat(rich.text).isEqualTo("Patate fiocchi per purè (latte)")
        assertThat(rich.bold).containsExactly(25..29)
        assertThat(rich.text.substring(25, 30)).isEqualTo("latte")
    }

    @Test
    fun `allergen names lose their markup`() {
        assertThat(MensaHtml.plain("Cereali contenenti glutine: <strong>Grano</strong>"))
            .isEqualTo("Cereali contenenti glutine: Grano")
        assertThat(MensaHtml.plain("100 <span>g</span>")).isEqualTo("100 g")
    }

    @Test
    fun `a bare greater-than sign is text, not a tag`() {
        assertThat(MensaHtml.plain("Anidride Solforosa e Solfiti >10 mg/l"))
            .isEqualTo("Anidride Solforosa e Solfiti >10 mg/l")
        assertThat(MensaHtml.plain("a < b and c > d")).isEqualTo("a < b and c > d")
    }

    @Test
    fun `breaks become line breaks in rich text and spaces in plain text`() {
        assertThat(MensaHtml.parse("di cui<br>Saturi").text).isEqualTo("di cui\nSaturi")
        assertThat(MensaHtml.plain("di cui<br>Saturi")).isEqualTo("di cui Saturi")
        assertThat(MensaHtml.parse("<p>Uno</p>\r\n<p>Due</p>").text).isEqualTo("Uno\n\nDue")
    }

    @Test
    fun `entities are decoded`() {
        assertThat(MensaHtml.plain("Panna &amp; latte&nbsp;caldo, caff&egrave; &#233; &#x41;"))
            .isEqualTo("Panna & latte caldo, caffè é A")
    }

    @Test
    fun `empty and null input give empty text`() {
        assertThat(MensaHtml.parse(null).text).isEmpty()
        assertThat(MensaHtml.parse("<p><strong></strong></p>").text).isEmpty()
    }

    @Test
    fun `the generic chef blurb counts as no description, accents or not`() {
        val generic = "<p>I nostri Chef, nell'elaborare le ricette utilizzeranno prodotti di prima qualità e si " +
            "atterranno scrupolosamente alle procedure dettate  dal ricettario aziendale. Tali procedure sono " +
            "frutto dell'esperienza maturata in molti anni di attività.</p>\r\n<p><strong></strong>N.B. : Gli " +
            "allergeni indicati derivano dagli ingredienti utilizzati, dalle ricette operative e dalle schede " +
            "tecniche dei prodotti. La preparazione e il servizio saranno gestiti secondo le procedure previste " +
            "dal piano HACCP, con misure atte a prevenire e ridurre il rischio di contaminazioni crociate. " +
            "Ingredienti e allergeni saranno comunicati agli utenti secondo la normativa vigente.</p>\r\n" +
            "<p><strong></strong></p>"

        assertThat(MensaDescriptions.useful(generic)).isNull()
        assertThat(MensaDescriptions.useful(generic.replace('à', '�'))).isNull()
        assertThat(MensaDescriptions.useful(null)).isNull()
        assertThat(MensaDescriptions.useful("  ")).isNull()
    }

    @Test
    fun `a description that differs from the blurb is shown cleaned`() {
        val rich = MensaDescriptions.useful("<p>Pasta con <strong>pomodoro</strong> fresco.</p>")

        assertThat(rich?.text).isEqualTo("Pasta con pomodoro fresco.")
        assertThat(rich?.bold).hasSize(1)
        assertThat(MensaDescriptions.useful("FRUTTA FRESCA A ROTAZIONE")?.text).isEqualTo("FRUTTA FRESCA A ROTAZIONE")
    }

    @Test
    fun `ready-made products' placeholder counts as no description`() {
        assertThat(MensaDescriptions.useful("Prodotto pronto")).isNull()
    }

    @Test
    fun `allergen chips drop the leading Contiene and capitalise`() {
        assertThat(cleanAllergenName("Contiene ingredienti surgelati/congelati"))
            .isEqualTo("Ingredienti surgelati/congelati")
        assertThat(cleanAllergenName("Contiene Carne di Maiale")).isEqualTo("Carne di Maiale")
        assertThat(cleanAllergenName("contiene soia")).isEqualTo("Soia")
        assertThat(cleanAllergenName("Cereali contenenti glutine: <strong>Grano</strong>"))
            .isEqualTo("Cereali contenenti glutine: Grano")
        assertThat(cleanAllergenName("Latte e prodotti a base di Latte")).isEqualTo("Latte e prodotti a base di Latte")
    }
}
