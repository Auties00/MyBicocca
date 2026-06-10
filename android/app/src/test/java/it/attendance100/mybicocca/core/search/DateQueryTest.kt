package it.attendance100.mybicocca.core.search

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Behaviour of [parseDateQuery]: relative day words, weekday resolution (today included),
 * and numeric DD/MM[/YY[YY]] parsing with the 2000-base year rule and the past-date
 * roll-forward, all resolved against a fixed [today].
 */
class DateQueryTest {

    /** A Wednesday, deliberately mid-week and mid-month for unambiguous deltas. */
    private val today = LocalDate.of(2026, 6, 10)

    @Test
    fun `oggi resolves to today with the Oggi label`() {
        val match = parseDateQuery("oggi", today)
        assertThat(match).isNotNull()
        assertThat(match!!.date).isEqualTo(today)
        assertThat(match.label).isEqualTo("Oggi")
    }

    @Test
    fun `domani resolves to tomorrow with the Domani label`() {
        val match = parseDateQuery("domani", today)!!
        assertThat(match.date).isEqualTo(today.plusDays(1))
        assertThat(match.label).isEqualTo("Domani")
    }

    @Test
    fun `dopodomani resolves two days ahead`() {
        val match = parseDateQuery("dopodomani", today)!!
        assertThat(match.date).isEqualTo(today.plusDays(2))
        assertThat(match.label).isEqualTo("Dopodomani")
    }

    @Test
    fun `relative day words match through accent and case folding`() {
        assertThat(parseDateQuery("OGGI", today)!!.date).isEqualTo(today)
    }

    @Test
    fun `weekday later in the same week resolves forward`() {
        val match = parseDateQuery("venerdi", today)!!
        assertThat(match.date.dayOfWeek).isEqualTo(DayOfWeek.FRIDAY)
        assertThat(match.date).isEqualTo(LocalDate.of(2026, 6, 12))
    }

    @Test
    fun `accented weekday folds to the same day`() {
        val match = parseDateQuery("venerdì", today)!!
        assertThat(match.date).isEqualTo(LocalDate.of(2026, 6, 12))
    }

    @Test
    fun `asking for today's weekday resolves to today`() {
        val match = parseDateQuery("mercoledi", today)!!
        assertThat(match.date).isEqualTo(today)
    }

    @Test
    fun `a weekday already passed this week rolls to next week`() {
        val match = parseDateQuery("lunedi", today)!!
        assertThat(match.date.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
        assertThat(match.date).isEqualTo(LocalDate.of(2026, 6, 15))
    }

    @Test
    fun `domenica resolves to the upcoming Sunday`() {
        val match = parseDateQuery("domenica", today)!!
        assertThat(match.date.dayOfWeek).isEqualTo(DayOfWeek.SUNDAY)
        assertThat(match.date).isEqualTo(LocalDate.of(2026, 6, 14))
    }

    @Test
    fun `weekday label starts with the capitalized weekday name`() {
        val match = parseDateQuery("venerdi", today)!!
        assertThat(match.label).startsWith("Venerd")
        assertThat(match.label).contains("12")
    }

    @Test
    fun `numeric date with no year and in the future stays this year`() {
        val match = parseDateQuery("22/06", today)!!
        assertThat(match.date).isEqualTo(LocalDate.of(2026, 6, 22))
    }

    @Test
    fun `numeric date in the past with no year rolls to next year`() {
        val match = parseDateQuery("01/01", today)!!
        assertThat(match.date).isEqualTo(LocalDate.of(2027, 1, 1))
    }

    @Test
    fun `dot is accepted as a date separator`() {
        val match = parseDateQuery("22.06", today)!!
        assertThat(match.date).isEqualTo(LocalDate.of(2026, 6, 22))
    }

    @Test
    fun `two-digit year is interpreted as 2000-based`() {
        val match = parseDateQuery("15/03/27", today)!!
        assertThat(match.date).isEqualTo(LocalDate.of(2027, 3, 15))
    }

    @Test
    fun `four-digit year is taken literally`() {
        val match = parseDateQuery("15/03/2024", today)!!
        assertThat(match.date).isEqualTo(LocalDate.of(2024, 3, 15))
    }

    @Test
    fun `an explicit past year is not rolled forward`() {
        val match = parseDateQuery("01/01/2020", today)!!
        assertThat(match.date).isEqualTo(LocalDate.of(2020, 1, 1))
    }

    @Test
    fun `day out of range is rejected`() {
        assertThat(parseDateQuery("32/06", today)).isNull()
    }

    @Test
    fun `month out of range is rejected`() {
        assertThat(parseDateQuery("10/13", today)).isNull()
    }

    @Test
    fun `day zero is rejected`() {
        assertThat(parseDateQuery("00/06", today)).isNull()
    }

    @Test
    fun `an impossible calendar day is rejected`() {
        assertThat(parseDateQuery("31/02", today)).isNull()
    }

    @Test
    fun `empty query yields no match`() {
        assertThat(parseDateQuery("", today)).isNull()
    }

    @Test
    fun `blank query yields no match`() {
        assertThat(parseDateQuery("   ", today)).isNull()
    }

    @Test
    fun `non-date text yields no match`() {
        assertThat(parseDateQuery("analisi", today)).isNull()
    }

    @Test
    fun `partial numeric token without separator is not a date`() {
        assertThat(parseDateQuery("2206", today)).isNull()
    }

    @Test
    fun `single-digit day and month are accepted`() {
        val match = parseDateQuery("3/7", today)!!
        assertThat(match.date).isEqualTo(LocalDate.of(2026, 7, 3))
    }
}
