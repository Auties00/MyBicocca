package it.attendance100.mybicocca.core.time

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Covers [StalePolicy.ttlFor]: a per-source override wins when present, otherwise the default TTL applies.
 */
class StalePolicyTest {

    @Test
    fun `ttlFor falls back to the default when no override exists`() {
        val policy = StalePolicy(defaultTtlMs = 60_000L)

        assertThat(policy.ttlFor("calendar")).isEqualTo(60_000L)
    }

    @Test
    fun `ttlFor uses the default for an unknown source code with overrides present`() {
        val policy = StalePolicy(
            defaultTtlMs = 60_000L,
            perSourceTtlMs = mapOf("exams" to 5_000L),
        )

        assertThat(policy.ttlFor("unmapped")).isEqualTo(60_000L)
    }

    @Test
    fun `ttlFor returns the per-source override when present`() {
        val policy = StalePolicy(
            defaultTtlMs = 60_000L,
            perSourceTtlMs = mapOf("exams" to 5_000L),
        )

        assertThat(policy.ttlFor("exams")).isEqualTo(5_000L)
    }

    @Test
    fun `ttlFor honours a zero override distinct from the default`() {
        val policy = StalePolicy(
            defaultTtlMs = 60_000L,
            perSourceTtlMs = mapOf("live" to 0L),
        )

        assertThat(policy.ttlFor("live")).isEqualTo(0L)
    }

    @Test
    fun `ttlFor resolves each source independently across multiple overrides`() {
        val policy = StalePolicy(
            defaultTtlMs = 60_000L,
            perSourceTtlMs = mapOf("exams" to 5_000L, "taxes" to 30_000L),
        )

        assertThat(policy.ttlFor("exams")).isEqualTo(5_000L)
        assertThat(policy.ttlFor("taxes")).isEqualTo(30_000L)
        assertThat(policy.ttlFor("other")).isEqualTo(60_000L)
    }
}
