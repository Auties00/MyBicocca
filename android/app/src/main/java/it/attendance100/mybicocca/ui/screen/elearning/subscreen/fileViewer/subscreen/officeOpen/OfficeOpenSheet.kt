package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.subscreen.officeOpen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Slideshow
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.navigation.route.AppRoute
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.FileViewerViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.launchExternalViewer
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.launchOfficeUri
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.launchPlayStore
import it.attendance100.mybicocca.ui.component.file.OfficeApp
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.state.FileViewerOneShotEvent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Hand-off sheet for Office documents. Office formats have no free native renderer on Android,
 * so opening one is a hand-off, not a viewing experience — hence a modal over the requesting
 * screen rather than a viewer page. The document goes to Microsoft's app through their
 * documented ms-* protocol in forced view-only mode; when the app is missing, the primary
 * action becomes a Play Store install, and the install check re-runs on resume so coming back
 * from the store flips the CTA to "open". The secondary action downloads the file and hands it
 * to any other app.
 *
 * Visually: a brand-colored hero shape with the family icon, the file name, a document-type +
 * size chip, an explanatory line, and a pinned connected button pair in the app's shared
 * sheet-action shape language (brand-filled leading, tonal trailing with an inline progress
 * indicator while downloading).
 *
 * Reuses the file-viewer ViewModel, which owns the office hand-off paths (protocol launch, lazy
 * download for the external-app fallback). It is keyed per file because the sheet lives in the
 * shell's store, which would otherwise hand back the instance created for the first file.
 * Zip-extraction, share, and save events never originate from this sheet and are ignored.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OfficeOpenSheet(
    app: OfficeApp,
    route: AppRoute.FileViewer,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val snackbar = LocalAppSnackbarController.current
    val scope = rememberCoroutineScope()
    val viewModel = hiltViewModel<FileViewerViewModel, FileViewerViewModel.Factory>(
        key = "office:${route.fileUrl ?: route.localPath ?: route.fileName}",
        creationCallback = { it.create(route) },
    )
    val downloadStatus by viewModel.downloadStatus.collectAsStateWithLifecycle()
    val downloading = downloadStatus is SyncStatus.Refreshing

    var installed by remember(app) { mutableStateOf(isInstalled(context, app)) }
    LifecycleResumeEffect(app) {
        installed = isInstalled(context, app)
        onPauseOrDispose { }
    }

    LaunchedEffect(viewModel) {
        viewModel.oneShotEvents.collectLatest { event ->
            when (event) {
                is FileViewerOneShotEvent.DownloadFailed ->
                    snackbar.showError("Download del file non riuscito", event.cause)

                is FileViewerOneShotEvent.LaunchOfficeUri -> {
                    if (launchOfficeUri(context, event.uri, event.app)) onDismiss()
                    else snackbar.showError("Impossibile aprire ${event.app.label}")
                }

                is FileViewerOneShotEvent.OpenWithExternalApp -> {
                    if (launchExternalViewer(context, event.localPath, event.mimeType)) onDismiss()
                    else snackbar.showError("Nessuna app installata può aprire questo file")
                }

                is FileViewerOneShotEvent.OpenExtractedFile,
                is FileViewerOneShotEvent.ShareFile,
                FileViewerOneShotEvent.FileSaved -> Unit
            }
        }
    }

    it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet(
        onDismiss = onDismiss,
    ) { _, _ ->
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(app.heroShape().toShape())
                        .background(app.brandColor()),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = app.icon(),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(52.dp),
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    text = route.fileName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Text(
                        text = listOfNotNull(app.documentLabel(), formatSize(route.sizeBytes))
                            .joinToString(" · "),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = if (installed) {
                        "Si apre in ${app.label} in sola lettura."
                    } else {
                        "Per aprire questo documento serve l'app ${app.label} di Microsoft, gratuita sul Play Store."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Button(
                    onClick = {
                        if (installed) viewModel.openInOffice()
                        else if (!launchPlayStore(context, app.packageName)) {
                            scope.launch { snackbar.showError("Impossibile aprire il Play Store") }
                        }
                    },
                    modifier = Modifier
                        .weight(1.4f)
                        .height(56.dp),
                    shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = app.brandColor(),
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(
                        imageVector = if (installed) {
                            Icons.AutoMirrored.Outlined.OpenInNew
                        } else {
                            Icons.Outlined.Download
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (installed) "Apri in ${app.label}" else "Installa ${app.label}",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                FilledTonalButton(
                    onClick = viewModel::openWithExternalApp,
                    enabled = !downloading,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    if (downloading) {
                        LoadingIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Apps,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Altra app", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

private fun isInstalled(context: Context, app: OfficeApp): Boolean =
    runCatching { context.packageManager.getPackageInfo(app.packageName, 0) }.isSuccess

private fun OfficeApp.icon() = when (this) {
    OfficeApp.Word -> Icons.Outlined.Description
    OfficeApp.Excel -> Icons.Outlined.TableChart
    OfficeApp.PowerPoint -> Icons.Outlined.Slideshow
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun OfficeApp.heroShape() = when (this) {
    OfficeApp.Word -> MaterialShapes.Cookie9Sided
    OfficeApp.Excel -> MaterialShapes.Clover4Leaf
    OfficeApp.PowerPoint -> MaterialShapes.Sunny
}

/**
 * Microsoft brand hues — fixed colors on neutral surfaces, safe in both themes. Content drawn
 * on them must be explicit white: a theme-reactive onPrimary would flip dark in dark mode.
 */
private fun OfficeApp.brandColor() = when (this) {
    OfficeApp.Word -> Color(0xFF185ABD)
    OfficeApp.Excel -> Color(0xFF107C41)
    OfficeApp.PowerPoint -> Color(0xFFC43E1C)
}

private fun OfficeApp.documentLabel() = when (this) {
    OfficeApp.Word -> "Documento Word"
    OfficeApp.Excel -> "Foglio di calcolo Excel"
    OfficeApp.PowerPoint -> "Presentazione PowerPoint"
}

private fun formatSize(bytes: Long?): String? = when {
    bytes == null || bytes <= 0 -> null
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format(Locale.ITALIAN, "%.1f MB", bytes / (1024.0 * 1024.0))
}
