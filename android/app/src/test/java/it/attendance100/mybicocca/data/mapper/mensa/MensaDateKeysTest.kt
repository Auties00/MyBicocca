package it.attendance100.mybicocca.data.mapper.mensa

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class MensaDateKeysTest {

    @Test
    fun `the take-away schedule keys its days by a timestamp taken at the time of the request`() {
        // Mon 23 Nov 2026 22:46 in Rome, as the live service sent it.
        assertThat(parseCompassDate("1795470397")).isEqualTo(LocalDate.of(2026, 11, 23))
        assertThat(parseCompassDate("1795556797")).isEqualTo(LocalDate.of(2026, 11, 24))
    }

    @Test
    fun `menu days still parse from ISO and Italian dates`() {
        assertThat(parseCompassDate("2026-11-04T00:00:00+01:00")).isEqualTo(LocalDate.of(2026, 11, 4))
        assertThat(parseCompassDate("04/11/2026")).isEqualTo(LocalDate.of(2026, 11, 4))
        assertThat(parseCompassDate(null)).isNull()
        assertThat(parseCompassDate("  ")).isNull()
    }
}
