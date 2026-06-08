package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.navigation.AppRoute
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.CodeViewerContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.HtmlViewerContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.ImageViewerContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.MediaViewerContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.PdfViewerContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.UnknownFileContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.ViewerError
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.ViewerLoading
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.ZipViewerContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.state.FileKind
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.state.FileViewerOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.player.LocalPipController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

// Opens an in-app file with a Custom-Tab-style chrome: a top bar with the file name and a
// close (✕). All viewer-specific actions (share, download, PiP, …) live in each viewer's own
// bottom action bar so the chrome stays minimal and consistent. In PiP the chrome collapses.
@Composable
fun FileViewerScreen(
    onOpenFile: (AppRoute.FileViewer, forceChooser: Boolean) -> Unit,
    onClose: () -> Unit = {},
    viewModel: FileViewerViewModel,
) {
    val context = LocalContext.current
    val snackbar = LocalAppSnackbarController.current

    val localPath by viewModel.localPath.collectAsStateWithLifecycle()
    val mediaUri by viewModel.mediaUri.collectAsStateWithLifecycle()
    val downloadStatus by viewModel.downloadStatus.collectAsStateWithLifecycle()
    val zipEntries by viewModel.zipEntries.collectAsStateWithLifecycle()
    val pipController = LocalPipController.current
    val inPip by pipController.isInPip

    LaunchedEffect(viewModel) {
        viewModel.oneShotEvents.collectLatest { event ->
            when (event) {
                is FileViewerOneShotEvent.DownloadFailed ->
                    snackbar.showError("Download del file non riuscito", event.cause)

                is FileViewerOneShotEvent.LaunchOfficeUri -> {
                    if (!launchOfficeUri(context, event.uri, event.app)) {
                        snackbar.showError("Impossibile aprire ${event.app.label}")
                    }
                }

                is FileViewerOneShotEvent.OpenWithExternalApp -> {
                    if (!launchExternalViewer(context, event.localPath, event.mimeType)) {
                        snackbar.showError("Nessuna app installata può aprire questo file")
                    }
                }

                is FileViewerOneShotEvent.ShareFile -> {
                    if (!shareFile(context, event.localPath, event.fileName, event.mimeType)) {
                        snackbar.showError("Impossibile condividere il file")
                    }
                }

                is FileViewerOneShotEvent.OpenExtractedFile -> onOpenFile(
                    AppRoute.FileViewer(
                        fileName = event.fileName,
                        localPath = event.localPath,
                        mimeType = event.mimeType,
                        sizeBytes = event.sizeBytes,
                    ),
                    event.forceChooser,
                )

                FileViewerOneShotEvent.FileSaved ->
                    snackbar.showInfo("File salvato")
            }
        }
    }

    val kind = viewModel.kind
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        if (!inPip) {
            FileViewerChrome(
                fileName = viewModel.fileName,
                onClose = onClose,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            FileContent(
                kind = kind,
                viewModel = viewModel,
                localPath = localPath,
                mediaUri = mediaUri,
                downloadStatus = downloadStatus,
                zipEntries = zipEntries,
            )
        }
    }
}

@Composable
private fun FileViewerChrome(
    fileName: String,
    onClose: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(color = scheme.surfaceContainer, contentColor = scheme.onSurface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                // status bar + 8dp gap + 56dp row to match the global top bar's height.
                .padding(top = 8.dp)
                .height(56.dp)
                .padding(start = 4.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Outlined.Close, contentDescription = "Chiudi")
            }
            Text(
                text = fileName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
            )
        }
    }
}

@Composable
private fun FileContent(
    kind: FileKind,
    viewModel: FileViewerViewModel,
    localPath: Loadable<String>,
    mediaUri: Loadable<String>,
    downloadStatus: SyncStatus,
    zipEntries: Loadable<List<it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.state.ZipEntryItem>>,
) {
    val pipController = LocalPipController.current
    val context = LocalContext.current
    val snackbar = LocalAppSnackbarController.current
    val scope = rememberCoroutineScope()

    // Saving to the public Downloads/Pictures dirs needs WRITE_EXTERNAL_STORAGE on API < 29;
    // Q+ writes through MediaStore scoped storage and needs no permission. Hold the requested
    // save and run it once the grant resolves. (Share / open-with use a FileProvider on the
    // cache dir, so they never need this.)
    var pendingSave by remember { mutableStateOf<(() -> Unit)?>(null) }
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val save = pendingSave
        pendingSave = null
        if (granted) save?.invoke()
        else scope.launch { snackbar.showError("Permesso di archiviazione negato") }
    }
    val saveWithPermission: (() -> Unit) -> Unit = { save ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            save()
        } else {
            pendingSave = save
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    when (kind) {
        FileKind.Pdf -> LocalFileGate(localPath, downloadStatus, viewModel.sizeBytes, viewModel::retry) { path ->
            PdfViewerContent(
                localPath = path,
                onShare = viewModel::shareFile,
                onDownload = { saveWithPermission(viewModel::saveToDownloads) },
                onPip = { pipController.enterPipNow() },
            )
        }

        FileKind.Image -> LocalFileGate(localPath, downloadStatus, viewModel.sizeBytes, viewModel::retry) { path ->
            ImageViewerContent(
                localPath = path,
                fileName = viewModel.fileName,
                onShare = viewModel::shareFile,
                onSaveToGallery = { saveWithPermission(viewModel::saveToGallery) },
            )
        }

        FileKind.Video, FileKind.Audio -> LocalFileGate(mediaUri, downloadStatus, viewModel.sizeBytes, viewModel::retry) { uri ->
            MediaViewerContent(
                mediaUri = uri,
                isVideo = kind == FileKind.Video,
                fileName = viewModel.fileName,
                onShare = viewModel::shareFile,
            )
        }

        FileKind.Html -> LocalFileGate(localPath, downloadStatus, viewModel.sizeBytes, viewModel::retry) { path ->
            HtmlViewerContent(
                localPath = path,
                onShare = viewModel::shareFile,
                onDownload = { saveWithPermission(viewModel::saveToDownloads) },
                onOpenInBrowser = viewModel::openWithExternalApp,
            )
        }

        FileKind.Text -> {
            val darkTheme = isSystemInDarkTheme()
            LocalFileGate(localPath, downloadStatus, viewModel.sizeBytes, viewModel::retry) { path ->
                CodeViewerContent(
                    localPath = path,
                    fileName = viewModel.fileName,
                    darkTheme = darkTheme,
                    onShare = viewModel::shareFile,
                    onDownload = { saveWithPermission(viewModel::saveToDownloads) },
                )
            }
        }

        FileKind.Zip -> LocalFileGate(localPath, downloadStatus, viewModel.sizeBytes, viewModel::retry) {
            when (val entries = zipEntries) {
                Loadable.NotYetLoaded -> ViewerLoading()
                is Loadable.Loaded -> ZipViewerContent(
                    entries = entries.value,
                    onOpenEntry = viewModel::openZipEntry,
                    onLongOpenEntry = { viewModel.openZipEntry(it, forceChooser = true) },
                    onShare = viewModel::shareFile,
                    onDownload = { saveWithPermission(viewModel::saveToDownloads) },
                )
            }
        }

        // Office (install prompt / app) is intercepted by the shell and never navigates here.
        // Unknown only reaches here defensively: generic hand-off.
        is FileKind.Office,
        FileKind.Unknown -> UnknownFileContent(
            fileName = viewModel.fileName,
            sizeBytes = viewModel.sizeBytes,
            downloading = downloadStatus is SyncStatus.Refreshing,
            onOpenWith = viewModel::openWithExternalApp,
            onShare = viewModel::shareFile,
        )
    }
}

// Renders the download lifecycle around a viewer that needs the resolved value:
// spinner while the fetch runs, error + retry on failure, content once loaded.
@Composable
private fun LocalFileGate(
    value: Loadable<String>,
    status: SyncStatus,
    sizeBytes: Long?,
    onRetry: () -> Unit,
    content: @Composable (String) -> Unit,
) {
    when (value) {
        is Loadable.Loaded -> content(value.value)
        Loadable.NotYetLoaded -> when (status) {
            is SyncStatus.Failed -> ViewerError(
                message = "Download del file non riuscito.",
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize(),
            )
            else -> ViewerLoading(
                label = formatDownloadSize(sizeBytes),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private fun formatDownloadSize(bytes: Long?): String? = when {
    bytes == null || bytes <= 0 -> null
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format(Locale.ITALIAN, "%.1f MB", bytes / (1024.0 * 1024.0))
}
