package it.attendance100.mybicocca.data.update

import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.BuildConfig
import it.attendance100.mybicocca.data.local.settings.DownloadedApk
import it.attendance100.mybicocca.data.local.settings.UpdateStateStore
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.model.update.DownloadState
import it.attendance100.mybicocca.domain.model.update.AppReleaseAsset
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.rules.TemporaryFolder
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ApkDownloaderTest {

    // Deletes everything it hands out when the test finishes, so a download test doesn't leave
    // an APK-sized directory behind on every run.
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var downloader: ApkDownloader
    private lateinit var testScope: TestScope
    private lateinit var store: UpdateStateStore

    @Before
    fun setup() {
        context = mockk<Context>(relaxed = true)
        store = mockk<UpdateStateStore>(relaxed = true)
        testScope = TestScope()
        downloader = ApkDownloader(context, testScope, store)
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

        // Without CLEAR_TASK the intent can land on a still-closing installer task and be lost.
        val flags = intentSlot.captured.flags
        assertThat(flags and Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0)
        assertThat(flags and Intent.FLAG_ACTIVITY_CLEAR_TASK).isNotEqualTo(0)
        assertThat(flags and Intent.FLAG_GRANT_READ_URI_PERMISSION).isNotEqualTo(0)

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

    @Test
    fun restorePendingDownload_reOffersAVerifiedFileFromAnotherBuild() = testScope.runTest {
        val file = File.createTempFile("pending", ".apk").apply { writeText("payload") }
        every { store.downloadedApk } returns kotlinx.coroutines.flow.flowOf(
            DownloadedApk(file.absolutePath, file.length(), "0.0.6", NOT_THIS_BUILD_SHA)
        )

        downloader.restorePendingDownload()

        assertThat(downloader.downloadState.value).isEqualTo(DownloadState.Success(file))
        file.delete()
    }

    /** The cache is evictable, so a record outliving its file has to be dropped, not restored. */
    @Test
    fun restorePendingDownload_dropsTheRecordWhenTheFileIsGone() = testScope.runTest {
        every { store.downloadedApk } returns kotlinx.coroutines.flow.flowOf(
            DownloadedApk("/does/not/exist.apk", 123L, "0.0.6", NOT_THIS_BUILD_SHA)
        )

        downloader.restorePendingDownload()

        assertThat(downloader.downloadState.value).isEqualTo(DownloadState.Idle)
        io.mockk.coVerify(exactly = 1) { store.clearDownloadedApk() }
    }

    /** Matching BuildConfig.COMMIT_SHA means it's already installed; anything else must not. */
    @Test
    fun restorePendingDownload_dropsTheRecordForTheRunningBuild() = testScope.runTest {
        val file = File.createTempFile("pending", ".apk").apply { writeText("payload") }
        every { store.downloadedApk } returns kotlinx.coroutines.flow.flowOf(
            DownloadedApk(file.absolutePath, file.length(), "0.0.6", BuildConfig.COMMIT_SHA)
        )

        downloader.restorePendingDownload()

        assertThat(downloader.downloadState.value).isEqualTo(DownloadState.Idle)
        io.mockk.coVerify(exactly = 1) { store.clearDownloadedApk() }
        file.delete()
    }

    /**
     * The refactor's whole point: download() runs where it is called, so the caller owns the
     * download's lifetime. A caller that cancels must actually stop it, which is what makes a
     * foreground service wrapped around the call protect the work it is covering.
     */
    @Test
    fun download_isCancelledWithTheCallerRatherThanOutlivingIt() = testScope.runTest {
        val release = cachedRelease()

        val job = async { downloader.download(release) }
        advanceTimeBy(150)
        job.cancel()
        advanceUntilIdle()

        // Had it been launched onto the application scope, it would have run to Success regardless.
        assertThat(downloader.downloadState.value).isEqualTo(DownloadState.Idle)
    }

    /** Single-flight: a second caller is told nothing started, not queued behind the first. */
    @Test
    fun download_returnsNullWhileAnotherDownloadIsInFlight() = testScope.runTest {
        val release = cachedRelease()

        val first = async { downloader.download(release) }
        advanceTimeBy(150)
        val second = downloader.download(release)

        assertThat(second).isNull()
        advanceUntilIdle()
        assertThat(first.await()).isInstanceOf(DownloadState.Success::class.java)
    }

    @Test
    fun download_returnsTheTerminalStateToItsCaller() = testScope.runTest {
        val result = downloader.download(cachedRelease())

        assertThat(result).isInstanceOf(DownloadState.Success::class.java)
        assertThat(downloader.downloadState.value).isEqualTo(result)
    }

    /** The callback exists so a caller driving a progress notification needn't collect the flow. */
    @Test
    fun download_reportsProgressToTheCallback() = testScope.runTest {
        val seen = mutableListOf<Int>()

        downloader.download(cachedRelease()) { seen += it }

        assertThat(seen).isNotEmpty()
        assertThat(seen.first()).isEqualTo(0)
        assertThat(seen.last()).isEqualTo(100)
        assertThat(seen).isInOrder()
    }

    @Test
    fun download_withNoApkAsset_failsWithoutTouchingTheNetwork() = testScope.runTest {
        val release = releaseWith(
            AppReleaseAsset(name = "notes.txt", downloadUrl = "https://example.test/notes.txt", size = 4)
        )

        val result = downloader.download(release)

        assertThat(result).isInstanceOf(DownloadState.Error::class.java)
    }

    /** A plaintext URL must be refused before anything is fetched, never handed to the installer. */
    @Test
    fun download_overPlainHttp_isRefused() = testScope.runTest {
        val release = releaseWith(
            AppReleaseAsset(name = "app-universal.apk", downloadUrl = "http://example.test/app.apk", size = 4)
        )

        val result = downloader.download(release)

        assertThat(result).isInstanceOf(DownloadState.Error::class.java)
    }

    /**
     * A release whose APK is already in the cache and passes verification, so the download path
     * completes without a network call.
     */
    private fun cachedRelease(): AppRelease {
        val cacheDir = tempFolder.newFolder("cache")
        every { context.cacheDir } returns cacheDir

        val payload = ByteArray(2048)
        val apk = File(cacheDir, "updates").apply { mkdirs() }
            .resolve("app-universal.apk").apply { writeBytes(payload) }

        return releaseWith(
            AppReleaseAsset(
                name = apk.name,
                downloadUrl = "https://example.test/${apk.name}",
                size = payload.size.toLong(),
            )
        )
    }

    private fun releaseWith(asset: AppReleaseAsset) = AppRelease(
        versionName = "9.9.9",
        title = "Test",
        notes = "",
        pageUrl = "https://example.test",
        publishedAt = null,
        isPreRelease = false,
        assets = listOf(asset),
        commitSha = NOT_THIS_BUILD_SHA,
    )

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

    private companion object {
        /** Any SHA the running build can't have, so "already installed" never matches by accident. */
        const val NOT_THIS_BUILD_SHA = "deadbee"
    }
}
