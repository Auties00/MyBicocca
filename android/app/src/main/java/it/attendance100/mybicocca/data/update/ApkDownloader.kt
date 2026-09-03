package it.attendance100.mybicocca.data.update

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.prepareGet
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.readRemaining
import it.attendance100.mybicocca.core.io.sha256Hex
import it.attendance100.mybicocca.core.text.UiText
import it.attendance100.mybicocca.core.version.isRunningBuild
import it.attendance100.mybicocca.data.local.settings.UpdateStateStore
import it.attendance100.mybicocca.di.ApplicationScope
import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.model.update.DownloadState
import it.attendance100.mybicocca.domain.model.update.AppReleaseAsset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.io.asSink
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

// TODO(update-notifications): the interactive call sites still go through startDownload, which
// launches on @ApplicationScope with no foreground-service promotion, so the OS can freeze/kill
// them seconds after the app backgrounds mid-download. download() below is the shape that fixes
// it; see /NOTIFICATIONS_PLAN.md step 6 for the migration.
@Singleton
class ApkDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val scope: CoroutineScope,
    private val store: UpdateStateStore,
) {
    private val client = HttpClient(OkHttp)

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    // One updates directory and one downloadState, so one download at a time. tryLock rather than
    // lock: a second caller is told "already running" instead of queueing behind the first and
    // then re-downloading the moment it finishes.
    private val downloadLock = Mutex()

    // The APK handed to the system installer, if we're waiting on that dialog. ACTION_VIEW reports
    // nothing back, so a decline has to be inferred: a successful install replaces the process, so
    // coming back with this still set means the user dismissed the dialog.
    private var pendingInstall: File? = null

    // ...but only once the installer actually took us to the background, so a resume that never
    // left (or one the installer never fronted) isn't mistaken for a decline.
    private var leftForInstaller = false

    /** Driven by [UpdateChecker]'s process-lifecycle observer rather than one registered here. */
    fun onAppBackgrounded() {
        if (pendingInstall != null) leftForInstaller = true
    }

    fun onAppForegrounded() {
        val file = pendingInstall
        if (file == null || !leftForInstaller) return
        clearPendingInstall()
        _downloadState.value = DownloadState.InstallDeclined(file)
    }

    private fun clearPendingInstall() {
        pendingInstall = null
        leftForInstaller = false
    }

    /**
     * Downloads [release] **in the caller's coroutine**, returning the terminal state, or null
     * when a download is already in flight.
     *
     * Running in the caller's coroutine is the whole point. Whoever calls owns the download's
     * lifetime: cancelling the caller cancels the download, and a foreground service wrapped
     * around this call covers exactly the work it is protecting. Neither holds for a download
     * launched onto an application-wide scope, which outlives whatever started it and leaves the
     * service protecting nothing.
     *
     * [onProgress] reports percentage as it goes, for a caller driving its own progress display.
     * It exists so such a caller doesn't have to collect [downloadState] and guess which download
     * the values belong to.
     */
    suspend fun download(release: AppRelease, onProgress: (Int) -> Unit = {}): DownloadState? {
        if (!downloadLock.tryLock()) return null

        return try {
            clearPendingInstall()
            publish(DownloadState.Downloading(0), onProgress)
            runDownload(release, onProgress)
        } finally {
            downloadLock.unlock()
        }
    }

    /**
     * Starts a download on the application scope, for a caller with no coroutine of its own and
     * no way to be told when it finishes.
     */
    fun startDownload(release: AppRelease) {
        scope.launch { download(release) }
    }

    private suspend fun runDownload(release: AppRelease, onProgress: (Int) -> Unit): DownloadState =
        try {
            val defaultAbi = "universal"
            val supportedAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: defaultAbi

            // Find the best asset
            val asset = release.assets.find { it.name.contains(supportedAbi, ignoreCase = true) }
                ?: release.assets.find { it.name.contains("universal", ignoreCase = true) }
                ?: release.assets.firstOrNull { it.name.endsWith(".apk") }

            if (asset == null) {
                val availableAssets = release.assets.joinToString { it.name }
                fail(
                    UiText.StringResource(
                        it.attendance100.mybicocca.R.string.apk_downloader_no_suitable_apk,
                        supportedAbi,
                        availableAssets
                    )
                )
            } else if (!asset.downloadUrl.startsWith("https://", ignoreCase = true)) {
                // Never hand a plaintext-fetched binary to the installer
                fail(UiText.StringResource(it.attendance100.mybicocca.R.string.apk_downloader_insecure_connection))
            } else {
                fetch(asset, release, onProgress)
            }
        } catch (e: CancellationException) {
            // Cancellation leaves no terminal state behind: whoever cancelled decides what to show.
            _downloadState.value = DownloadState.Idle
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            fail(
                e.message?.let { UiText.DynamicString(it) }
                    ?: UiText.StringResource(it.attendance100.mybicocca.R.string.apk_downloader_failed)
            )
        }

    private suspend fun fetch(
        asset: AppReleaseAsset,
        release: AppRelease,
        onProgress: (Int) -> Unit,
    ): DownloadState {
        val updatesDir = File(context.cacheDir, "updates")
        if (!updatesDir.exists()) updatesDir.mkdirs()
        val apkFile = File(updatesDir, asset.name)

        if (apkFile.exists() && apkFile.passesIntegrityCheck(asset)) {
            // Already downloaded and verified; play the progress animation for UX
            for (i in 0..100 step 10) {
                publish(DownloadState.Downloading(i), onProgress)
                delay(100.milliseconds)
            }
            delay(500.milliseconds)
            return markDownloaded(apkFile, release)
        }

        downloadToFile(asset, apkFile, onProgress)

        if (!apkFile.passesIntegrityCheck(asset)) {
            apkFile.delete()
            return fail(UiText.StringResource(it.attendance100.mybicocca.R.string.apk_downloader_integrity_check_failed))
        }

        publish(DownloadState.Downloading(100), onProgress)
        return markDownloaded(apkFile, release)
    }

    private fun publish(state: DownloadState, onProgress: (Int) -> Unit) {
        _downloadState.value = state
        if (state is DownloadState.Downloading) onProgress(state.progress)
    }

    private fun fail(message: UiText): DownloadState =
        DownloadState.Error(message).also { _downloadState.value = it }

    /**
     * Streams [asset] to [target] in fixed-size chunks, publishing download progress as it goes.
     */
    private suspend fun downloadToFile(
        asset: AppReleaseAsset,
        target: File,
        onProgress: (Int) -> Unit,
    ) {
        withContext(Dispatchers.IO) {
            client.prepareGet(asset.downloadUrl).execute { response ->
                val channel: ByteReadChannel = response.body()
                val total = response.contentLength() ?: asset.size
                var received = 0L
                var lastProgress = -1

                target.outputStream().asSink().use { sink ->
                    while (!channel.exhausted()) {
                        received += channel.readRemaining(DOWNLOAD_CHUNK_BYTES).transferTo(sink)
                        if (total > 0) {
                            val progress =
                                ((received.toFloat() / total) * 100).roundToInt().coerceIn(0, 100)
                            if (progress != lastProgress) {
                                lastProgress = progress
                                publish(DownloadState.Downloading(progress), onProgress)
                            }
                        }
                    }
                }
            }
        }
    }

    fun dismissError() {
        if (_downloadState.value is DownloadState.Error) {
            _downloadState.value = DownloadState.Idle
        }
    }

    fun resetState() {
        clearPendingInstall()
        _downloadState.value = DownloadState.Idle
        scope.launch { store.clearDownloadedApk() }
    }

    private suspend fun markDownloaded(file: File, release: AppRelease): DownloadState {
        store.setDownloadedApk(
            path = file.absolutePath,
            size = file.length(),
            versionName = release.versionName,
            commitSha = release.commitSha,
        )
        return DownloadState.Success(file).also { _downloadState.value = it }
    }

    /**
     * Re-offers a download that finished before the process died, so a ready APK isn't forgotten
     * and re-fetched. Called once at startup.
     *
     * The record is dropped rather than restored when it turns out to describe the build that's
     * now running — which is the normal case after the user actually installs it, since a
     * successful install replaces the process and never gets to clear it.
     */
    suspend fun restorePendingDownload() {
        if (_downloadState.value !is DownloadState.Idle) return
        val record = store.downloadedApk.first() ?: return

        val file = File(record.path)
        if (isRunningBuild(record.commitSha, record.versionName) ||
            !file.isFile || file.length() != record.size
        ) {
            store.clearDownloadedApk()
            return
        }

        _downloadState.value = DownloadState.Success(file)
    }

    /**
     * Whether a local file can be trusted as the given [asset]'s payload. The size must always
     * match; when the source advertised a `sha256:<hex>` [AppReleaseAsset.digest] the file's
     * hash must match it too. If the digest is missing or uses an algorithm we don't recognise,
     * the size check stands alone — the best guarantee the source gave us.
     */
    private fun File.passesIntegrityCheck(asset: AppReleaseAsset): Boolean {
        if (length() != asset.size) return false

        val expectedSha = asset.digest
            ?.substringAfter("sha256:", missingDelimiterValue = "")
            ?.takeIf { it.isNotBlank() }
            ?: return true

        return sha256Hex()?.equals(expectedSha, ignoreCase = true) == true
    }

    /**
     * Hands a downloaded APK to the system installer, which always asks the user to confirm.
     * Installing is never automatic: the app only ever gets an update as far as "downloaded and
     * ready", and the tap that opens this dialog is the user's.
     */
    fun installApk(file: File) {
        pendingInstall = file
        leftForInstaller = false
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            // CLEAR_TASK because NEW_TASK on its own reuses a matching existing task: after an
            // earlier install in the same session, the installer's own task can still be winding
            // down, and the intent gets delivered to whatever activity is on top of it (observed:
            // START_DELIVERED_TO_TOP onto a dying DeleteStagedFileOnResult) instead of starting a
            // fresh install. Nothing is shown and the tap is silently lost.
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    companion object {
        private const val DOWNLOAD_CHUNK_BYTES: Long = 1L * 1024 * 1024
    }
}
