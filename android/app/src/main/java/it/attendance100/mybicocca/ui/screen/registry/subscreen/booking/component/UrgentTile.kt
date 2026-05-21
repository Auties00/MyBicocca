package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.ExamCardAccent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

private val MonthFormat = DateTimeFormatter.ofPattern("MMM", Locale.ITALIAN)

@Composable
fun UrgentTile(
    call: ExamCall,
    accent: ExamCardAccent,
    today: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>(),
        label = "urgentScale",
    )

    val tileShape = RoundedCornerShape(
        topStart = 32.dp,
        topEnd = 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 32.dp,
    )

    val daysLeft = call.enrollmentWindow.closesAt?.let {
        ChronoUnit.DAYS.between(today, it).coerceAtLeast(0)
    }
    val urgencyLabel = when (daysLeft) {
        null -> ""
        0L -> "Oggi"
        1L -> "Domani"
        else -> "${daysLeft}g"
    }

    Surface(
        onClick = onClick,
        interactionSource = interaction,
        modifier = modifier
            .width(168.dp)
            .height(208.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        color = accent.accent,
        contentColor = accent.onAccent,
        shape = tileShape,
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp)
                    .size(150.dp)
                    .graphicsLayer { rotationZ = -14f }
                    .clip(accent.shape)
                    .background(accent.onAccent.copy(alpha = 0.14f)),
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 18.dp, top = 16.dp, end = 18.dp),
            ) {
                if (call.callDate != null) {
                    Text(
                        text = call.callDate.dayOfMonth.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = accent.onAccent,
                    )
                    val sub = listOf(
                        call.callDate.format(MonthFormat).uppercase(Locale.ITALIAN),
                        call.callDate.dayOfWeek
                            .getDisplayName(TextStyle.SHORT, Locale.ITALIAN)
                            .uppercase(Locale.ITALIAN),
                    ).joinToString(" · ")
                    Text(
                        text = sub,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = accent.onAccent.copy(alpha = 0.80f),
                    )
                } else {
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = accent.onAccent,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, bottom = 16.dp),
            ) {
                if (urgencyLabel.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(accent.onAccent.copy(alpha = 0.20f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "Chiude $urgencyLabel",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = accent.onAccent,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    text = call.activityDescription?.takeIf { it.isNotBlank() } ?: "Esame",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent.onAccent,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
