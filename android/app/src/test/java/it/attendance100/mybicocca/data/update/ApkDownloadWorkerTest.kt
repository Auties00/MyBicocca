package it.attendance100.mybicocca.data.update

import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.WorkInfo
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.update.ApkDownloadWorker.Companion.SOURCE_EXPLICIT
import it.attendance100.mybicocca.data.update.ApkDownloadWorker.Companion.SOURCE_NIGHTLY
import it.attendance100.mybicocca.data.update.ApkDownloadWorker.Companion.SOURCE_STABLE
import it.attendance100.mybicocca.data.update.ApkDownloadWorker.Companion.networkTypeFor
import it.attendance100.mybicocca.data.update.ApkDownloadWorker.Companion.policyFor
import org.junit.Test

class ApkDownloadWorkerTest {

    @Test
    fun `an automatic download waits for an unmetered network unless mobile data is allowed`() {
        for (source in listOf(SOURCE_STABLE, SOURCE_NIGHTLY)) {
            assertThat(networkTypeFor(source, allowMobileData = false)).isEqualTo(NetworkType.UNMETERED)
            assertThat(networkTypeFor(source, allowMobileData = true)).isEqualTo(NetworkType.CONNECTED)
        }
    }

    @Test
    fun `a tap runs on any connection whatever the setting`() {
        assertThat(networkTypeFor(SOURCE_EXPLICIT, allowMobileData = false)).isEqualTo(NetworkType.CONNECTED)
    }

    @Test
    fun `an empty slot takes any request`() {
        assertThat(policyFor(SOURCE_NIGHTLY, existingState = null, existingSource = null))
            .isEqualTo(ExistingWorkPolicy.KEEP)
    }

    @Test
    fun `a running download is never replaced`() {
        for (source in listOf(SOURCE_EXPLICIT, SOURCE_STABLE, SOURCE_NIGHTLY)) {
            assertThat(policyFor(source, WorkInfo.State.RUNNING, SOURCE_NIGHTLY))
                .isEqualTo(ExistingWorkPolicy.KEEP)
        }
    }

    /** Otherwise the tap is dropped behind an automatic download that is waiting for Wi-Fi. */
    @Test
    fun `a tap replaces a download still waiting`() {
        assertThat(policyFor(SOURCE_EXPLICIT, WorkInfo.State.ENQUEUED, SOURCE_NIGHTLY))
            .isEqualTo(ExistingWorkPolicy.REPLACE)
    }

    /** Otherwise a stable auto-download is dropped behind a nightly that is waiting for Wi-Fi. */
    @Test
    fun `an automatic request replaces an automatic one still waiting`() {
        assertThat(policyFor(SOURCE_STABLE, WorkInfo.State.ENQUEUED, SOURCE_NIGHTLY))
            .isEqualTo(ExistingWorkPolicy.REPLACE)
    }

    @Test
    fun `an automatic request never replaces a tap still waiting for a connection`() {
        assertThat(policyFor(SOURCE_NIGHTLY, WorkInfo.State.ENQUEUED, SOURCE_EXPLICIT))
            .isEqualTo(ExistingWorkPolicy.KEEP)
    }
}
