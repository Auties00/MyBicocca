package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.navigation.AppRoute
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.CodeViewerContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.HtmlViewerContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.ImageViewerContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.MediaViewerContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.OfficeViewerContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.PdfViewerContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.UnknownFileContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.ViewerError
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.ViewerLoading
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component.ZipViewerContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.state.FileKind
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.state.FileViewerOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.state.OfficeApp
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.util.Locale

@Composable
fun FileViewerScreen(
    onOpenFile: (AppRoute.FileViewer) -> Unit,
    viewModel: FileViewerViewModel,
) {
    val context = LocalContext.current
    val snackbar = LocalAppSnackbarController.current

    val localPath by viewModel.localPath.collectAsStateWithLifecycle()
    val mediaUri by viewModel.mediaUri.collectAsStateWithLifecycle()
    val downloadStatus by viewModel.downloadStatus.collectAsStateWithLifecycle()
    val zipEntries by viewModel.zipEntries.collectAsStateWithLifecycle()

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

    when (val kind = viewModel.kind) {
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

        is FileKind.Office -> OfficeViewerContent(
            app = kind.app,
            fileName = viewModel.fileName,
            sizeBytes = viewModel.sizeBytes,
            onOpenInOffice = viewModel::openInOffice,
            onInstallOffice = { launchPlayStore(context, kind.app.packageName) },
            onOpenWithOtherApp = viewModel::openWithExternalApp,
        )

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

// Microsoft's documented deep link: "<protocol>:ofv|u|<url>" forces view-only mode and
// lets the Office app download the (tokenized) URL itself. Target the package explicitly
// so a rogue app registering the scheme can't intercept the token-bearing URL.
private fun launchOfficeUri(context: Context, uri: String, app: OfficeApp): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        .setPackage(app.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return runCatching { context.startActivity(intent) }.isSuccess
}

private fun launchPlayStore(context: Context, packageName: String): Boolean {
    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val web = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return runCatching { context.startActivity(market) }
        .recoverCatching { context.startActivity(web) }
        .isSuccess
}

private fun launchExternalViewer(context: Context, localPath: String, mimeType: String?): Boolean {
    val file = File(localPath)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val resolvedMime = mimeType
        ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            file.extension.lowercase(Locale.ROOT),
        )
        ?: "*/*"
    val intent = Intent(Intent.ACTION_VIEW)
        .setDataAndType(uri, resolvedMime)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    return try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}

private fun formatDownloadSize(bytes: Long?): String? = when {
    bytes == null || bytes <= 0 -> null
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format(Locale.ITALIAN, "%.1f MB", bytes / (1024.0 * 1024.0))
}
