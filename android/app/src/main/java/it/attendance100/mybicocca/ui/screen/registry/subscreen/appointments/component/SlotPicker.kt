package it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.appointment.AppointmentMonthAvailability
import it.attendance100.mybicocca.domain.model.appointment.AppointmentSlot
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.component.button.RetryButton
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MonthTitleFormat = DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())
private val DayHeaderFormat = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.getDefault())
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

/** Italian week, Monday-first. The narrow names collide (Mar/Mer both "M"), so the letters are fixed. */
private val WeekdayLetters = listOf("L", "M", "M", "G", "V", "S", "D")

/** Rounded shape shared by the calendar and time grouping cards. */
private val PickerCardShape = RoundedCornerShape(32.dp)

/**
 * Expressive day/time picker: a full monthly calendar grid whose bookable days read as tonal
 * circles and morph into a brand-filled squircle when picked, then a reveal "orari" card that
 * splits the day into Mattina / Pomeriggio bands of expressive time toggles — all fed by live
 * availability.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SlotPicker(
    month: YearMonth,
    canGoToPreviousMonth: Boolean,
    canGoToNextMonth: Boolean,
    monthAvailability: Loadable<AppointmentMonthAvailability>,
    availabilityStatus: SyncStatus,
    selectedDate: LocalDate?,
    daySlots: Loadable<List<AppointmentSlot>>,
    slotsStatus: SyncStatus,
    selectedSlot: AppointmentSlot?,
    enabled: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onRetryMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onRetryDaySlots: () -> Unit,
    onSelectSlot: (AppointmentSlot) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PickerCard {
            MonthSwitcher(
                month = month,
                canGoToPreviousMonth = canGoToPreviousMonth,
                canGoToNextMonth = canGoToNextMonth,
                enabled = enabled,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
            )

            when (monthAvailability) {
                Loadable.NotYetLoaded -> when (availabilityStatus) {
                    is SyncStatus.Failed -> ErrorRow(onRetry = onRetryMonth)
                    else -> CenteredLoading()
                }

                is Loadable.Loaded -> {
                    val days = monthAvailability.value.availableDays
                    if (days.isEmpty()) {
                        PickerMessage(stringResource(R.string.appointments_no_availability))
                    } else {
                        CalendarGrid(
                            month = month,
                            availableDays = days,
                            selectedDate = selectedDate,
                            enabled = enabled,
                            onSelectDate = onSelectDate,
                        )
                    }
                }
            }
        }

        if (selectedDate != null) {
            PickerCard {
                TimeCard(
                    date = selectedDate,
                    daySlots = daySlots,
                    slotsStatus = slotsStatus,
                    selectedSlot = selectedSlot,
                    enabled = enabled,
                    onRetry = onRetryDaySlots,
                    onSelectSlot = onSelectSlot,
                )
            }
        }
    }
}

/** Tonal surface that groups one picker step, content vertically stacked. */
@Composable
private fun PickerCard(content: @Composable () -> Unit) {
    Surface(
        shape = PickerCardShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MonthSwitcher(
    month: YearMonth,
    canGoToPreviousMonth: Boolean,
    canGoToNextMonth: Boolean,
    enabled: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    val haptic = rememberHapticManager()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = month.format(MonthTitleFormat).replaceFirstChar { it.titlecase(Locale.getDefault()) },
            style = MaterialTheme.typography.titleLargeEmphasized,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        FilledTonalIconButton(
            onClick = { haptic.tap(); onPreviousMonth() },
            enabled = enabled && canGoToPreviousMonth,
            modifier = Modifier.size(44.dp),
            shapes = IconButtonDefaults.shapes(),
            colors = monthNavColors(),
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.appointments_previous_month)
            )
        }
        Spacer(Modifier.size(6.dp))
        FilledTonalIconButton(
            onClick = { haptic.tap(); onNextMonth() },
            enabled = enabled && canGoToNextMonth,
            modifier = Modifier.size(44.dp),
            shapes = IconButtonDefaults.shapes(),
            colors = monthNavColors(),
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = stringResource(R.string.appointments_next_month)
            )
        }
    }
}

/**
 * Brand-tinted month-nav chips: neutral container with a primary-red chevron, matching the
 * picker's other accents (today ring, schedule icon).
 */
@Composable
private fun monthNavColors() = IconButtonDefaults.filledTonalIconButtonColors(
    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    contentColor = MaterialTheme.colorScheme.primary,
)

/**
 * 7-column monthly grid. Bookable days are interactive tonal cells; every other day is an
 * inert muted number, so the month's real openings are legible at a glance.
 */
@Composable
private fun CalendarGrid(
    month: YearMonth,
    availableDays: List<LocalDate>,
    selectedDate: LocalDate?,
    enabled: Boolean,
    onSelectDate: (LocalDate) -> Unit,
) {
    val availableSet = remember(availableDays) { availableDays.toHashSet() }
    val today = LocalDate.now()
    val weeks = remember(month) { monthWeeks(month) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            WeekdayLetters.forEach { letter ->
                Text(
                    text = letter,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    if (day == null) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        DayCell(
                            date = day,
                            available = day in availableSet,
                            selected = day == selectedDate,
                            isToday = day == today,
                            enabled = enabled,
                            onSelectDate = onSelectDate,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

/**
 * One day in the grid. Bookable days morph circle -> brand-filled squircle on selection (M3
 * Expressive selection language); today carries a brand ring until it is the picked day. The
 * brand-red fill carries explicit white content in both themes.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DayCell(
    date: LocalDate,
    available: Boolean,
    selected: Boolean,
    isToday: Boolean,
    enabled: Boolean,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val cornerPercent by animateIntAsState(
        targetValue = if (selected) 32 else 50,
        animationSpec = motion.defaultSpatialSpec(),
        label = "day_cell_corner",
    )
    val container by animateColorAsState(
        targetValue = when {
            selected -> scheme.primary
            available -> scheme.surfaceContainerHighest
            else -> Color.Transparent
        },
        animationSpec = motion.defaultEffectsSpec(),
        label = "day_cell_color",
    )
    val content = when {
        selected -> Color.White
        !available -> scheme.onSurfaceVariant.copy(alpha = 0.45f)
        isToday -> scheme.primary
        else -> scheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(3.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (available) {
            Surface(
                onClick = { haptic.tap(); onSelectDate(date) },
                enabled = enabled,
                shape = RoundedCornerShape(cornerPercent),
                color = container,
                border = if (isToday && !selected) BorderStroke(1.5.dp, scheme.primary) else null,
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = content,
                    )
                }
            }
        } else {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = content,
            )
        }
    }
}

@Composable
private fun TimeCard(
    date: LocalDate,
    daySlots: Loadable<List<AppointmentSlot>>,
    slotsStatus: SyncStatus,
    selectedSlot: AppointmentSlot?,
    enabled: Boolean,
    onRetry: () -> Unit,
    onSelectSlot: (AppointmentSlot) -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Schedule,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = date.format(DayHeaderFormat).replaceFirstChar { it.titlecase(Locale.getDefault()) },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }

    when (daySlots) {
        Loadable.NotYetLoaded -> when (slotsStatus) {
            is SyncStatus.Failed -> ErrorRow(onRetry = onRetry)
            else -> CenteredLoading()
        }

        is Loadable.Loaded -> SlotGrid(
            slots = daySlots.value,
            selectedSlot = selectedSlot,
            enabled = enabled,
            onSelectSlot = onSelectSlot,
        )
    }
}

/**
 * Slots split by part of day into icon-led bands; free slots morph round -> square when
 * picked, taken ones stay disabled so the day's real occupancy is visible at a glance.
 */
@Composable
private fun SlotGrid(
    slots: List<AppointmentSlot>,
    selectedSlot: AppointmentSlot?,
    enabled: Boolean,
    onSelectSlot: (AppointmentSlot) -> Unit,
) {
    if (slots.none { it.available }) {
        PickerMessage(stringResource(R.string.appointments_all_slots_full))
        return
    }

    val morning = slots.filter { it.start.hour < 13 }
    val afternoon = slots.filter { it.start.hour >= 13 }

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        SlotSection(
            stringResource(R.string.appointments_morning),
            Icons.Outlined.WbSunny,
            morning,
            selectedSlot,
            enabled,
            onSelectSlot
        )
        SlotSection(
            stringResource(R.string.appointments_afternoon),
            Icons.Outlined.WbTwilight,
            afternoon,
            selectedSlot,
            enabled,
            onSelectSlot
        )
    }
}

/**
 * One part-of-day band: an icon-led label over a fixed 3-column grid of equal-width time
 * toggles, which reads as a tidy block whatever the slot count.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SlotSection(
    label: String,
    icon: ImageVector,
    slots: List<AppointmentSlot>,
    selectedSlot: AppointmentSlot?,
    enabled: Boolean,
    onSelectSlot: (AppointmentSlot) -> Unit,
) {
    val haptic = rememberHapticManager()
    if (slots.isEmpty()) return
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            slots.chunked(3).forEach { rowSlots ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowSlots.forEach { slot ->
                        ToggleButton(
                            checked = slot == selectedSlot,
                            onCheckedChange = { haptic.tap(); onSelectSlot(slot) },
                            enabled = enabled && slot.available,
                            shapes = ToggleButtonDefaults.shapes(),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(text = slot.start.format(TimeFormat), fontWeight = FontWeight.SemiBold)
                        }
                    }
                    repeat(3 - rowSlots.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun PickerMessage(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CenteredLoading() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LoadingIndicator(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun ErrorRow(onRetry: () -> Unit) {
    val haptic = rememberHapticManager()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.appointments_availability_load_failed),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        RetryButton(onClick = { haptic.tap(); onRetry() })
    }
}

/** Month laid out Monday-first as weeks of nullable days; leading/trailing nulls pad the grid. */
private fun monthWeeks(month: YearMonth): List<List<LocalDate?>> {
    val length = month.lengthOfMonth()
    val leadingBlanks = month.atDay(1).dayOfWeek.value - 1
    val cells = ArrayList<LocalDate?>(leadingBlanks + length)
    repeat(leadingBlanks) { cells.add(null) }
    for (day in 1..length) cells.add(month.atDay(day))
    while (cells.size % 7 != 0) cells.add(null)
    return cells.chunked(7)
}
