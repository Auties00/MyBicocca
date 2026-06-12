package it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.subscreen.qualityPicker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.elearning.video.VideoVariant

/**
 * Bottom sheet for picking the video stream quality: an "Automatica" row for adaptive selection
 * followed by the stream's variants from highest to lowest resolution, each labelled with its
 * height (or flavor id) plus bitrate/format details and a check mark on the selected row.
 * Choosing a row applies the quality constraint and closes the sheet.
 */
@Composable
fun QualityPickerSheet(
    variants: List<VideoVariant>,
    selected: VideoVariant?,
    onSelect: (VideoVariant?) -> Unit,
    onDismiss: () -> Unit,
) {
    it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet(onDismiss = onDismiss) { _, _ ->
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Text(
                text = stringResource(R.string.elearning_video_quality),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 8.dp),
            )
            QualityRow(
                label = stringResource(R.string.elearning_video_quality_auto),
                detail = stringResource(R.string.elearning_video_quality_auto_detail),
                isSelected = selected == null,
                onClick = { onSelect(null); onDismiss() },
            )
            variants.asReversed().forEach { variant ->
                val label = variant.heightPx?.let { "${it}p" } ?: variant.flavorId
                val detail = listOfNotNull(
                    variant.bitrateKbps?.let { "${it} kbps" },
                    variant.fileExtension?.uppercase(),
                ).joinToString(" · ").takeIf { it.isNotBlank() }
                QualityRow(
                    label = label,
                    detail = detail,
                    isSelected = selected?.flavorId == variant.flavorId,
                    onClick = { onSelect(variant); onDismiss() },
                )
            }
        }
    }
}

@Composable
private fun QualityRow(
    label: String,
    detail: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            if (!detail.isNullOrBlank()) {
                Text(
                    text = detail,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
