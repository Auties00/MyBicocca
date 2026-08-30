package it.attendance100.mybicocca.data.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
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

    @Test
    @Config(sdk = [Build.VERSION_CODES.R]) // API 30: fallback to ACTION_VIEW
    fun installApk_silentTrue_api30_fallsBackToActionView() = testScope.runTest {
        val file = File.createTempFile("test", ".apk")
        val intentSlot = slot<Intent>()
        every { context.startActivity(capture(intentSlot)) } returns Unit
        every { context.packageName } returns "it.attendance100.mybicocca"

        io.mockk.mockkStatic(androidx.core.content.FileProvider::class)
        every { androidx.core.content.FileProvider.getUriForFile(context, any(), file) } returns android.net.Uri.parse("content://dummy")

        downloader.installApk(file, silent = true)

        verify(exactly = 1) { context.startActivity(any()) }
        assertThat(intentSlot.captured.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(intentSlot.captured.type).isEqualTo("application/vnd.android.package-archive")

        io.mockk.unmockkStatic(androidx.core.content.FileProvider::class)
        file.delete()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S]) // API 31: uses silent PackageInstaller
    fun installApk_silentTrue_api31_usesPackageInstaller() = testScope.runTest {
        val file = File.createTempFile("test", ".apk")
        file.writeText("dummy content")

        val packageManager = mockk<PackageManager>(relaxed = true)
        val packageInstaller = mockk<PackageInstaller>(relaxed = true)
        val session = mockk<PackageInstaller.Session>(relaxed = true)

        every { context.packageManager } returns packageManager
        every { packageManager.packageInstaller } returns packageInstaller
        every { packageInstaller.createSession(any()) } returns 123
        every { packageInstaller.openSession(123) } returns session
        
        val outStream = java.io.ByteArrayOutputStream()
        every { session.openWrite("package", 0, file.length()) } returns outStream

        downloader.installApk(file, silent = true)

        verify(exactly = 1) { packageInstaller.createSession(any()) }
        verify(exactly = 1) { session.commit(any()) }

        file.delete()
    }
}
