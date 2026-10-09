package it.attendance100.mybicocca.data.mapper.account

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.core.observability.UnknownValueSink
import it.attendance100.mybicocca.core.observability.UnknownValues
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.career.isOpen
import it.attendance100.mybicocca.domain.model.elearning.course.CourseLevel
import org.junit.Test

/**
 * Covers [mapCareerStatus]: the single-letter and mnemonic Esse3 status codes, the provisional
 * "Ipotesi" code, the collapsed interruption family, case folding, the null/empty sentinel, and
 * the reported unknown-code fallback.
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
    fun `the Ipotesi code maps to PROVISIONAL and counts as open`() {
        assertThat(mapCareerStatus("I")).isEqualTo(CareerStatus.PROVISIONAL)
        assertThat(CareerStatus.PROVISIONAL.isOpen).isTrue()
    }

    @Test
    fun `an unknown status reports its code and its reason`() {
        val reported = mutableListOf<Pair<String, String>>()
        UnknownValues.resetForTest()
        UnknownValues.sink = UnknownValueSink { field, value -> reported += field to value }
        try {
            mapCareerStatus("X", reasonCode = "TIT")
        } finally {
            UnknownValues.sink = null
            UnknownValues.resetForTest()
        }

        assertThat(reported).containsExactly(
            "career_status" to "X",
            "career_status_reason" to "TIT",
        )
    }

    @Test
    fun `course type codes map to the degree level`() {
        assertThat(mapCareerLevel("L2")).isEqualTo(CourseLevel.Bachelor)
        assertThat(mapCareerLevel("lm")).isEqualTo(CourseLevel.Master)
        assertThat(mapCareerLevel("LM5")).isEqualTo(CourseLevel.Master)
        assertThat(mapCareerLevel("ZZ")).isNull()
        assertThat(mapCareerLevel(null)).isNull()
    }

    @Test
    fun `the interruption family collapses interrupted transferred and withdrawn`() {
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
