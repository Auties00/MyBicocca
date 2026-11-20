package it.attendance100.mybicocca.domain.model.mensa

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MensaMenuIdentityTest {

    @Test
    fun `the five Bicocca menus get the names students use`() {
        assertThat(mensaMenuIdentity(400, "Men� Bicocca").displayName).isEqualTo("Menù Bicocca")
        assertThat(mensaMenuIdentity(598, "Menu Bicocca Senza Glutine").displayName).isEqualTo("Senza glutine")
        assertThat(mensaMenuIdentity(701, "RISTORAZIONE COMPLEMENTARE U3").displayName).isEqualTo("Bar U3")
        assertThat(mensaMenuIdentity(709, "Ristorazione complementare U12").displayName).isEqualTo("Bar U12")
        assertThat(mensaMenuIdentity(712, "Ristorazione complementare U6PT").displayName).isEqualTo("Bar U6")
    }

    @Test
    fun `kinds tell the canteen, the gluten-free menu and the bars apart`() {
        assertThat(mensaMenuIdentity(400, "Menù Bicocca").kind).isEqualTo(MensaMenuKind.CANTEEN)
        assertThat(mensaMenuIdentity(598, "x").kind).isEqualTo(MensaMenuKind.GLUTEN_FREE)
        assertThat(mensaMenuIdentity(709, "x").kind).isEqualTo(MensaMenuKind.BAR)
        assertThat(mensaMenuIdentity(709, "x").building).isEqualTo("U12")
    }

    @Test
    fun `a menu with an id we do not know is read from its name`() {
        assertThat(mensaMenuIdentity(9001, "Ristorazione complementare U7").displayName).isEqualTo("Bar U7")
        assertThat(mensaMenuIdentity(9002, "Menu Bicocca senza glutine serale").kind).isEqualTo(MensaMenuKind.GLUTEN_FREE)
        assertThat(mensaMenuIdentity(9003, "Qualcos'altro").displayName).isEqualTo("Qualcos'altro")
    }

    @Test
    fun `a bare username is completed with the university domain, anything else is left alone`() {
        assertThat("l.lupi3".asMensaLoginEmail()).isEqualTo("l.lupi3@campus.unimib.it")
        assertThat("  l.lupi3 ".asMensaLoginEmail()).isEqualTo("l.lupi3@campus.unimib.it")
        assertThat("giulia@gmail.com".asMensaLoginEmail()).isEqualTo("giulia@gmail.com")
        assertThat("".asMensaLoginEmail()).isEmpty()
    }

    @Test
    fun `avoiding gluten is any of the cereal allergens`() {
        assertThat(MensaAllergenPreferences(allergenIds = setOf(48)).avoidsGluten()).isTrue()
        assertThat(MensaAllergenPreferences(allergenIds = setOf(7, 3)).avoidsGluten()).isFalse()
        val custom = MensaAllergen(99, "Cereali contenenti glutine: Kamut", "Kamut", null)
        assertThat(MensaAllergenPreferences(allergenIds = setOf(99)).avoidsGluten(listOf(custom))).isTrue()
    }
}
