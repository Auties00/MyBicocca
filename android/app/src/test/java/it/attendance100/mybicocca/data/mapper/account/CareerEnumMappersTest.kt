package it.attendance100.mybicocca.data.mapper.account

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import org.junit.Test

/**
 * Covers [mapCareerStatus]: the single-letter and mnemonic Esse3 status codes, the collapsed
 * interruption family, case folding, the null/empty sentinel, and the logged unknown-code
 * fallback. The unknown-code branch calls `android.util.Log.w`, which the module's
 * `isReturnDefaultValues` unit-test config stubs to a no-op on the JVM.
 */
class CareerEnumMappersTest {

    @Test
    fun `active codes map to ACTIVE`() {
        assertThat(mapCareerStatus("A")).isEqualTo(CareerStatus.ACTIVE)
        assertThat(mapCareerStatus("ATT")).isEqualTo(CareerStatus.ACTIVE)
        assertThat(mapCareerStatus("ATTIVA")).isEqualTo(CareerStatus.ACTIVE)
    }

    @Test
    fun `suspended codes map to SUSPENDED`() {
        assertThat(mapCareerStatus("S")).isEqualTo(CareerStatus.SUSPENDED)
        assertThat(mapCareerStatus("SOS")).isEqualTo(CareerStatus.SUSPENDED)
        assertThat(mapCareerStatus("SOSPESA")).isEqualTo(CareerStatus.SUSPENDED)
    }

    @Test
    fun `graduated codes map to GRADUATED including both gender spellings`() {
        assertThat(mapCareerStatus("L")).isEqualTo(CareerStatus.GRADUATED)
        assertThat(mapCareerStatus("LAU")).isEqualTo(CareerStatus.GRADUATED)
        assertThat(mapCareerStatus("LAUREATO")).isEqualTo(CareerStatus.GRADUATED)
        assertThat(mapCareerStatus("LAUREATA")).isEqualTo(CareerStatus.GRADUATED)
    }

    @Test
    fun `the interruption family collapses interrupted transferred and withdrawn`() {
        assertThat(mapCareerStatus("I")).isEqualTo(CareerStatus.INTERRUPTED)
        assertThat(mapCareerStatus("INT")).isEqualTo(CareerStatus.INTERRUPTED)
        assertThat(mapCareerStatus("T")).isEqualTo(CareerStatus.INTERRUPTED)
        assertThat(mapCareerStatus("TRA")).isEqualTo(CareerStatus.INTERRUPTED)
        assertThat(mapCareerStatus("R")).isEqualTo(CareerStatus.INTERRUPTED)
        assertThat(mapCareerStatus("RIN")).isEqualTo(CareerStatus.INTERRUPTED)
    }

    @Test
    fun `codes are matched case-insensitively`() {
        assertThat(mapCareerStatus("att")).isEqualTo(CareerStatus.ACTIVE)
        assertThat(mapCareerStatus("Lau")).isEqualTo(CareerStatus.GRADUATED)
    }

    @Test
    fun `null maps to OTHER`() {
        assertThat(mapCareerStatus(null)).isEqualTo(CareerStatus.OTHER)
    }

    @Test
    fun `empty string maps to OTHER`() {
        assertThat(mapCareerStatus("")).isEqualTo(CareerStatus.OTHER)
    }

    @Test
    fun `an unrecognized code degrades to OTHER`() {
        assertThat(mapCareerStatus("ZZZ")).isEqualTo(CareerStatus.OTHER)
    }
}
