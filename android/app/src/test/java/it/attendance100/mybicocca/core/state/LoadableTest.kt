package it.attendance100.mybicocca.core.state

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Covers the [Loadable] transformation helpers: [map] passes [Loadable.NotYetLoaded] through
 * unchanged and transforms a [Loadable.Loaded] value, and [valueOrNull] unwraps only the loaded case.
 */
class LoadableTest {

    @Test
    fun `map passes NotYetLoaded through unchanged`() {
        val source: Loadable<Int> = Loadable.NotYetLoaded

        val result = source.map { it + 1 }

        assertThat(result).isSameInstanceAs(Loadable.NotYetLoaded)
    }

    @Test
    fun `map does not invoke transform for NotYetLoaded`() {
        var invoked = false
        val source: Loadable<Int> = Loadable.NotYetLoaded

        source.map { invoked = true; it }

        assertThat(invoked).isFalse()
    }

    @Test
    fun `map transforms the loaded value`() {
        val source: Loadable<Int> = Loadable.Loaded(21)

        val result = source.map { it * 2 }

        assertThat(result).isEqualTo(Loadable.Loaded(42))
    }

    @Test
    fun `map can change the loaded type`() {
        val source: Loadable<Int> = Loadable.Loaded(7)

        val result = source.map { "value=$it" }

        assertThat(result).isEqualTo(Loadable.Loaded("value=7"))
    }

    @Test
    fun `map preserves an empty loaded list as a genuine empty state`() {
        val source: Loadable<List<String>> = Loadable.Loaded(emptyList())

        val result = source.map { it.size }

        assertThat(result).isEqualTo(Loadable.Loaded(0))
    }

    @Test
    fun `valueOrNull returns null while NotYetLoaded`() {
        val source: Loadable<String> = Loadable.NotYetLoaded

        assertThat(source.valueOrNull()).isNull()
    }

    @Test
    fun `valueOrNull returns the loaded value`() {
        val source: Loadable<String> = Loadable.Loaded("ready")

        assertThat(source.valueOrNull()).isEqualTo("ready")
    }

    @Test
    fun `valueOrNull returns a loaded null value`() {
        val source: Loadable<String?> = Loadable.Loaded(null)

        assertThat(source.valueOrNull()).isNull()
    }
}
