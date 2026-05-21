package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

private val MonthFormat = DateTimeFormatter.ofPattern("MMM", Locale.ITALIAN)
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun DateItem(
    call: ExamCall,
    today: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>(),
        label = "dateItemScale",
    )

    val tileShape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 10.dp,
        bottomStart = 10.dp,
        bottomEnd = 18.dp,
    )

    Surface(
        onClick = onClick,
        interactionSource = interaction,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        color = scheme.surfaceContainerHigh,
        shape = tileShape,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = dateLabel(call.callDate),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
            )
            Box(modifier = Modifier.weight(1f))
            Text(
                text = call.callTime?.format(TimeFormat) ?: "—",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
            WindowChip(
                opens = call.enrollmentWindow.opensAt,
                closes = call.enrollmentWindow.closesAt,
                today = today,
            )
        }
    }
}

private fun dateLabel(date: LocalDate?): String {
    if (date == null) return "Data da definire"
    val weekday = date.dayOfWeek
        .getDisplayName(TextStyle.SHORT, Locale.ITALIAN)
        .replaceFirstChar { it.titlecase(Locale.ITALIAN) }
    val month = date.format(MonthFormat).lowercase(Locale.ITALIAN)
    return "$weekday ${date.dayOfMonth} $month"
}

@Composable
private fun WindowChip(
    opens: LocalDate?,
    closes: LocalDate?,
    today: LocalDate,
) {
    val scheme = MaterialTheme.colorScheme
    val state = remember(opens, closes, today) { windowState(opens, closes, today) }
    if (state is WindowChipState.Unknown) return

    val (bg, fg) = when (state) {
        is WindowChipState.Closing -> scheme.errorContainer to scheme.onErrorContainer
        is WindowChipState.Open -> scheme.surfaceContainerHighest to scheme.onSurface
        is WindowChipState.Upcoming -> scheme.surfaceContainerHighest to scheme.onSurfaceVariant
        WindowChipState.Closed -> scheme.surfaceContainerHighest to scheme.onSurfaceVariant
        WindowChipState.Unknown -> return
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = state.label(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = fg,
        )
    }
}

private sealed interface WindowChipState {
    data class Closing(val daysLeft: Long) : WindowChipState
    data class Open(val daysLeft: Long) : WindowChipState
    data class Upcoming(val daysUntilOpen: Long) : WindowChipState
    data object Closed : WindowChipState
    data object Unknown : WindowChipState
}

private fun WindowChipState.label(): String = when (this) {
    is WindowChipState.Closing -> when (daysLeft) {
        0L -> "Oggi"
        1L -> "Domani"
        else -> "${daysLeft}g"
    }
    is WindowChipState.Open -> "${daysLeft}g"
    is WindowChipState.Upcoming -> "tra ${daysUntilOpen}g"
    WindowChipState.Closed -> "Chiuse"
    WindowChipState.Unknown -> ""
}

private fun windowState(opens: LocalDate?, closes: LocalDate?, today: LocalDate): WindowChipState {
    if (opens == null && closes == null) return WindowChipState.Unknown
    if (closes != null && closes.isBefore(today)) return WindowChipState.Closed
    if (opens != null && opens.isAfter(today)) {
        return WindowChipState.Upcoming(daysUntilOpen = ChronoUnit.DAYS.between(today, opens))
    }
    val daysLeft = closes?.let { ChronoUnit.DAYS.between(today, it).coerceAtLeast(0) }
        ?: return WindowChipState.Unknown
    return if (daysLeft <= ClosingThreshold) WindowChipState.Closing(daysLeft)
    else WindowChipState.Open(daysLeft)
}

private const val ClosingThreshold: Long = 3
