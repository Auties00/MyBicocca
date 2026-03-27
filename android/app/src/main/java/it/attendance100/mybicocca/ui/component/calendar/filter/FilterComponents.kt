package it.attendance100.mybicocca.ui.component.calendar.filter

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.model.calendar.EventType
import it.attendance100.mybicocca.ui.component.calendar.CalendarConfig
import it.attendance100.mybicocca.ui.screen.calendar.TimeRange
import it.attendance100.mybicocca.util.getCurrentLocale
import kotlin.math.roundToInt

// HELPER FUNCTIONS

private fun snapTo15Minutes(value: Float): Float {
    return ((value / 15f).roundToInt() * 15).toFloat()
}

@Composable
private fun formatTime(totalMinutes: Int): String {
    val hour = totalMinutes / 60
    val minute = totalMinutes % 60
    return String.format(getCurrentLocale(), "%02d:%02d", hour, minute)
}

// EVENT TYPE CARDS

private data class EventTypeConfig(
    val type: EventType,
    val icon: ImageVector,
    val labelRes: Int
)

private val eventTypeConfigs = listOf(
    EventTypeConfig(EventType.LECTURE, Icons.AutoMirrored.Outlined.MenuBook, R.string.calendar_filter_lectures),
    EventTypeConfig(EventType.LAB, Icons.Outlined.Science, R.string.calendar_filter_labs),
    EventTypeConfig(EventType.EXAM, Icons.Outlined.Edit, R.string.calendar_filter_exams),
    EventTypeConfig(EventType.OTHER, Icons.Outlined.Category, R.string.event_type_other)
)

/**
 * Griglia compatta di card per selezione tipo evento.
 */
@Composable
fun EventTypeCardGrid(
    activeFilters: Set<EventType>,
    onFilterToggle: (EventType) -> Unit,
    getEventColor: (EventType) -> Color,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            eventTypeConfigs.take(2).forEach { config ->
                CompactEventTypeCard(
                    config = config,
                    isSelected = config.type in activeFilters,
                    eventColor = getEventColor(config.type),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFilterToggle(config.type)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            eventTypeConfigs.drop(2).forEach { config ->
                CompactEventTypeCard(
                    config = config,
                    isSelected = config.type in activeFilters,
                    eventColor = getEventColor(config.type),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFilterToggle(config.type)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Card compatta per tipo evento - layout orizzontale.
 * Usa il colore dell'evento per l'evidenziatura quando selezionato.
 */
@Composable
private fun CompactEventTypeCard(
    config: EventTypeConfig,
    isSelected: Boolean,
    eventColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) eventColor.copy(alpha = 0.1f) else Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) eventColor else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Icon container
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) eventColor else eventColor.copy(alpha = 0.15f)
            ) {
                Box(
                    modifier = Modifier.padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = config.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isSelected) Color.White else eventColor
                    )
                }
            }

            Text(
                text = stringResource(config.labelRes),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) eventColor else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.weight(1f))

            // Checkmark when selected
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = eventColor
                )
            }
        }
    }
}

// TIME RANGE SLIDER

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InlineTimeRangeSelector(
    currentRange: TimeRange?,
    onRangeChange: (TimeRange?) -> Unit,
    modifier: Modifier = Modifier
) {
    val range = currentRange ?: TimeRange()
    val haptic = LocalHapticFeedback.current
    val primaryColor = MaterialTheme.colorScheme.primary

    val minMinutes = CalendarConfig.TimeSettings.START_HOUR * 60
    val maxMinutes = CalendarConfig.TimeSettings.END_HOUR * 60

    // Preset ranges: Morning 7:00-13:00, Afternoon 13:00-22:00
    val morningRange = TimeRange(420, 780)  // 7*60=420, 13*60=780
    val afternoonRange = TimeRange(780, 1320) // 13*60=780, 22*60=1320

    var rawSliderPosition by remember(range) {
        mutableStateOf(range.startMinutes.toFloat()..range.endMinutes.toFloat())
    }
    var isDragging by remember { mutableStateOf(false) }
    var lastSnappedStart by remember { mutableIntStateOf(range.startMinutes) }
    var lastSnappedEnd by remember { mutableIntStateOf(range.endMinutes) }

    val animatedStart by animateFloatAsState(
        targetValue = if (isDragging) rawSliderPosition.start else snapTo15Minutes(rawSliderPosition.start),
        animationSpec = if (isDragging) snap() else spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessLow
        ),
        label = "startSnap"
    )
    val animatedEnd by animateFloatAsState(
        targetValue = if (isDragging) rawSliderPosition.endInclusive else snapTo15Minutes(rawSliderPosition.endInclusive),
        animationSpec = if (isDragging) snap() else spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessLow
        ),
        label = "endSnap"
    )

    val displayPosition = animatedStart..animatedEnd
    val hasCustomRange = currentRange != null && !currentRange.isDefault

    // Check if preset is active
    val isMorningActive = currentRange == morningRange
    val isAfternoonActive = currentRange == afternoonRange

    LaunchedEffect(animatedStart, animatedEnd, isDragging) {
        if (!isDragging) {
            val snappedStart = snapTo15Minutes(rawSliderPosition.start).toInt()
            val snappedEnd = snapTo15Minutes(rawSliderPosition.endInclusive).toInt()
            val newRange = TimeRange(snappedStart, snappedEnd)
            if (newRange != currentRange) {
                onRangeChange(if (newRange.isDefault) null else newRange)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = if (hasCustomRange) primaryColor.copy(alpha = 0.1f) else Color.Transparent,
            border = BorderStroke(
                width = 1.dp,
                color = if (hasCustomRange) primaryColor else MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Time display with preset pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Morning preset pill (left)
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (isMorningActive) {
                                onRangeChange(null) // Deselect
                            } else {
                                onRangeChange(morningRange)
                                rawSliderPosition = morningRange.startMinutes.toFloat()..morningRange.endMinutes.toFloat()
                            }
                        },
                        shape = RoundedCornerShape(50),
                        color = if (isMorningActive) primaryColor else MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Outlined.WbSunny,
                                contentDescription = stringResource(R.string.filter_preset_morning),
                                tint = if (isMorningActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Start time pill
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = primaryColor
                    ) {
                        Text(
                            text = formatTime(snapTo15Minutes(displayPosition.start).toInt()),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    Text(
                        text = "—",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    // End time pill
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = primaryColor
                    ) {
                        Text(
                            text = formatTime(snapTo15Minutes(displayPosition.endInclusive).toInt()),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Afternoon preset pill (right)
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (isAfternoonActive) {
                                onRangeChange(null) // Deselect
                            } else {
                                onRangeChange(afternoonRange)
                                rawSliderPosition = afternoonRange.startMinutes.toFloat()..afternoonRange.endMinutes.toFloat()
                            }
                        },
                        shape = RoundedCornerShape(50),
                        color = if (isAfternoonActive) primaryColor else MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Outlined.WbTwilight,
                                contentDescription = stringResource(R.string.filter_preset_afternoon),
                                tint = if (isAfternoonActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // RangeSlider
                RangeSlider(
                    value = displayPosition,
                    onValueChange = { newRange ->
                        isDragging = true
                        if (newRange.endInclusive - newRange.start >= 15f) {
                            rawSliderPosition = newRange

                            val currentSnappedStart = snapTo15Minutes(newRange.start).toInt()
                            val currentSnappedEnd = snapTo15Minutes(newRange.endInclusive).toInt()

                            if (currentSnappedStart != lastSnappedStart) {
                                if (currentSnappedStart % 60 == 0) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                } else {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                lastSnappedStart = currentSnappedStart
                            }

                            if (currentSnappedEnd != lastSnappedEnd) {
                                if (currentSnappedEnd % 60 == 0) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                } else {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                lastSnappedEnd = currentSnappedEnd
                            }
                        }
                    },
                    onValueChangeFinished = {
                        isDragging = false
                    },
                    valueRange = minMinutes.toFloat()..maxMinutes.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = primaryColor,
                        activeTrackColor = primaryColor,
                        inactiveTrackColor = primaryColor.copy(alpha = 0.2f)
                    )
                )
            }
        }
    }
}

// LOCATION NAVIGATION CARD

@Composable
fun LocationNavigationCard(
    selectedCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = selectedCount > 0
    val haptic = LocalHapticFeedback.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val locationColor = Color(0xFF4CAF50) // Verde per location

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (isActive) primaryColor.copy(alpha = 0.1f) else Color.Transparent,
            border = BorderStroke(
                width = 1.dp,
                color = if (isActive) primaryColor else MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Icon container - stesso stile EventType cards
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isActive) primaryColor else locationColor.copy(alpha = 0.15f)
                ) {
                    Box(
                        modifier = Modifier.padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Business,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (isActive) Color.White else locationColor
                        )
                    }
                }

                // Text
                Text(
                    text = stringResource(R.string.filter_select_buildings),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) primaryColor else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.weight(1f))

                // Badge count when active
                if (isActive) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = primaryColor
                    ) {
                        Text(
                            text = selectedCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Arrow
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = if (isActive) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
