package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import java.util.Locale

// Opens an in-app file with a Custom-Tab-style chrome: a top bar with the file name, a close
// (✕), a share action, and — for video — a Picture-in-Picture button. In PiP the chrome (and
// the video's own controls) collapse to just the video.
@Composable
fun FileViewerScreen(
    onOpenFile: (AppRoute.FileViewer) -> Unit,
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
                )
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
                onShare = viewModel::shareFile,
                // Available for every file; only media auto-enters PiP when you leave the app.
                onPip = { pipController.enterPipNow() },
            )
        }
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
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
    onShare: () -> Unit,
    onPip: (() -> Unit)?,
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
                .padding(start = 4.dp, end = 8.dp),
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
                    .padding(start = 4.dp, end = 8.dp),
            )
            if (onPip != null) {
                IconButton(onClick = onPip) {
                    Icon(Icons.Outlined.PictureInPictureAlt, contentDescription = "Picture-in-picture")
                }
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Outlined.Share, contentDescription = "Condividi")
            }
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
    when (kind) {
        FileKind.Pdf -> LocalFileGate(localPath, downloadStatus, viewModel.sizeBytes, viewModel::retry) { path ->
            PdfViewerContent(localPath = path)
        }

        FileKind.Image -> LocalFileGate(localPath, downloadStatus, viewModel.sizeBytes, viewModel::retry) { path ->
            ImageViewerContent(localPath = path, fileName = viewModel.fileName)
        }

        FileKind.Video, FileKind.Audio -> LocalFileGate(mediaUri, downloadStatus, viewModel.sizeBytes, viewModel::retry) { uri ->
            MediaViewerContent(
                mediaUri = uri,
                isVideo = kind == FileKind.Video,
                fileName = viewModel.fileName,
            )
        }

        FileKind.Html -> LocalFileGate(localPath, downloadStatus, viewModel.sizeBytes, viewModel::retry) { path ->
            HtmlViewerContent(localPath = path)
        }

        FileKind.Text -> {
            val darkTheme = isSystemInDarkTheme()
            LocalFileGate(localPath, downloadStatus, viewModel.sizeBytes, viewModel::retry) { path ->
                CodeViewerContent(
                    localPath = path,
                    fileName = viewModel.fileName,
                    darkTheme = darkTheme,
                )
            }
        }

        FileKind.Zip -> LocalFileGate(localPath, downloadStatus, viewModel.sizeBytes, viewModel::retry) {
            when (val entries = zipEntries) {
                Loadable.NotYetLoaded -> ViewerLoading()
                is Loadable.Loaded -> ZipViewerContent(
                    entries = entries.value,
                    onOpenEntry = viewModel::openZipEntry,
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
