package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Slideshow
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.state.OfficeApp
import java.util.Locale

// Office formats have no free native renderer on Android; this hands the document to
// Microsoft's apps through their documented ms-* protocol in forced view-only mode.
// When the matching app is missing, the primary action becomes a Play Store install.
@Composable
fun OfficeViewerContent(
    app: OfficeApp,
    fileName: String,
    sizeBytes: Long?,
    onOpenInOffice: () -> Unit,
    onInstallOffice: () -> Unit,
    onOpenWithOtherApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val installed = remember(app) {
        runCatching { context.packageManager.getPackageInfo(app.packageName, 0) }.isSuccess
    }
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(
                imageVector = app.icon(),
                contentDescription = null,
                tint = app.brandColor(),
                modifier = Modifier.size(72.dp),
            )
            Text(
                text = fileName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            formatOfficeSize(sizeBytes)?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = if (installed) {
                    "Questo documento si apre in ${app.label} in sola lettura."
                } else {
                    "Per visualizzare questo documento serve ${app.label}."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            Button(
                onClick = if (installed) onOpenInOffice else onInstallOffice,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(if (installed) "Apri in ${app.label}" else "Installa ${app.label}")
            }
            TextButton(onClick = onOpenWithOtherApp) {
                Text("Apri con un'altra app")
            }
        }
    }
}

@Composable
fun UnknownFileContent(
    fileName: String,
    sizeBytes: Long?,
    downloading: Boolean,
    onOpenWith: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.InsertDriveFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(72.dp),
            )
            Text(
                text = fileName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            formatOfficeSize(sizeBytes)?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "Questo tipo di file non ha un'anteprima integrata.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            Button(
                onClick = onOpenWith,
                enabled = !downloading,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(if (downloading) "Download in corso…" else "Apri con un'altra app")
            }
        }
    }
}

private fun OfficeApp.icon() = when (this) {
    OfficeApp.Word -> Icons.Outlined.Description
    OfficeApp.Excel -> Icons.Outlined.TableChart
    OfficeApp.PowerPoint -> Icons.Outlined.Slideshow
}

// Microsoft brand hues; fixed colors on neutral surfaces, so safe in both themes.
private fun OfficeApp.brandColor() = when (this) {
    OfficeApp.Word -> Color(0xFF185ABD)
    OfficeApp.Excel -> Color(0xFF107C41)
    OfficeApp.PowerPoint -> Color(0xFFC43E1C)
}

private fun formatOfficeSize(bytes: Long?): String? = when {
    bytes == null || bytes <= 0 -> null
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format(Locale.ITALIAN, "%.1f MB", bytes / (1024.0 * 1024.0))
}
