package it.attendance100.mybicocca.data.update

import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ApkDownloadWorkerTest {

    @Test
    fun `an unattended nightly download waits for an unmetered network`() {
        assertThat(ApkDownloadWorker.networkTypeFor(ApkDownloadWorker.SOURCE_NIGHTLY))
            .isEqualTo(NetworkType.UNMETERED)
    }

    @Test
    fun `a tapped or stable download runs on any connection`() {
        assertThat(ApkDownloadWorker.networkTypeFor(ApkDownloadWorker.SOURCE_EXPLICIT))
            .isEqualTo(NetworkType.CONNECTED)
        assertThat(ApkDownloadWorker.networkTypeFor(ApkDownloadWorker.SOURCE_STABLE))
            .isEqualTo(NetworkType.CONNECTED)
    }

    /** Otherwise a tap is dropped behind a background nightly that is still waiting for Wi-Fi. */
    @Test
    fun `a tap replaces queued background work`() {
        assertThat(ApkDownloadWorker.policyFor(ApkDownloadWorker.SOURCE_EXPLICIT))
            .isEqualTo(ExistingWorkPolicy.REPLACE)
    }

    @Test
    fun `background requests never replace a download already queued or running`() {
        assertThat(ApkDownloadWorker.policyFor(ApkDownloadWorker.SOURCE_NIGHTLY))
            .isEqualTo(ExistingWorkPolicy.KEEP)
        assertThat(ApkDownloadWorker.policyFor(ApkDownloadWorker.SOURCE_STABLE))
            .isEqualTo(ExistingWorkPolicy.KEEP)
    }
}
