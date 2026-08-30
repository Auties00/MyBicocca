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
import it.attendance100.mybicocca.di.ApplicationScope
import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.model.update.AppReleaseAsset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.asSink
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

sealed interface DownloadState {
    data object Idle : DownloadState
    data class Downloading(val progress: Int) : DownloadState
    data class Success(val file: File) : DownloadState
    data class Error(val message: UiText) : DownloadState
}

@Singleton
class ApkDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val scope: CoroutineScope
) {
    private val client = HttpClient(OkHttp)

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    fun startDownload(release: AppRelease) {
        if (_downloadState.value is DownloadState.Downloading) return

        scope.launch {
            _downloadState.value = DownloadState.Downloading(0)

            try {
                val defaultAbi = "universal"
                val supportedAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: defaultAbi

                // Find the best asset
                val asset =
                    release.assets.find { it.name.contains(supportedAbi, ignoreCase = true) }
                        ?: release.assets.find { it.name.contains("universal", ignoreCase = true) }
                        ?: release.assets.firstOrNull { it.name.endsWith(".apk") }

                if (asset == null) {
                    val availableAssets = release.assets.joinToString { it.name }
                    _downloadState.value =
                        DownloadState.Error(
                            UiText.StringResource(
                                it.attendance100.mybicocca.R.string.apk_downloader_no_suitable_apk,
                                supportedAbi,
                                availableAssets
                            )
                        )
                    return@launch
                }

                // Never hand a plaintext-fetched binary to the installer
                if (!asset.downloadUrl.startsWith("https://", ignoreCase = true)) {
                    _downloadState.value =
                        DownloadState.Error(UiText.StringResource(it.attendance100.mybicocca.R.string.apk_downloader_insecure_connection))
                    return@launch
                }

                val updatesDir = File(context.cacheDir, "updates")
                if (!updatesDir.exists()) updatesDir.mkdirs()
                val apkFile = File(updatesDir, asset.name)

                if (apkFile.exists() && apkFile.passesIntegrityCheck(asset)) {
                    // Already downloaded and verified; play the progress animation for UX
                    for (i in 0..100 step 10) {
                        _downloadState.value = DownloadState.Downloading(i)
                        delay(100.milliseconds)
                    }
                    delay(500.milliseconds)
                    _downloadState.value = DownloadState.Success(apkFile)
                    return@launch
                }

                downloadToFile(asset, apkFile)

                if (!apkFile.passesIntegrityCheck(asset)) {
                    apkFile.delete()
                    _downloadState.value =
                        DownloadState.Error(UiText.StringResource(it.attendance100.mybicocca.R.string.apk_downloader_integrity_check_failed))
                    return@launch
                }

                _downloadState.value = DownloadState.Downloading(100)
                _downloadState.value = DownloadState.Success(apkFile)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                _downloadState.value =
                    DownloadState.Error(e.message?.let { UiText.DynamicString(it) }
                        ?: UiText.StringResource(it.attendance100.mybicocca.R.string.apk_downloader_failed))
            }
        }
    }

    /**
     * Streams [asset] to [target] in fixed-size chunks, publishing download progress as it goes.
     */
    private suspend fun downloadToFile(asset: AppReleaseAsset, target: File) {
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
                                _downloadState.value = DownloadState.Downloading(progress)
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
        _downloadState.value = DownloadState.Idle
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
     * Installs a downloaded APK.
     * When [silent] is false, or on older Android versions where silent installs aren't supported,
     * fires the system installer UI via `ACTION_VIEW`.
     * When [silent] is true and the device runs API 31+, uses the `PackageInstaller` session API
     * with `USER_ACTION_NOT_REQUIRED` to install the update in the background.
     */
    suspend fun installApk(file: File, silent: Boolean) {
        if (silent && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            withContext(kotlinx.coroutines.Dispatchers.IO) {
                installSilently(file)
            }
        } else {
            installWithSystemUi(file)
        }
    }

    private fun installWithSystemUi(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    private suspend fun installSilently(file: File) {
        val packageInstaller = context.packageManager.packageInstaller
        val params = android.content.pm.PackageInstaller.SessionParams(
            android.content.pm.PackageInstaller.SessionParams.MODE_FULL_INSTALL
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setRequireUserAction(android.content.pm.PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
            }
        }
        
        var sessionId = -1
        try {
            sessionId = packageInstaller.createSession(params)
            val session = packageInstaller.openSession(sessionId)

            session.openWrite("package", 0, file.length()).use { out ->
                file.inputStream().use { input ->
                    input.copyTo(out)
                }
                session.fsync(out)
            }

            val intent = Intent(context, InstallResultReceiver::class.java)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_MUTABLE
            } else {
                android.app.PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = android.app.PendingIntent.getBroadcast(context, sessionId, intent, flags)

            session.commit(pendingIntent.intentSender)
            session.close()
        } catch (e: Exception) {
            e.printStackTrace()
            if (sessionId != -1) {
                try {
                    packageInstaller.abandonSession(sessionId)
                } catch (ignored: Exception) {}
            }
            _downloadState.value = DownloadState.Error(
                UiText.StringResource(it.attendance100.mybicocca.R.string.apk_downloader_failed)
            )
        }
    }

    fun onInstallResult(status: Int, message: String?) {
        if (status == android.content.pm.PackageInstaller.STATUS_SUCCESS) {
            _downloadState.value = DownloadState.Idle
        } else {
            val errorMsg = message ?: "Install failed (code $status)"
            _downloadState.value = DownloadState.Error(UiText.DynamicString(errorMsg))
        }
    }

    companion object {
        private const val DOWNLOAD_CHUNK_BYTES: Long = 1L * 1024 * 1024
    }
}
