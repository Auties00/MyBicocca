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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.LocalPipController
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.navigation.route.AppRoute
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.CodeViewerContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.HtmlViewerContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.ImageViewerContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.MediaViewerContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.PdfViewerContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.UnknownFileContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.ViewerError
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.ViewerLoading
import it.attendance100.mybicocca.ui.component.file.FileKind
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.ZipViewerContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.state.FileViewerOneShotEvent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Full-screen in-app viewer for a course file, with a Custom-Tab-style chrome: a top bar with
 * the file name and a close (✕). All viewer-specific actions (share, download, PiP, …) live in
 * each viewer's own bottom action bar so the chrome stays minimal and consistent. In PiP the
 * chrome collapses, leaving only the content.
 *
 * The body routes on [FileViewerViewModel.kind] to the per-kind viewer (pdf, image, media, html,
 * code, zip, generic hand-off). Viewers that render from disk are gated on the download: a
 * loading indicator (captioned with the expected size) while the fetch runs, an error state with
 * retry on failure. One-shot events from the ViewModel drive intent launches (office protocol,
 * external app, share sheet), result snackbars, and — via [onOpenFile] — nested viewers for
 * zip-extracted entries.
 */
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

    val downloadFailedMessage = stringResource(R.string.elearning_file_download_failed)
    val noAppMessage = stringResource(R.string.elearning_file_no_app)
    val shareFailedMessage = stringResource(R.string.elearning_file_share_failed)
    val fileSavedMessage = stringResource(R.string.elearning_file_saved)
    val storageDeniedMessage = stringResource(R.string.elearning_file_storage_denied)

    LaunchedEffect(viewModel) {
        viewModel.oneShotEvents.collectLatest { event ->
            when (event) {
                is FileViewerOneShotEvent.DownloadFailed ->
                    snackbar.showError(downloadFailedMessage, event.cause)

                is FileViewerOneShotEvent.LaunchOfficeUri -> {
                    if (!launchOfficeUri(context, event.uri, event.app)) {
                        snackbar.showError(
                            context.getString(R.string.elearning_file_open_app_failed, event.app.label),
                        )
                    }
                }

                is FileViewerOneShotEvent.OpenWithExternalApp -> {
                    if (!launchExternalViewer(context, event.localPath, event.mimeType)) {
                        snackbar.showError(noAppMessage)
                    }
                }

                is FileViewerOneShotEvent.ShareFile -> {
                    if (!shareFile(context, event.localPath, event.fileName, event.mimeType)) {
                        snackbar.showError(shareFailedMessage)
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
                    snackbar.showInfo(fileSavedMessage)
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

/**
 * Top bar of the viewer: close button plus single-line file name on a surface container,
 * sized as status bar inset + 8dp gap + 56dp row to match the global top bar's height.
 */
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
                .padding(top = 8.dp)
                .height(56.dp)
                .padding(start = 4.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.common_close))
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

/**
 * Routes a [FileKind] to its viewer composable and wires the per-viewer actions.
 *
 * Saving to the public Downloads/Pictures directories needs WRITE_EXTERNAL_STORAGE on API < 29,
 * while Q+ writes through MediaStore scoped storage and needs no permission: the requested save
 * is held and runs once the grant resolves. Share and open-with use a FileProvider on the cache
 * dir, so they never need the permission.
 *
 * Office kinds are intercepted by the shell (hand-off sheet / install prompt) and never navigate
 * here; the Office branch exists only defensively and falls back to the generic hand-off content
 * together with [FileKind.Unknown].
 */
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
    val pdfThemeMode by viewModel.pdfThemeMode.collectAsStateWithLifecycle()
    val pdfOrientation by viewModel.pdfOrientation.collectAsStateWithLifecycle()

    val storageDeniedMessage = stringResource(R.string.elearning_file_storage_denied)
    var pendingSave by remember { mutableStateOf<(() -> Unit)?>(null) }
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val save = pendingSave
        pendingSave = null
        if (granted) save?.invoke()
        else scope.launch { snackbar.showError(storageDeniedMessage) }
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
                themeMode = pdfThemeMode,
                orientation = pdfOrientation,
                onThemeModeChange = viewModel::setPdfThemeMode,
                onOrientationChange = viewModel::setPdfOrientation,
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

/**
 * Renders the download lifecycle around a viewer that needs the resolved value:
 * spinner while the fetch runs, error + retry on failure, content once loaded.
 */
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
                message = stringResource(R.string.elearning_file_download_failed_detail),
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
    else -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
}
