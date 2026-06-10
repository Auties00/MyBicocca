package it.attendance100.mybicocca.data.mapper.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Covers the tolerant Esse3 date parsing helpers: canonical dd/MM/yyyy, the trailing time
 * component stripping, the optional ISO fallback, and the null-on-anything-bad contract.
 */
class Esse3DateFormatsTest {

    @Test
    fun `parseEsse3Date reads a plain dd MM yyyy value`() {
        assertThat("25/12/2024".parseEsse3Date()).isEqualTo(LocalDate.of(2024, 12, 25))
    }

    @Test
    fun `parseEsse3Date strips a trailing space time component`() {
        assertThat("01/03/2023 23:59:59".parseEsse3Date()).isEqualTo(LocalDate.of(2023, 3, 1))
    }

    @Test
    fun `parseEsse3Date strips a trailing T time component`() {
        assertThat("01/03/2023T10:30:00".parseEsse3Date()).isEqualTo(LocalDate.of(2023, 3, 1))
    }

    @Test
    fun `parseEsse3Date trims surrounding whitespace`() {
        assertThat("  07/07/2022  ".parseEsse3Date()).isEqualTo(LocalDate.of(2022, 7, 7))
    }

    @Test
    fun `parseEsse3Date returns null for null`() {
        assertThat((null as String?).parseEsse3Date()).isNull()
    }

    @Test
    fun `parseEsse3Date returns null for blank`() {
        assertThat("   ".parseEsse3Date()).isNull()
    }

    @Test
    fun `parseEsse3Date returns null for an unparseable value`() {
        assertThat("not-a-date".parseEsse3Date()).isNull()
    }

    @Test
    fun `parseEsse3Date does not accept an ISO local date`() {
        assertThat("2024-12-25".parseEsse3Date()).isNull()
    }

    @Test
    fun `parseEsse3DateOrIso reads the canonical dd MM yyyy first`() {
        assertThat("25/12/2024".parseEsse3DateOrIso()).isEqualTo(LocalDate.of(2024, 12, 25))
    }

    @Test
    fun `parseEsse3DateOrIso falls back to an ISO local date`() {
        assertThat("2024-12-25".parseEsse3DateOrIso()).isEqualTo(LocalDate.of(2024, 12, 25))
    }

    @Test
    fun `parseEsse3DateOrIso strips an ISO time component before parsing`() {
        assertThat("2024-12-25T08:00:00".parseEsse3DateOrIso()).isEqualTo(LocalDate.of(2024, 12, 25))
    }

    @Test
    fun `parseEsse3DateOrIso returns null for null`() {
        assertThat((null as String?).parseEsse3DateOrIso()).isNull()
    }

    @Test
    fun `parseEsse3DateOrIso returns null when nothing matches`() {
        assertThat("garbage".parseEsse3DateOrIso()).isNull()
    }

    @Test
    fun `parseEsse3DateTime reads a full dd MM yyyy HH mm ss value`() {
        assertThat("31/01/2025 14:05:09".parseEsse3DateTime())
            .isEqualTo(LocalDateTime.of(2025, 1, 31, 14, 5, 9))
    }

    @Test
    fun `parseEsse3DateTime returns null when the time component is missing`() {
        assertThat("31/01/2025".parseEsse3DateTime()).isNull()
    }

    @Test
    fun `parseEsse3DateTime returns null for null`() {
        assertThat((null as String?).parseEsse3DateTime()).isNull()
    }

    @Test
    fun `parseEsse3DateTime returns null for blank`() {
        assertThat("  ".parseEsse3DateTime()).isNull()
    }
}
