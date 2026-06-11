package it.attendance100.mybicocca.ui.screen.calendar.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.EventSource
import it.attendance100.mybicocca.ui.screen.calendar.ext.durationMinutes
import it.attendance100.mybicocca.ui.screen.calendar.ext.monthGridCells
import it.attendance100.mybicocca.ui.screen.calendar.ext.rememberCurrentTime
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Seven-column grid of a single month under a Mon–Sun initials header. Each day cell is a
 * heat map of how booked the day is — the heavier the scheduled hours, the stronger the
 * brand tint — with a red dot marking days that hold an exam, a strong ring around the
 * selected day and a softer one around today. Leading cells before the month's first day
 * stay blank.
 */
@Composable
fun MonthGrid(
    yearMonth: YearMonth,
    selectedDay: LocalDate,
    eventsByDay: Map<LocalDate, List<CalendarEvent>>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalLocale.current.platformLocale
    val scheme = MaterialTheme.colorScheme
    val today by rememberCurrentTime()
    val cells = remember(yearMonth) { monthGridCells(yearMonth) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            WeekHeaderDays.forEach { dow ->
                Text(
                    text = dayHeader(locale, dow),
                    color = scheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.4.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
        Spacer(Modifier.height(6.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(WeekHeaderDays.size),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(cells) { date ->
                if (date == null) {
                    Box(modifier = Modifier.aspectRatio(MonthCellAspectRatio))
                } else {
                    val dayEvents = eventsByDay[date].orEmpty()
                    MonthCell(
                        date = date,
                        events = dayEvents,
                        isSelected = date == selectedDay,
                        isToday = date == today.toLocalDate(),
                        onClick = { onDayClick(date) },
                        cellAspectRatio = MonthCellAspectRatio
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthCell(
    date: LocalDate,
    events: List<CalendarEvent>,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    cellAspectRatio: Float,
) {
    val level = remember(events) { busyLevelOf(events) }
    val accent = busyAccent()
    val background = busyBackground(level)
    val numberColor = busyForeground(level)
    val borderColor = when {
        isSelected -> accent
        isToday -> accent.copy(alpha = 0.7f)
        else -> Color.Transparent
    }
    val borderWidth = when {
        isSelected -> 2.dp
        isToday -> 1.5.dp
        else -> 0.dp
    }
    val hasExam = events.any { it.source == EventSource.EXAM }

    Box(
        modifier = Modifier
            .aspectRatio(cellAspectRatio)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (hasExam) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error),
            )
        }
        Text(
            text = date.dayOfMonth.toString(),
            color = numberColor,
            fontSize = 13.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

private fun dayHeader(locale: Locale, dow: DayOfWeek): String =
    dow.getDisplayName(TextStyle.NARROW_STANDALONE, locale).uppercase(locale)

private val WeekHeaderDays: List<DayOfWeek> = listOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY
)

private const val MonthCellAspectRatio = 1f / 1.05f

/** Buckets of a day's total scheduled hours, driving the strength of the heat-map tint. */
private enum class BusyLevel { NONE, LIGHT, MODERATE, HEAVY, FULL }

private fun busyLevelOf(events: List<CalendarEvent>): BusyLevel {
    if (events.isEmpty()) return BusyLevel.NONE
    val hours = events.sumOf { it.durationMinutes() } / 60f
    return when {
        hours <= 2f -> BusyLevel.LIGHT
        hours <= 4f -> BusyLevel.MODERATE
        hours <= 6f -> BusyLevel.HEAVY
        else -> BusyLevel.FULL
    }
}

/** Base of the heat tint: brand primary in light theme, the softer primary container in dark. */
@Composable
private fun busyAccent(): Color {
    val scheme = MaterialTheme.colorScheme
    return if (isSystemInDarkTheme()) scheme.primaryContainer else scheme.primary
}

@Composable
private fun busyBackground(level: BusyLevel): Color {
    val scheme = MaterialTheme.colorScheme
    val accent = busyAccent()
    return when (level) {
        BusyLevel.NONE -> scheme.surfaceContainerLow
        BusyLevel.LIGHT -> lerp(scheme.surfaceContainerHighest, accent, 0.25f)
        BusyLevel.MODERATE -> lerp(scheme.surfaceContainerHighest, accent, 0.5f)
        BusyLevel.HEAVY -> lerp(scheme.surfaceContainerHighest, accent, 0.75f)
        BusyLevel.FULL -> accent
    }
}

/** Day-number color: flips to the on-accent color at the two strongest tints, where onSurface would lose contrast. */
@Composable
private fun busyForeground(level: BusyLevel): Color {
    val scheme = MaterialTheme.colorScheme
    val onAccent = if (isSystemInDarkTheme()) scheme.onPrimaryContainer else scheme.onPrimary
    return when (level) {
        BusyLevel.HEAVY, BusyLevel.FULL -> onAccent
        else -> scheme.onSurface
    }
}

/**
 * "Leggenda" caption over the light-to-full heat gradient bar with its hour-bucket labels,
 * explaining the tint scale of the month grid's day cells.
 */
@Composable
fun MonthBusyLegend(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val gradientStart = busyBackground(BusyLevel.LIGHT)
    val gradientEnd = busyBackground(BusyLevel.FULL)
    val bucketLabels = listOf("≤2h", "2–4h", "4–6h", ">6h")
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.calendar_legend),
            color = scheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Brush.horizontalGradient(listOf(gradientStart, gradientEnd))),
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            bucketLabels.forEach { label ->
                Text(
                    text = label,
                    color = scheme.onSurfaceVariant,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
