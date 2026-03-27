package it.attendance100.mybicocca.ui.component.calendar.dialog

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.startDateTime
import it.attendance100.mybicocca.data.model.calendar.endDateTime
import it.attendance100.mybicocca.data.model.calendar.EventType
import it.attendance100.mybicocca.ui.component.calendar.getEventTypeIcon
import it.attendance100.mybicocca.ui.component.calendar.CalendarUtils
import it.attendance100.mybicocca.ui.component.calendar.EventStatus
import it.attendance100.mybicocca.ui.component.calendar.resolveStatus
import java.time.Duration
import java.time.LocalDateTime
import java.util.Locale

/**
 * Schermata dettaglio evento per visualizzazione in-modal.
 * Ottimizzata per bottom sheet con navigazione slide.
 */
@Composable
fun EventDetailScreen(
    event: CalendarEvent,
    onBack: () -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val eventColor = CalendarUtils.getEventColor(event.eventType, primaryColor)
    val eventStatus = remember(event) { event.resolveStatus() }

    // Animated gradient
    val infiniteTransition = rememberInfiniteTransition(label = "headerGradient")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(4000, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "gradientOffset"
    )

    // Stagger animation state
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Header with gradient background
        EventDetailHeader(
            event = event,
            eventColor = eventColor,
            gradientOffset = gradientOffset,
            isVisible = isVisible,
            onBack = onBack
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Time card (overlapping header)
            EventDetailTimeCard(
                event = event,
                eventColor = eventColor,
                eventStatus = eventStatus,
                isVisible = isVisible
            )

            // Details section
            EventDetailInfoSection(
                event = event,
                eventColor = eventColor,
                isVisible = isVisible
            )

            // E-Learning button
            EventDetailActionButton(
                isVisible = isVisible,
                onClick = { /* TODO: Navigate to e-learning */ }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// === HEADER SECTION ===

@Composable
private fun EventDetailHeader(
    event: CalendarEvent,
    eventColor: Color,
    gradientOffset: Float,
    isVisible: Boolean,
    onBack: () -> Unit
) {
    val locale = Locale.getDefault()
    val dayOfWeekFormatter = CalendarUtils.dayOfWeekFormatter(locale)
    val dateFormatter = CalendarUtils.dateWithYearFormatter(locale)

    val headerAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "headerAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        eventColor,
                        eventColor.copy(alpha = 0.85f),
                        Color(
                            red = (eventColor.red * 0.8f + gradientOffset * 0.1f).coerceIn(0f, 1f),
                            green = (eventColor.green * 0.9f).coerceIn(0f, 1f),
                            blue = (eventColor.blue * 1.1f).coerceIn(0f, 1f),
                            alpha = 1f
                        )
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(500f + gradientOffset * 200f, 500f)
                )
            )
    ) {
        // Overlay decorativo
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.1f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .graphicsLayer { alpha = headerAlpha },
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top row: back button + event type chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                FilledTonalIconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.arrow_back),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Event type chip
                EventTypeChip(event.eventType)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Course name
            Text(
                text = event.title,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp
            )

            // Date
            Text(
                text = buildString {
                    append(event.startDateTime.format(dayOfWeekFormatter).replaceFirstChar { it.uppercase() })
                    append(", ")
                    append(event.startDateTime.format(dateFormatter))
                },
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun EventTypeChip(eventType: EventType) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getEventTypeIcon(eventType),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(CalendarUtils.getEventTypeStringRes(eventType)),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// === TIME CARD SECTION ===

@Composable
private fun EventDetailTimeCard(
    event: CalendarEvent,
    eventColor: Color,
    eventStatus: EventStatus,
    isVisible: Boolean
) {
    val timeFormatter = CalendarUtils.timeFormatter
    val durationText = CalendarUtils.formatDuration(event.startDateTime, event.endDateTime)
    val statusText = getStatusText(event, eventStatus)

    val cardAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300, delayMillis = 100),
        label = "cardAlpha"
    )

    val cardOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 30f,
        animationSpec = tween(300, delayMillis = 100),
        label = "cardOffset"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-16).dp)
            .graphicsLayer {
                alpha = cardAlpha
                translationY = cardOffset
            },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Time row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Start time
                TimeColumn(
                    label = stringResource(R.string.event_detail_start),
                    time = event.startDateTime.format(timeFormatter)
                )

                // Duration indicator
                DurationIndicator(
                    durationText = durationText,
                    eventColor = eventColor,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 24.dp)
                )

                // End time
                TimeColumn(
                    label = stringResource(R.string.event_detail_end),
                    time = event.endDateTime.format(timeFormatter)
                )
            }

            // Progress bar (if in progress)
            if (eventStatus == EventStatus.IN_PROGRESS) {
                Spacer(modifier = Modifier.height(16.dp))
                EventProgressBar(
                    event = event,
                    eventColor = eventColor
                )
            }

            // Status badge
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = eventColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = statusText,
                    color = eventColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun TimeColumn(
    label: String,
    time: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = time,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DurationIndicator(
    durationText: String,
    eventColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .padding(horizontal = 8.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, eventColor.copy(alpha = 0.5f))
                    ),
                    shape = RoundedCornerShape(1.dp)
                )
        )

        AssistChip(
            onClick = {},
            label = {
                Text(
                    durationText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Outlined.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = eventColor.copy(alpha = 0.15f),
                labelColor = eventColor,
                leadingIconContentColor = eventColor
            ),
            border = null
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .padding(horizontal = 8.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(eventColor.copy(alpha = 0.5f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(1.dp)
                )
        )
    }
}

@Composable
private fun EventProgressBar(
    event: CalendarEvent,
    eventColor: Color
) {
    val progress = remember { CalendarUtils.calculateEventProgress(event) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "animatedProgress"
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
        color = eventColor,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

@Composable
private fun getStatusText(event: CalendarEvent, eventStatus: EventStatus): String {
    return when (eventStatus) {
        EventStatus.CANCELLED -> stringResource(R.string.event_status_cancelled)
        EventStatus.ENDED -> {
            val elapsed = Duration.between(event.endDateTime, LocalDateTime.now())
            val elapsedMins = elapsed.toMinutes()
            when {
                elapsedMins < 60 -> stringResource(R.string.event_status_ended_mins, elapsedMins)
                elapsedMins < 1440 -> stringResource(R.string.event_status_ended_hours, elapsedMins / 60)
                elapsedMins < 10080 -> stringResource(R.string.event_status_ended_days, elapsedMins / 1440)
                else -> stringResource(R.string.event_status_ended)
            }
        }
        EventStatus.IN_PROGRESS -> {
            val elapsed = Duration.between(event.startDateTime, LocalDateTime.now())
            val elapsedMins = elapsed.toMinutes()
            if (elapsedMins >= 60) {
                stringResource(R.string.event_status_in_progress_hours, elapsedMins / 60, elapsedMins % 60)
            } else {
                stringResource(R.string.event_status_in_progress_mins, elapsedMins)
            }
        }
        EventStatus.UPCOMING -> {
            val until = Duration.between(LocalDateTime.now(), event.startDateTime)
            val untilMins = until.toMinutes()
            when {
                untilMins < 60 -> stringResource(R.string.event_status_starts_mins, untilMins)
                untilMins < 1440 -> stringResource(R.string.event_status_starts_hours, untilMins / 60)
                else -> stringResource(R.string.event_status_starts_days, untilMins / 1440)
            }
        }
    }
}

// === INFO SECTION ===

@Composable
private fun EventDetailInfoSection(
    event: CalendarEvent,
    eventColor: Color,
    isVisible: Boolean
) {
    val sectionAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300, delayMillis = 200),
        label = "sectionAlpha"
    )

    val sectionOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 30f,
        animationSpec = tween(300, delayMillis = 200),
        label = "sectionOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = sectionAlpha
                translationY = sectionOffset
            },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Location
        CalendarUtils.formatEventLocation(event.location, event.buildingCode)?.let { location ->
            DetailInfoCard(
                icon = Icons.Outlined.LocationOn,
                label = stringResource(R.string.event_detail_location),
                value = location,
                accentColor = eventColor
            )
        }

        // Professor
        event.teacherName?.let { professor ->
            DetailInfoCard(
                icon = Icons.Outlined.Person,
                label = stringResource(R.string.event_detail_professor),
                value = professor,
                accentColor = eventColor
            )
        }

    }
}

@Composable
private fun DetailInfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Surface(
                shape = MaterialTheme.shapes.small,
                color = accentColor.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// === ACTION BUTTON ===

@Composable
private fun EventDetailActionButton(
    isVisible: Boolean,
    onClick: () -> Unit
) {
    val buttonAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300, delayMillis = 300),
        label = "buttonAlpha"
    )

    val buttonOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 30f,
        animationSpec = tween(300, delayMillis = 300),
        label = "buttonOffset"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = buttonAlpha
                translationY = buttonOffset
            },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = stringResource(R.string.event_action_elearning),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(24.dp)
            )
        }
    }
}

