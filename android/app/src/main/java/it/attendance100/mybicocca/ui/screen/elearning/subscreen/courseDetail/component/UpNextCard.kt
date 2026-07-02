package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.shape.OrganicShapes
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.UpNextItem
import java.time.Duration
import java.time.Instant

/**
 * Tertiary-container callout for the nearest upcoming deadline: an uppercase countdown label
 * ("TRA 2G 5H" / "SCADUTO"), the item title and optional subtitle, and a pill-shaped "Apri"
 * action, over faded organic blob decor. Horizontal padding comes from the host list's
 * content padding.
 */
@Composable
fun UpNextCard(
    modifier: Modifier = Modifier,
    item: UpNextItem,
    now: Instant,
    onOpen: () -> Unit = item.onClick,
) {
    val scheme = MaterialTheme.colorScheme
    val countdown = formatCountdown(now, item.dueAt)
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = scheme.tertiaryContainer,
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 30.dp, y = 30.dp)
                    .size(120.dp)
                    .graphicsLayer { alpha = 0.28f }
                    .background(scheme.tertiary, OrganicShapes.Puffy),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-30).dp, y = 14.dp)
                    .size(24.dp)
                    .graphicsLayer { alpha = 0.15f }
                    .background(scheme.onTertiaryContainer, OrganicShapes.Sunny),
            )
            Column {
                Text(
                    text = countdown,
                    color = scheme.tertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = scheme.onTertiaryContainer,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 26.sp,
                )
                if (!item.subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onTertiaryContainer,
                        fontStyle = FontStyle.Italic,
                    )
                }
                Spacer(Modifier.height(14.dp))
                Surface(
                    shape = CircleShape,
                    color = scheme.onTertiaryContainer,
                    modifier = Modifier.clickable(onClick = onOpen),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = scheme.tertiaryContainer,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = stringResource(R.string.elearning_link_open),
                            color = scheme.tertiaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun formatCountdown(now: Instant, due: Instant): String {
    val duration = Duration.between(now, due)
    if (duration.isNegative) return stringResource(R.string.elearning_upnext_overdue)
    val days = duration.toDays()
    val hours = (duration.toHours() % 24)
    val minutes = (duration.toMinutes() % 60)
    return when {
        days >= 1 -> stringResource(R.string.elearning_upnext_in_days, days, hours)
        hours >= 1 -> stringResource(R.string.elearning_upnext_in_hours, hours, minutes)
        else -> stringResource(R.string.elearning_upnext_in_minutes, minutes)
    }
}
