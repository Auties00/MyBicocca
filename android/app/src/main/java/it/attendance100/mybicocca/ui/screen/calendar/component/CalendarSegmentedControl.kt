package it.attendance100.mybicocca.ui.screen.calendar.component

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.screen.calendar.CalendarTestTags
import it.attendance100.mybicocca.ui.screen.calendar.state.CalendarViewMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val DisplayLocale: Locale = Locale.getDefault()
private val IndicatorEasing = CubicBezierEasing(0.5f, 0.05f, 0.1f, 1f)
private const val IndicatorDurationMs = 320

/**
 * Day/Week/Month switcher pinned above the calendar content: a pill-shaped container with
 * three equal segments, each stacking an uppercase mode caption over a summary of the
 * current selection — weekday and day number, the week's day span, the month name. A
 * filled indicator slides under the active segment on an emphasized-deceleration curve;
 * active content flips to the on-fill color while inactive segments stay muted, and dark
 * theme swaps the brand fill for the softer primary container.
 */
@Composable
fun CalendarSegmentedControl(
    viewMode: CalendarViewMode,
    selectedDay: LocalDate,
    weekStart: LocalDate,
    selectedMonth: YearMonth,
    onSelect: (CalendarViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val containerColor = if (dark) scheme.surfaceContainerHigh else scheme.surfaceContainer
    val indicatorColor = if (dark) scheme.primaryContainer else scheme.primary
    val activeContentColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary
    val inactiveContentColor = scheme.onSurfaceVariant

    val dayLabel = stringResource(R.string.calendar_view_day)
    val weekLabel = stringResource(R.string.calendar_view_week)
    val monthLabel = stringResource(R.string.calendar_view_month)
    val segments = remember(selectedDay, weekStart, selectedMonth) {
        listOf(
            Segment(CalendarViewMode.DAY, primary = dayPrimary(selectedDay), secondary = dayLabel),
            Segment(CalendarViewMode.WEEK, primary = weekPrimary(weekStart), secondary = weekLabel),
            Segment(
                CalendarViewMode.MONTH,
                primary = monthPrimary(selectedMonth),
                secondary = monthLabel
            ),
        )
    }
    val selectedIndex = segments.indexOfFirst { it.mode == viewMode }.coerceAtLeast(0)
    val indicatorFraction by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = tween(durationMillis = IndicatorDurationMs, easing = IndicatorEasing),
        label = "segmented_indicator_fraction",
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(containerColor)
            .padding(4.dp),
    ) {
        val density = LocalDensity.current
        val slotWidthPx = with(density) { maxWidth.toPx() } / segments.size
        val indicatorRadiusPx = with(density) { 18.dp.toPx() }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .drawBehind {
                    drawRoundRect(
                        color = indicatorColor,
                        topLeft = Offset(indicatorFraction * slotWidthPx, 0f),
                        size = Size(slotWidthPx, size.height),
                        cornerRadius = CornerRadius(indicatorRadiusPx, indicatorRadiusPx),
                    )
                },
        ) {
            segments.forEach { segment ->
                val active = segment.mode == viewMode
                val contentColor = if (active) activeContentColor else inactiveContentColor
                Segment(
                    primary = segment.primary,
                    secondary = segment.secondary,
                    active = active,
                    contentColor = contentColor,
                    onClick = { onSelect(segment.mode) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag(CalendarTestTags.segment(segment.mode)),
                )
            }
        }
    }
}

@Composable
private fun Segment(
    primary: String,
    secondary: String,
    active: Boolean,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = secondary.uppercase(DisplayLocale),
            color = contentColor.copy(alpha = if (active) 0.9f else 0.75f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.4.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = primary,
            color = contentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.1).sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private data class Segment(
    val mode: CalendarViewMode,
    val primary: String,
    val secondary: String,
)

private fun dayPrimary(date: LocalDate): String {
    val dow = date.dayOfWeek
        .getDisplayName(TextStyle.SHORT_STANDALONE, DisplayLocale)
        .replaceFirstChar { it.uppercase() }
        .trimEnd('.')
    return "$dow ${date.dayOfMonth}"
}

private fun weekPrimary(weekStart: LocalDate): String =
    "${weekStart.dayOfMonth}–${weekStart.plusDays(6).dayOfMonth}"

private fun monthPrimary(yearMonth: YearMonth): String =
    yearMonth.month
        .getDisplayName(TextStyle.FULL_STANDALONE, DisplayLocale)
        .replaceFirstChar { it.uppercase() }
