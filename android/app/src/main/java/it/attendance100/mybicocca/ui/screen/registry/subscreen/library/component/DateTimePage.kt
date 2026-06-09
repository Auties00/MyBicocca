package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.library.LibraryBookingConstraints
import it.attendance100.mybicocca.domain.model.library.LibrarySeat
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.durationLabel
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MonthTitleFormat = DateTimeFormatter.ofPattern("LLLL yyyy", Locale.ITALIAN)
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")
private val WeekdayLetters = listOf("L", "M", "M", "G", "V", "S", "D")
private val CardShape = RoundedCornerShape(28.dp)

// Wizard step 2: pick the day, the duration and the start time. Start times are derived from the
// live seat grid for the chosen day + duration, so they reflect real openings.
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
internal fun DateTimePage(
    constraints: Loadable<LibraryBookingConstraints>,
    constraintsStatus: SyncStatus,
    selectedDate: LocalDate?,
    selectedDuration: Int?,
    seats: Loadable<List<LibrarySeat>>,
    seatsStatus: SyncStatus,
    availableStartTimes: List<LocalTime>,
    selectedStartTime: LocalTime?,
    enabled: Boolean,
    onSelectDate: (LocalDate) -> Unit,
    onSelectDuration: (Int) -> Unit,
    onSelectStartTime: (LocalTime) -> Unit,
    onContinue: () -> Unit,
    onRetryConstraints: () -> Unit,
    onRetrySeats: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (constraints) {
                Loadable.NotYetLoaded -> when (constraintsStatus) {
                    is SyncStatus.Failed -> SheetMessage(
                        icon = Icons.Outlined.CalendarMonth,
                        title = "Caricamento non riuscito",
                        body = "Non è stato possibile caricare le disponibilità.",
                        action = { RetryButton(onClick = onRetryConstraints) },
                    )
                    else -> SheetLoadingIndicator(label = "Caricamento disponibilità…")
                }

                is Loadable.Loaded -> {
                    val openDays = constraints.value.openDays
                    Card(icon = Icons.Outlined.CalendarMonth, title = "Giorno") {
                        if (openDays.isEmpty()) {
                            Message("Nessun giorno prenotabile per questa zona.")
                        } else {
                            CalendarSection(
                                openDays = openDays,
                                selectedDate = selectedDate,
                                enabled = enabled,
                                onSelectDate = onSelectDate,
                            )
                        }
                    }

                    if (constraints.value.durationsMinutes.isNotEmpty()) {
                        Card(icon = Icons.Outlined.Timelapse, title = "Durata") {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                constraints.value.durationsMinutes.forEach { minutes ->
                                    ToggleButton(
                                        checked = minutes == selectedDuration,
                                        onCheckedChange = { onSelectDuration(minutes) },
                                        enabled = enabled,
                                        shapes = ToggleButtonDefaults.shapes(),
                                    ) {
                                        Text(durationLabel(minutes), fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    if (selectedDate != null && selectedDuration != null) {
                        Card(icon = Icons.Outlined.Schedule, title = "Orario di inizio") {
                            StartTimeSection(
                                seats = seats,
                                seatsStatus = seatsStatus,
                                availableStartTimes = availableStartTimes,
                                selectedStartTime = selectedStartTime,
                                enabled = enabled,
                                onSelectStartTime = onSelectStartTime,
                                onRetry = onRetrySeats,
                            )
                        }
                    }
                    Spacer(Modifier.size(4.dp))
                }
            }
        }

        Button(
            onClick = onContinue,
            enabled = enabled && selectedStartTime != null,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ButtonDefaults.MediumContainerHeight)
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
            contentPadding = ButtonDefaults.MediumContentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (dark) scheme.primaryContainer else scheme.primary,
                contentColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
            ),
        ) {
            Text("Scegli il posto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StartTimeSection(
    seats: Loadable<List<LibrarySeat>>,
    seatsStatus: SyncStatus,
    availableStartTimes: List<LocalTime>,
    selectedStartTime: LocalTime?,
    enabled: Boolean,
    onSelectStartTime: (LocalTime) -> Unit,
    onRetry: () -> Unit,
) {
    when (seats) {
        Loadable.NotYetLoaded -> when (seatsStatus) {
            is SyncStatus.Failed -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Caricamento orari non riuscito.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                RetryButton(onClick = onRetry)
            }
            else -> SheetLoadingIndicator(label = "Caricamento orari…")
        }

        is Loadable.Loaded -> {
            if (availableStartTimes.isEmpty()) {
                Message("Nessun orario disponibile in questa giornata.")
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableStartTimes.forEach { time ->
                        ToggleButton(
                            checked = time == selectedStartTime,
                            onCheckedChange = { onSelectStartTime(time) },
                            enabled = enabled,
                            shapes = ToggleButtonDefaults.shapes(),
                        ) {
                            Text(time.format(TimeFormat), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Card(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(shape = CardShape, color = scheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(20.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = scheme.onSurface)
            }
            content()
        }
    }
}

@Composable
private fun Message(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CalendarSection(
    openDays: List<LocalDate>,
    selectedDate: LocalDate?,
    enabled: Boolean,
    onSelectDate: (LocalDate) -> Unit,
) {
    val minMonth = remember(openDays) { YearMonth.from(openDays.min()) }
    val maxMonth = remember(openDays) { YearMonth.from(openDays.max()) }
    var month by remember(openDays) {
        mutableStateOf(selectedDate?.let { YearMonth.from(it) } ?: minMonth)
    }
    val availableSet = remember(openDays) { openDays.toHashSet() }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = month.format(MonthTitleFormat).replaceFirstChar { it.titlecase(Locale.ITALIAN) },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            FilledTonalIconButton(
                onClick = { if (month > minMonth) month = month.minusMonths(1) },
                enabled = enabled && month > minMonth,
                modifier = Modifier.size(40.dp),
                shapes = IconButtonDefaults.shapes(),
            ) { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Mese precedente") }
            Spacer(Modifier.size(6.dp))
            FilledTonalIconButton(
                onClick = { if (month < maxMonth) month = month.plusMonths(1) },
                enabled = enabled && month < maxMonth,
                modifier = Modifier.size(40.dp),
                shapes = IconButtonDefaults.shapes(),
            ) { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Mese successivo") }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            WeekdayLetters.forEach {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        monthWeeks(month).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    if (day == null) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        DayCell(
                            date = day,
                            available = day in availableSet,
                            selected = day == selectedDate,
                            isToday = day == LocalDate.now(),
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
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val cornerPercent by animateIntAsState(
        targetValue = if (selected) 32 else 50,
        animationSpec = motion.defaultSpatialSpec(),
        label = "day_corner",
    )
    val container by animateColorAsState(
        targetValue = when {
            selected -> scheme.primary
            available -> scheme.surfaceContainerHighest
            else -> Color.Transparent
        },
        animationSpec = motion.defaultEffectsSpec(),
        label = "day_color",
    )
    val content = when {
        selected -> Color.White
        !available -> scheme.onSurfaceVariant.copy(alpha = 0.45f)
        isToday -> scheme.primary
        else -> scheme.onSurface
    }

    Box(modifier = modifier.aspectRatio(1f).padding(3.dp), contentAlignment = Alignment.Center) {
        if (available) {
            Surface(
                onClick = { onSelectDate(date) },
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

private fun monthWeeks(month: YearMonth): List<List<LocalDate?>> {
    val length = month.lengthOfMonth()
    val leadingBlanks = month.atDay(1).dayOfWeek.value - 1
    val cells = ArrayList<LocalDate?>(leadingBlanks + length)
    repeat(leadingBlanks) { cells.add(null) }
    for (day in 1..length) cells.add(month.atDay(day))
    while (cells.size % 7 != 0) cells.add(null)
    return cells.chunked(7)
}
