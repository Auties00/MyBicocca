package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.state.ZipEntryItem
import java.util.Locale

// Lists archive entries in-app; tapping one extracts it to cache and re-dispatches it
// through a nested viewer, so a pdf inside a zip opens like any other pdf. A long-press
// ([onLongOpenEntry]) re-shows the in-app/external chooser even when a choice was already
// remembered, matching the long-press on a course content row.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ZipViewerContent(
    entries: List<ZipEntryItem>,
    onOpenEntry: (ZipEntryItem) -> Unit,
    onLongOpenEntry: (ZipEntryItem) -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) {
        ViewerError(message = "L'archivio è vuoto o non leggibile.", modifier = modifier)
        return
    }
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
        ) {
            itemsIndexed(entries, key = { _, entry -> entry.entryPath }) { _, entry ->
                ZipEntryRow(
                    entry = entry,
                    onClick = { onOpenEntry(entry) },
                    onLongClick = { onLongOpenEntry(entry) },
                )
            }
        }
        ViewerBottomBar(
            primary = ViewerAction(
                icon = Icons.Outlined.Download,
                label = "Scarica",
                onClick = onDownload,
            ),
            ViewerAction(
                icon = Icons.Outlined.Share,
                label = "Condividi",
                onClick = onShare,
            ),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ZipEntryRow(entry: ZipEntryItem, onClick: () -> Unit, onLongClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)
    Surface(
        shape = shape,
        color = scheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = entry.icon(),
                contentDescription = null,
                tint = scheme.onSecondaryContainer,
                modifier = Modifier.size(22.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val folder = entry.entryPath.substringBeforeLast('/', "")
                val meta = listOfNotNull(
                    folder.takeIf { it.isNotBlank() },
                    formatEntrySize(entry.sizeBytes),
                ).joinToString(" · ")
                if (meta.isNotBlank()) {
                    Spacer(Modifier.height(1.dp))
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private fun ZipEntryItem.icon(): ImageVector {
    val ext = displayName.substringAfterLast('.', "").lowercase(Locale.ROOT)
    return when (ext) {
        "pdf" -> Icons.Outlined.PictureAsPdf
        "png", "jpg", "jpeg", "gif", "webp", "svg", "bmp" -> Icons.Outlined.Image
        "mp4", "mp3", "m4a", "webm", "mov", "wav" -> Icons.Outlined.PlayCircleOutline
        "java", "py", "sql", "c", "cpp", "h", "hpp", "js", "ts", "kt", "json", "xml", "sh" ->
            Icons.Outlined.Code
        else -> Icons.AutoMirrored.Outlined.InsertDriveFile
    }
}

private fun formatEntrySize(bytes: Long): String? = when {
    bytes <= 0 -> null
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format(Locale.ITALIAN, "%.1f MB", bytes / (1024.0 * 1024.0))
}
