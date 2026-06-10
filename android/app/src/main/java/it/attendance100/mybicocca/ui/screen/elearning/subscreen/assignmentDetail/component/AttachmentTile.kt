package it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import java.util.Locale

/**
 * A file as a connected segment in the plan compiler's course-tile language: leading
 * extension chip and name with the size as caption. Tapping the tile opens the file; a
 * long-press ([onLongOpen]) re-shows the in-app/external chooser even when a choice was
 * already remembered, mirroring the long-press on a course content row.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AttachmentTile(
    file: Assignment.AttachmentRef,
    onOpen: () -> Unit,
    onLongOpen: () -> Unit,
    isLast: Boolean,
    modifier: Modifier = Modifier,
    isFirst: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = segmentShape(isFirst = isFirst, isLast = isLast)
    val extension = file.fileName.substringAfterLast('.', missingDelimiterValue = "")
        .uppercase(Locale.ITALIAN).take(4).ifBlank { "FILE" }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .combinedClickable(onClick = onOpen, onLongClick = onLongOpen),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = shape,
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(scheme.primaryContainer, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = extension,
                    color = scheme.onPrimaryContainer,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.5.sp,
                    letterSpacing = 0.4.sp,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.fileName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val sizeLabel = file.sizeBytes?.let(::formatFileSize)
                if (sizeLabel != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = sizeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1_048_576L -> String.format(Locale.ITALIAN, "%.1f MB", bytes / 1_048_576.0)
    bytes >= 1_024L -> String.format(Locale.ITALIAN, "%.0f KB", bytes / 1_024.0)
    else -> "$bytes B"
}
