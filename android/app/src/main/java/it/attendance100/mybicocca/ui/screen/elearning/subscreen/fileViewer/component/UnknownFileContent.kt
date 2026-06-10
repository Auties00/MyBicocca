package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Locale

/**
 * Fallback content for files without an in-app preview: a centered document icon, the file name
 * and size, and a note that no integrated preview exists. The bottom action bar offers open-with
 * and share, both disabled — with the primary label switched to a download-in-progress message —
 * while the file is being fetched.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UnknownFileContent(
    fileName: String,
    sizeBytes: Long?,
    downloading: Boolean,
    onOpenWith: () -> Unit,
    onShare: () -> Unit,
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
            formatSize(sizeBytes)?.let {
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
        }
        ViewerBottomBar(
            primary = ViewerAction(
                icon = Icons.AutoMirrored.Outlined.OpenInNew,
                label = if (downloading) "Download in corso…" else "Apri con",
                onClick = onOpenWith,
                enabled = !downloading,
            ),
            ViewerAction(
                icon = Icons.Outlined.Share,
                label = "Condividi",
                onClick = onShare,
                enabled = !downloading,
            ),
        )
    }
}

private fun formatSize(bytes: Long?): String? = when {
    bytes == null || bytes <= 0 -> null
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format(Locale.ITALIAN, "%.1f MB", bytes / (1024.0 * 1024.0))
}
