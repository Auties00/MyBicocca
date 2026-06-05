package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.usecase.elearning.file.DownloadCourseFileUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.file.GetAuthenticatedFileUrlUseCase
import it.attendance100.mybicocca.ui.navigation.AppRoute
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.state.FileKind
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.state.FileViewerOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.state.ZipEntryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipFile

@HiltViewModel(assistedFactory = FileViewerViewModel.Factory::class)
class FileViewerViewModel @AssistedInject constructor(
    @Assisted private val key: AppRoute.FileViewer,
    @ApplicationContext private val context: Context,
    private val downloadCourseFile: DownloadCourseFileUseCase,
    private val getAuthenticatedFileUrl: GetAuthenticatedFileUrlUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(key: AppRoute.FileViewer): FileViewerViewModel
    }

    val fileName: String = key.fileName
    val mimeType: String? = key.mimeType
    val sizeBytes: Long? = key.sizeBytes
    val kind: FileKind = FileKind.classify(key.fileName, key.mimeType)

    // Absolute path of the local copy, for kinds that render from disk.
    private val _localPath = MutableStateFlow<Loadable<String>>(Loadable.NotYetLoaded)
    val localPath: StateFlow<Loadable<String>> = _localPath.asStateFlow()

    // What the media player should play: the tokenized remote URL (pluginfile honors
    // Range requests, so streaming + seeking works without a download) or a local path.
    private val _mediaUri = MutableStateFlow<Loadable<String>>(Loadable.NotYetLoaded)
    val mediaUri: StateFlow<Loadable<String>> = _mediaUri.asStateFlow()

    private val _downloadStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val downloadStatus: StateFlow<SyncStatus> = _downloadStatus.asStateFlow()

    private val _zipEntries = MutableStateFlow<Loadable<List<ZipEntryItem>>>(Loadable.NotYetLoaded)
    val zipEntries: StateFlow<Loadable<List<ZipEntryItem>>> = _zipEntries.asStateFlow()

    private val oneShotChannel = Channel<FileViewerOneShotEvent>(Channel.BUFFERED)
    val oneShotEvents: Flow<FileViewerOneShotEvent> = oneShotChannel.receiveAsFlow()

    init {
        load()
    }

    fun retry() {
        load()
    }

    private fun load() {
        when (kind) {
            // Office is hand-off only — nothing to fetch until the user taps the button.
            is FileKind.Office -> Unit
            FileKind.Video, FileKind.Audio -> resolveMediaUri()
            else -> ensureLocalFile()
        }
    }

    // Office docs are opened through Microsoft's documented ms-* protocol handlers in
    // forced view-only mode (ofv). The Office app downloads the URL itself, so it gets
    // the tokenized variant; for zip-extracted local files the protocol can't help and
    // the file goes out via FileProvider instead.
    fun openInOffice() {
        val app = (kind as? FileKind.Office)?.app ?: return
        viewModelScope.launch {
            val local = key.localPath
            if (local != null) {
                oneShotChannel.trySend(FileViewerOneShotEvent.OpenWithExternalApp(local, mimeType))
                return@launch
            }
            val url = key.fileUrl ?: return@launch
            runCatching { getAuthenticatedFileUrl(url) }
                .onSuccess { authenticated ->
                    oneShotChannel.trySend(
                        FileViewerOneShotEvent.LaunchOfficeUri("${app.protocol}:ofv|u|$authenticated", app),
                    )
                }
                .onFailure { oneShotChannel.trySend(FileViewerOneShotEvent.DownloadFailed(it)) }
        }
    }

    // Hands the file to an external app. Office files are never downloaded eagerly, so
    // this may have to fetch the file first.
    fun openWithExternalApp() {
        viewModelScope.launch {
            val path = when (val loaded = _localPath.value) {
                is Loadable.Loaded -> loaded.value
                Loadable.NotYetLoaded -> {
                    val url = key.fileUrl ?: return@launch
                    _downloadStatus.value = SyncStatus.Refreshing
                    runCatching { downloadCourseFile(url, key.fileName) }
                        .onSuccess {
                            _localPath.value = Loadable.Loaded(it)
                            _downloadStatus.value = SyncStatus.Idle
                        }
                        .onFailure { cause ->
                            _downloadStatus.value = SyncStatus.Failed(cause)
                            oneShotChannel.trySend(FileViewerOneShotEvent.DownloadFailed(cause))
                        }
                        .getOrNull() ?: return@launch
                }
            }
            oneShotChannel.trySend(FileViewerOneShotEvent.OpenWithExternalApp(path, mimeType))
        }
    }

    fun openZipEntry(entry: ZipEntryItem) {
        val zipPath = (_localPath.value as? Loadable.Loaded)?.value ?: return
        viewModelScope.launch {
            runCatching { extractZipEntry(zipPath, entry) }
                .onSuccess { extracted ->
                    oneShotChannel.trySend(
                        FileViewerOneShotEvent.OpenExtractedFile(
                            fileName = entry.displayName,
                            localPath = extracted,
                            mimeType = null,
                            sizeBytes = entry.sizeBytes,
                        ),
                    )
                }
                .onFailure { oneShotChannel.trySend(FileViewerOneShotEvent.DownloadFailed(it)) }
        }
    }

    private fun ensureLocalFile() {
        key.localPath?.let { existing ->
            _localPath.value = Loadable.Loaded(existing)
            onLocalFileReady(existing)
            return
        }
        val url = key.fileUrl ?: return
        viewModelScope.launch {
            _downloadStatus.value = SyncStatus.Refreshing
            runCatching { downloadCourseFile(url, key.fileName) }
                .onSuccess { path ->
                    _localPath.value = Loadable.Loaded(path)
                    _downloadStatus.value = SyncStatus.Idle
                    onLocalFileReady(path)
                }
                .onFailure { cause ->
                    _downloadStatus.value = SyncStatus.Failed(cause)
                    oneShotChannel.trySend(FileViewerOneShotEvent.DownloadFailed(cause))
                }
        }
    }

    private fun resolveMediaUri() {
        key.localPath?.let {
            _mediaUri.value = Loadable.Loaded(it)
            return
        }
        val url = key.fileUrl ?: return
        viewModelScope.launch {
            _downloadStatus.value = SyncStatus.Refreshing
            runCatching { getAuthenticatedFileUrl(url) }
                .onSuccess {
                    _mediaUri.value = Loadable.Loaded(it)
                    _downloadStatus.value = SyncStatus.Idle
                }
                .onFailure { cause ->
                    _downloadStatus.value = SyncStatus.Failed(cause)
                    oneShotChannel.trySend(FileViewerOneShotEvent.DownloadFailed(cause))
                }
        }
    }

    private fun onLocalFileReady(path: String) {
        if (kind != FileKind.Zip) return
        viewModelScope.launch {
            runCatching { listZipEntries(path) }
                .onSuccess { _zipEntries.value = Loadable.Loaded(it) }
                .onFailure { _zipEntries.value = Loadable.Loaded(emptyList()) }
        }
    }

    private suspend fun listZipEntries(path: String): List<ZipEntryItem> =
        withContext(Dispatchers.IO) {
            ZipFile(path).use { zip ->
                zip.entries().asSequence()
                    .filter { !it.isDirectory }
                    .map {
                        ZipEntryItem(
                            entryPath = it.name,
                            displayName = it.name.substringAfterLast('/'),
                            sizeBytes = it.size.coerceAtLeast(0L),
                        )
                    }
                    .sortedBy { it.entryPath.lowercase() }
                    .toList()
            }
        }

    private suspend fun extractZipEntry(zipPath: String, entry: ZipEntryItem): String =
        withContext(Dispatchers.IO) {
            val targetDir = File(File(zipPath).parentFile, "extracted")
            // Zip entry names are attacker-controlled paths; sanitize to a flat leaf so a
            // "../" entry can never escape the cache directory.
            val leaf = entry.displayName.replace(Regex("""[\\/:*?"<>|]"""), "_").ifBlank { "file" }
            val target = File(targetDir, leaf)
            if (!target.exists() || target.length() == 0L) {
                targetDir.mkdirs()
                ZipFile(zipPath).use { zip ->
                    val zipEntry = zip.getEntry(entry.entryPath)
                        ?: error("Voce non trovata nell'archivio.")
                    zip.getInputStream(zipEntry).use { input ->
                        target.outputStream().use { output -> input.copyTo(output) }
                    }
                }
            }
            target.absolutePath
        }
}
