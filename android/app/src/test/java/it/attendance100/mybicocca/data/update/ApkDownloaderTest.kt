package it.attendance100.mybicocca.data.update

import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ApkDownloaderTest {

    private lateinit var context: Context
    private lateinit var downloader: ApkDownloader
    private lateinit var testScope: TestScope

    @Before
    fun setup() {
        context = mockk<Context>(relaxed = true)
        testScope = TestScope()
        downloader = ApkDownloader(context, testScope)
    }

    /**
     * API 31+ is where the silent `PackageInstaller` path used to take over. Installing is now
     * always the user's call, so even here it has to be the system installer dialog.
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun installApk_api31_usesSystemInstallerDialog() = testScope.runTest {
        val file = File.createTempFile("test", ".apk")
        val intentSlot = slot<Intent>()
        every { context.startActivity(capture(intentSlot)) } returns Unit
        every { context.packageName } returns "it.attendance100.mybicocca"

        io.mockk.mockkStatic(androidx.core.content.FileProvider::class)
        every {
            androidx.core.content.FileProvider.getUriForFile(context, any(), file)
        } returns android.net.Uri.parse("content://dummy")

        downloader.installApk(file)

        verify(exactly = 1) { context.startActivity(any()) }
        assertThat(intentSlot.captured.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(intentSlot.captured.type).isEqualTo("application/vnd.android.package-archive")
        verify(exactly = 0) { context.packageManager.packageInstaller }

        io.mockk.unmockkStatic(androidx.core.content.FileProvider::class)
        file.delete()
    }

    @Test
    fun returningFromInstallerWithInstallPending_reportsDeclined() = testScope.runTest {
        val file = stubInstallerLaunch()

        downloader.installApk(file)
        downloader.onAppBackgrounded()
        downloader.onAppForegrounded()

        assertThat(downloader.downloadState.value)
            .isEqualTo(DownloadState.InstallDeclined(file))

        io.mockk.unmockkStatic(androidx.core.content.FileProvider::class)
        file.delete()
    }

    /** Foregrounding without the installer ever having taken us away isn't a decline. */
    @Test
    fun foregroundedWithoutLeaving_doesNotReportDeclined() = testScope.runTest {
        val file = stubInstallerLaunch()

        downloader.installApk(file)
        downloader.onAppForegrounded()

        assertThat(downloader.downloadState.value).isEqualTo(DownloadState.Idle)

        io.mockk.unmockkStatic(androidx.core.content.FileProvider::class)
        file.delete()
    }

    private fun stubInstallerLaunch(): File {
        val file = File.createTempFile("test", ".apk")
        every { context.startActivity(any()) } returns Unit
        every { context.packageName } returns "it.attendance100.mybicocca"
        io.mockk.mockkStatic(androidx.core.content.FileProvider::class)
        every {
            androidx.core.content.FileProvider.getUriForFile(context, any(), file)
        } returns android.net.Uri.parse("content://dummy")
        return file
    }
}
