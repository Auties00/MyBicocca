package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.ui.component.shape.OrganicShapes
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.UpNextItem
import java.time.Duration
import java.time.Instant

@Composable
fun UpNextCard(
    item: UpNextItem,
    now: Instant,
    onSubmit: () -> Unit = item.onClick,
    onViewBrief: () -> Unit = item.onClick,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val countdown = formatCountdown(now, item.dueAt)
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = scheme.tertiaryContainer,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
                    text = "✦ $countdown",
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = scheme.onTertiaryContainer,
                        modifier = Modifier.clickable(onClick = onSubmit),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = scheme.tertiaryContainer,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "Consegna",
                                color = scheme.tertiaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                            )
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = scheme.tertiaryContainer,
                        border = BorderStroke(1.5.dp, scheme.onTertiaryContainer),
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onViewBrief),
                    ) {
                        Text(
                            text = "Apri",
                            color = scheme.onTertiaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun formatCountdown(now: Instant, due: Instant): String {
    val duration = Duration.between(now, due)
    if (duration.isNegative) return "SCADUTO"
    val days = duration.toDays()
    val hours = (duration.toHours() % 24)
    val minutes = (duration.toMinutes() % 60)
    return when {
        days >= 1 -> "TRA ${days}G ${hours}H"
        hours >= 1 -> "TRA ${hours}H ${minutes}MIN"
        else -> "TRA ${minutes}MIN"
    }
}
