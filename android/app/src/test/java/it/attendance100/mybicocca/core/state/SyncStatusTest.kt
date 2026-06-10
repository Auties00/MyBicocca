package it.attendance100.mybicocca.core.state

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Smoke coverage for the [SyncStatus] sealed hierarchy: the singletons are stable and [SyncStatus.Failed]
 * carries its cause for the user-facing error mapping.
 */
class SyncStatusTest {

    @Test
    fun `Idle and Refreshing are stable singletons`() {
        assertThat(SyncStatus.Idle).isSameInstanceAs(SyncStatus.Idle)
        assertThat(SyncStatus.Refreshing).isSameInstanceAs(SyncStatus.Refreshing)
        assertThat(SyncStatus.Idle).isNotEqualTo(SyncStatus.Refreshing)
    }

    @Test
    fun `Failed carries its cause and compares by it`() {
        val cause = IllegalStateException("boom")

        val failed = SyncStatus.Failed(cause)

        assertThat(failed.cause).isSameInstanceAs(cause)
        assertThat(failed).isEqualTo(SyncStatus.Failed(cause))
    }
}
