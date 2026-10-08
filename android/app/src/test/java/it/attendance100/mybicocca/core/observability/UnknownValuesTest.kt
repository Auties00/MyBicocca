package it.attendance100.mybicocca.core.observability

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class UnknownValuesTest {

    private val reported = mutableListOf<Pair<String, String>>()

    @Before
    fun setup() {
        UnknownValues.resetForTest()
        UnknownValues.sink = UnknownValueSink { field, value -> reported += field to value }
    }

    @After
    fun tearDown() {
        UnknownValues.sink = null
        UnknownValues.resetForTest()
    }

    @Test
    fun a_short_code_is_kept_upper_cased() {
        assertThat(sanitizeUnknownValue(" dec ")).isEqualTo("DEC")
        assertThat(sanitizeUnknownValue("PAGAMENTO_ESEGUITO")).isEqualTo("PAGAMENTO_ESEGUITO")
        assertThat(sanitizeUnknownValue("7")).isEqualTo("7")
        assertThat(sanitizeUnknownValue("S1")).isEqualTo("S1")
        assertThat(sanitizeUnknownValue("h5pactivity")).isEqualTo("H5PACTIVITY")
    }

    @Test
    fun free_text_is_replaced_by_its_length() {
        assertThat(sanitizeUnknownValue("Mario Rossi")).isEqualTo("<not-a-code:11>")
        assertThat(sanitizeUnknownValue("A".repeat(21))).isEqualTo("<not-a-code:21>")
    }

    @Test
    fun values_shaped_like_an_identifier_are_not_treated_as_codes() {
        assertThat(sanitizeUnknownValue("900001")).isEqualTo("<not-a-code:6>")
        assertThat(sanitizeUnknownValue("RSSMRA80A01H501U")).isEqualTo("<not-a-code:16>")
        assertThat(sanitizeUnknownValue("333-1234567")).isEqualTo("<not-a-code:11>")
        assertThat(sanitizeUnknownValue("m.rossi")).isEqualTo("<not-a-code:7>")
        assertThat(sanitizeUnknownValue("m.rossi@campus.unimib.it")).isEqualTo("<not-a-code:24>")
    }

    @Test
    fun missing_values_get_a_placeholder() {
        assertThat(sanitizeUnknownValue(null)).isEqualTo("<null>")
        assertThat(sanitizeUnknownValue("   ")).isEqualTo("<blank>")
    }

    @Test
    fun a_pair_is_reported_once() {
        UnknownValues.report("career_status", "dec")
        UnknownValues.report("career_status", "DEC")
        UnknownValues.report("career_status", "ces")

        assertThat(reported).containsExactly("career_status" to "DEC", "career_status" to "CES")
    }

    @Test
    fun a_missing_value_is_reported_only_when_asked_for() {
        UnknownValues.report("exam_type", null)
        UnknownValues.report("career_status", null, reportMissing = true)

        assertThat(reported).containsExactly("career_status" to "<null>")
    }

    @Test
    fun nothing_is_reported_without_a_sink() {
        UnknownValues.sink = null

        UnknownValues.report("career_status", "dec")

        assertThat(reported).isEmpty()
    }
}
