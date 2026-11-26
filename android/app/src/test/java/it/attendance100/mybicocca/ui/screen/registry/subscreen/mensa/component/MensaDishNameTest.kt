package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MensaDishNameTest {

    @Test
    fun `capitals become sentence case, keeping the codes`() {
        assertThat("PASTA AL POMODORO".toDishDisplayName()).isEqualTo("Pasta al pomodoro")
        assertThat("PASTA SG AL POMODORO".toDishDisplayName()).isEqualTo("Pasta SG al pomodoro")
    }

    @Test
    fun `litres keep their capital L`() {
        assertThat("ACQUA NATURALE 0,5 L".toDishDisplayName()).isEqualTo("Acqua naturale 0,5 L")
        assertThat("COCA COLA 1,5L".toDishDisplayName()).isEqualTo("Coca cola 1,5L")
        assertThat("INSALATA LEGGERA".toDishDisplayName()).isEqualTo("Insalata leggera")
    }

    @Test
    fun `an all-lowercase name gets a capital first letter`() {
        assertThat("bibita".toDishDisplayName()).isEqualTo("Bibita")
        assertThat("acqua naturale".toDishDisplayName()).isEqualTo("Acqua naturale")
    }

    @Test
    fun `a mixed-case name stays as written`() {
        assertThat("Insalata di Farro".toDishDisplayName()).isEqualTo("Insalata di Farro")
    }
}
