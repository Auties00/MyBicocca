package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.subscreen.folderContents

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.elearning.course.ModuleContent
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import java.util.Locale

// Folder modules (and the rare multi-file resource) hold several files; this sheet picks
// one before handing off to the file viewer. A long-press re-shows the in-app/external chooser
// for that file even when a choice was already remembered (forceChooser), mirroring the
// long-press on a single-file module row.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderContentsSheet(
    title: String,
    contents: List<ModuleContent>,
    onOpenContent: (content: ModuleContent, forceChooser: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    PredictiveModalBottomSheet(onDismiss = onDismiss) { _, _ ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
            )
            LazyColumn {
                itemsIndexed(contents) { _, content ->
                    FileRow(
                        content = content,
                        onClick = { onOpenContent(content, false); onDismiss() },
                        onLongClick = { onOpenContent(content, true); onDismiss() },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileRow(content: ModuleContent, onClick: () -> Unit, onLongClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = content.icon(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = content.fileName ?: "File",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            formatSize(content.sizeBytes)?.let {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun ModuleContent.icon(): ImageVector {
    val mime = mimeType.orEmpty()
    val ext = fileName.orEmpty().substringAfterLast('.', "").lowercase(Locale.ROOT)
    return when {
        mime == "application/pdf" || ext == "pdf" -> Icons.Outlined.PictureAsPdf
        mime.startsWith("image/") -> Icons.Outlined.Image
        mime.startsWith("video/") || mime.startsWith("audio/") -> Icons.Outlined.PlayCircleOutline
        mime.startsWith("text/") && ext != "txt" -> Icons.Outlined.Code
        else -> Icons.AutoMirrored.Outlined.InsertDriveFile
    }
}

private fun formatSize(bytes: Long?): String? = when {
    bytes == null || bytes <= 0 -> null
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format(Locale.ITALIAN, "%.1f MB", bytes / (1024.0 * 1024.0))
}
