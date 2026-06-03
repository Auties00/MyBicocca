package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import it.attendance100.mybicocca.ui.component.shape.OrganicShapes
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.ContinuePlayable

@Composable
fun ContinueWatchingCard(
    item: ContinuePlayable,
    onResume: () -> Unit,
    onGoToLesson: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    // Clickable Surface overload so the tap ripple is clipped to the card shape.
    Surface(
        onClick = onResume,
        shape = RoundedCornerShape(32.dp),
        color = scheme.surfaceContainerLowest,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            ThumbnailBlock(
                elapsedLabel = item.elapsedLabel,
                totalLabel = item.totalLabel,
                thumbnailUrl = item.thumbnailUrl,
            )
            Column(modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 14.dp, bottom = 4.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp,
                )
                if (!item.subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp,
                    )
                }
                Spacer(Modifier.height(12.dp))
                ActionRow(
                    label = if (item.progress > 0f) "Riprendi" else "Guarda",
                    onResume = onResume,
                    onGoToLesson = onGoToLesson,
                )
            }
        }
    }
}

@Composable
private fun ThumbnailBlock(
    elapsedLabel: String?,
    totalLabel: String?,
    thumbnailUrl: String?,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .clip(RoundedCornerShape(24.dp))
            .background(scheme.surfaceContainerHighest),
    ) {
        if (thumbnailUrl != null) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 10f),
            )
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp)
                    .size(160.dp)
                    .graphicsLayer { alpha = 0.10f }
                    .background(scheme.primary, OrganicShapes.Burst),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-25).dp, y = 25.dp)
                    .size(120.dp)
                    .graphicsLayer { alpha = 0.10f }
                    .background(scheme.tertiary, OrganicShapes.Puffy),
            )
        }

        if (elapsedLabel != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(
                    text = "$elapsedLabel / ${totalLabel ?: "—:—"}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                )
            }
        }
    }
}

@Composable
private fun ActionRow(label: String, onResume: () -> Unit, onGoToLesson: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Button(
            onClick = onResume,
            shape = CircleShape,
            modifier = Modifier
                .weight(1f)
                .height(72.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.size(12.dp))
            Text(
                text = label,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                letterSpacing = 0.1.sp,
            )
        }
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(OrganicShapes.SmoothCookie6)
                .background(scheme.surfaceContainerHigh)
                .clickable(onClick = onGoToLesson),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.SubdirectoryArrowRight,
                contentDescription = "Vai alla lezione",
                tint = scheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
