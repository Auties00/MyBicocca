package it.attendance100.mybicocca.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.hapticfeedback.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.entities.*
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.math.absoluteValue

// CONSTANTS

private object StackConstants {
    val CARD_CORNER_RADIUS = 16.dp
    val COLOR_BAR_WIDTH = 4.dp

    // Stack visual
    val STACK_OFFSET_X = 10.dp
    val STACK_OFFSET_Y = 6.dp

    // Swipe thresholds
    const val SWIPE_THRESHOLD = 0.25f
    const val VELOCITY_THRESHOLD = 400f

    // Visible cards
    const val MAX_VISIBLE_CARDS = 3
}

// MAIN COMPONENT

@Composable
fun SwipeableCardStack(
    events: List<CourseEvent>,
    currentEventId: Long,
    onEventSelected: (Long) -> Unit,
    onEventClick: (CourseEvent) -> Unit,
    cardHeight: Dp,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    if (events.isEmpty()) return

    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    val sortedEvents = events

    var currentIndex by remember {
        mutableIntStateOf(
            sortedEvents.indexOfFirst { it.id == currentEventId }.coerceAtLeast(0)
        )
    }

    LaunchedEffect(currentEventId) {
        val newIndex = sortedEvents.indexOfFirst { it.id == currentEventId }
        if (newIndex >= 0 && newIndex != currentIndex) {
            currentIndex = newIndex
        }
    }

    var isExpanded by remember { mutableStateOf(false) }
    val offsetX = remember { Animatable(0f) }
    var cardWidth by remember { mutableFloatStateOf(300f) }

    val stackExtraX = with(density) {
        StackConstants.STACK_OFFSET_X.toPx() * (StackConstants.MAX_VISIBLE_CARDS - 1)
    }
    val stackExtraY = with(density) {
        StackConstants.STACK_OFFSET_Y.toPx() * (StackConstants.MAX_VISIBLE_CARDS - 1)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // AGGIUNTA ANIMAZIONE QUI
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                if (targetState) {
                    // Espansione: Fade In + Espansione Verticale
                    (fadeIn(animationSpec = tween(300)) +
                            expandVertically(expandFrom = Alignment.Top, animationSpec = tween(300, easing = FastOutSlowInEasing)))
                        .togetherWith(
                            fadeOut(animationSpec = tween(300)) +
                                    shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(300))
                        )
                } else {
                    // Collasso: Fade Out + Restringimento Verticale
                    (fadeIn(animationSpec = tween(300)) +
                            expandVertically(expandFrom = Alignment.Top, animationSpec = tween(300)))
                        .togetherWith(
                            fadeOut(animationSpec = tween(300)) +
                                    shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(300, easing = FastOutSlowInEasing))
                        )
                }
            },
            label = "expand_collapse_animation"
        ) { targetExpanded ->
            if (targetExpanded) {
                // ============================
                // MODALITÀ ESPANSA
                // ============================
                ExpandedEventsList(
                    events = sortedEvents,
                    currentIndex = currentIndex,
                    textColor = textColor,
                    grayColor = grayColor,
                    primaryColor = primaryColor,
                    onEventSelect = { index ->
                        currentIndex = index
                        onEventSelected(sortedEvents[index].id)
                        isExpanded = false
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onCollapse = {
                        isExpanded = false
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                )
            } else {
                // ============================
                // MODALITÀ STACK
                // ============================
                // Raggruppiamo tutto in una Column per l'AnimatedContent
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(cardHeight + with(density) { stackExtraY.toDp() })
                            .padding(top = with(density) { stackExtraY.toDp() })
                            .onSizeChanged { cardWidth = it.width.toFloat() - stackExtraX }
                            .draggable(
                                state = rememberDraggableState { delta ->
                                    scope.launch { offsetX.snapTo(offsetX.value + delta) }
                                },
                                orientation = Orientation.Horizontal,
                                onDragStopped = { velocity ->
                                    scope.launch {
                                        val threshold = cardWidth * StackConstants.SWIPE_THRESHOLD
                                        when {
                                            offsetX.value < -threshold || velocity < -StackConstants.VELOCITY_THRESHOLD -> {
                                                offsetX.animateTo(-cardWidth, spring(dampingRatio = Spring.DampingRatioLowBouncy))
                                                currentIndex = (currentIndex + 1) % sortedEvents.size
                                                onEventSelected(sortedEvents[currentIndex].id)
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                offsetX.snapTo(0f)
                                            }
                                            offsetX.value > threshold || velocity > StackConstants.VELOCITY_THRESHOLD -> {
                                                offsetX.animateTo(cardWidth, spring(dampingRatio = Spring.DampingRatioLowBouncy))
                                                currentIndex = (currentIndex - 1 + sortedEvents.size) % sortedEvents.size
                                                onEventSelected(sortedEvents[currentIndex].id)
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                offsetX.snapTo(0f)
                                            }
                                            else -> {
                                                offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                            }
                                        }
                                    }
                                }
                            )
                    ) {
                        val visibleCount = minOf(StackConstants.MAX_VISIBLE_CARDS, sortedEvents.size)
                        for (stackPos in (visibleCount - 1) downTo 0) {
                            val eventIndex = (currentIndex + stackPos) % sortedEvents.size
                            val event = sortedEvents[eventIndex]
                            DeckCard(
                                event = event,
                                stackPosition = stackPos,
                                dragOffset = if (stackPos == 0) offsetX.value else 0f,
                                cardWidth = cardWidth,
                                cardHeight = cardHeight,
                                textColor = textColor,
                                grayColor = grayColor,
                                primaryColor = primaryColor,
                                onClick = { if (stackPos == 0) onEventClick(event) }
                            )
                        }
                    }

                    if (sortedEvents.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, end = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            PositionIndicator(
                                currentIndex = currentIndex,
                                totalEvents = sortedEvents.size,
                                grayColor = grayColor,
                                onClick = {
                                    isExpanded = true
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// COMPONENTI UI

@Composable
private fun PositionIndicator(
    currentIndex: Int,
    totalEvents: Int,
    grayColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = grayColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "${currentIndex + 1}/$totalEvents", color = grayColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Icon(imageVector = Icons.Outlined.UnfoldMore, contentDescription = null, tint = grayColor, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun ExpandedEventsList(
    events: List<CourseEvent>,
    currentIndex: Int,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onEventSelect: (Int) -> Unit,
    onCollapse: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${events.size} eventi sovrapposti", color = grayColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Surface(modifier = Modifier.clickable(onClick = onCollapse), color = grayColor.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Outlined.UnfoldLess, contentDescription = null, tint = grayColor, modifier = Modifier.size(16.dp))
                    Text(text = "Comprimi", color = grayColor, fontSize = 12.sp)
                }
            }
        }
        events.forEachIndexed { index, event ->
            val eventColor = CalendarUtils.getEventColor(event.eventType, primaryColor)
            val isSelected = index == currentIndex
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { onEventSelect(index) },
                color = if (isSelected) eventColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                border = if (isSelected) BorderStroke(1.5.dp, eventColor.copy(alpha = 0.5f)) else BorderStroke(0.5.dp, grayColor.copy(alpha = 0.15f))
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(3.dp).height(40.dp).clip(RoundedCornerShape(2.dp)).background(eventColor))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = event.courseName, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = "${event.startTime.format(CalendarUtils.timeFormatter)} - ${event.endTime.format(CalendarUtils.timeFormatter)}", color = grayColor, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeckCard(
    event: CourseEvent,
    stackPosition: Int,
    dragOffset: Float,
    cardWidth: Float,
    cardHeight: Dp,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    val eventColor = CalendarUtils.getEventColor(event.eventType, primaryColor)
    val eventStatus = remember(event) { getEventStatus(event) }

    val baseOffsetX = with(density) { StackConstants.STACK_OFFSET_X.toPx() * stackPosition }
    val baseOffsetY = with(density) { -StackConstants.STACK_OFFSET_Y.toPx() * stackPosition }
    val baseScale = 1f - (0.03f * stackPosition)

    val finalOffsetX = if (stackPosition == 0) dragOffset else baseOffsetX
    val rotation = if (stackPosition == 0 && cardWidth > 0) (dragOffset / cardWidth) * 8f else 0f

    val zIndex = (10 - stackPosition).toFloat()
    val elevation = if (stackPosition == 0) 6.dp else (3 - stackPosition).coerceAtLeast(1).dp

    Surface(
        modifier = Modifier
            .zIndex(zIndex)
            .width(with(density) { cardWidth.toDp() })
            .height(cardHeight)
            .graphicsLayer {
                translationX = if (stackPosition == 0) finalOffsetX else baseOffsetX
                translationY = baseOffsetY
                scaleX = baseScale
                scaleY = baseScale
                rotationZ = rotation
                transformOrigin = TransformOrigin(0.5f, 0.85f)
            },
        shape = RoundedCornerShape(StackConstants.CARD_CORNER_RADIUS),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = elevation,
        border = if (stackPosition > 0) BorderStroke(0.5.dp, grayColor.copy(alpha = 0.1f)) else null,
        onClick = onClick
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            EventColorBar(color = eventColor, status = eventStatus)
            EventCardContent(
                event = event,
                height = cardHeight,
                eventStatus = eventStatus,
                eventColor = eventColor,
                textColor = textColor,
                grayColor = grayColor
            )
        }
    }
}

@Composable
private fun EventCardContent(
    event: CourseEvent,
    height: Dp,
    eventStatus: TimelineEventStatus,
    eventColor: Color,
    textColor: Color,
    grayColor: Color
) {
    val showCompactContent = height < 80.dp
    val durationText = remember(event) { CalendarUtils.formatDuration(event.startTime, event.endTime) }

    VerticalClipLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (showCompactContent) 8.dp else 12.dp),
        spacing = if (showCompactContent) 4.dp else 6.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = event.courseName,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = if (showCompactContent) 1 else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            EventStatusIndicator(event = event, eventStatus = eventStatus, eventColor = eventColor)
        }

        if (showCompactContent) {
            EventInfoChip(
                icon = Icons.Outlined.Schedule,
                text = "${event.startTime.format(CalendarUtils.timeFormatter)} - ${event.endTime.format(CalendarUtils.timeFormatter)}",
                color = grayColor
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                EventInfoChip(icon = Icons.Outlined.Schedule, text = "${event.startTime.format(CalendarUtils.timeFormatter)} - ${event.endTime.format(CalendarUtils.timeFormatter)}", color = grayColor)
                EventInfoChip(icon = Icons.Outlined.Timer, text = durationText, color = grayColor)
            }
            CalendarUtils.formatEventLocation(event.room, event.building)?.let { location ->
                EventInfoChip(Icons.Outlined.LocationOn, location, grayColor)
            }
            if (height >= 120.dp) {
                event.professor?.let { professor ->
                    EventInfoChip(Icons.Outlined.Person, professor, grayColor)
                }
            }
        }
    }
}

@Composable
private fun EventStatusIndicator(event: CourseEvent, eventStatus: TimelineEventStatus, eventColor: Color) {
    when (eventStatus) {
        TimelineEventStatus.ENDED -> {
            Surface(shape = RoundedCornerShape(6.dp), color = EventInProgressColor, modifier = Modifier.size(24.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
        TimelineEventStatus.IN_PROGRESS -> {
            val infiniteTransition = rememberInfiniteTransition(label = "in_progress")
            val scale by infiniteTransition.animateFloat(initialValue = 0.8f, targetValue = 1.2f, animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale")
            Box(modifier = Modifier.size(24.dp).scale(scale), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(EventInProgressColor))
            }
        }
        TimelineEventStatus.UPCOMING -> {
            Surface(shape = RoundedCornerShape(6.dp), color = eventColor.copy(alpha = 0.15f), modifier = Modifier.size(28.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(getEventTypeIcon(event.eventType), null, tint = eventColor, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun EventColorBar(color: Color, status: TimelineEventStatus) {
    val barColor = if (status == TimelineEventStatus.IN_PROGRESS) EventInProgressColor else color
    val infiniteTransition = rememberInfiniteTransition(label = "bar_pulse")
    val animatedAlpha by if (status == TimelineEventStatus.IN_PROGRESS) {
        infiniteTransition.animateFloat(initialValue = 0.7f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "alpha")
    } else remember { mutableFloatStateOf(1f) }
    Box(modifier = Modifier.width(StackConstants.COLOR_BAR_WIDTH).fillMaxHeight().clip(RoundedCornerShape(topStart = StackConstants.CARD_CORNER_RADIUS, bottomStart = StackConstants.CARD_CORNER_RADIUS)).background(barColor.copy(alpha = animatedAlpha)))
}

@Composable
private fun EventInfoChip(icon: ImageVector, text: String, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
        Text(text = text, color = color, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private fun getEventTypeIcon(eventType: EventType): ImageVector = when (eventType) {
    EventType.LECTURE -> Icons.Outlined.School
    EventType.LAB -> Icons.Outlined.Science
    EventType.EXAM -> Icons.AutoMirrored.Outlined.Assignment
    EventType.OTHER -> Icons.Outlined.Event
}

private enum class TimelineEventStatus { ENDED, IN_PROGRESS, UPCOMING }

private fun getEventStatus(event: CourseEvent): TimelineEventStatus {
    val now = LocalDateTime.now()
    return when {
        event.isCancelled -> TimelineEventStatus.ENDED
        now.isAfter(event.endTime) -> TimelineEventStatus.ENDED
        now.isAfter(event.startTime) && now.isBefore(event.endTime) -> TimelineEventStatus.IN_PROGRESS
        else -> TimelineEventStatus.UPCOMING
    }
}

@Composable
fun VerticalClipLayout(
    modifier: Modifier = Modifier,
    spacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val spacingPx = spacing.roundToPx()
        val placeables = mutableListOf<Placeable>()
        var currentHeight = 0

        for (measurable in measurables) {
            val placeable = measurable.measure(constraints.copy(minHeight = 0))
            val requiredSpace = placeable.height + if (placeables.isNotEmpty()) spacingPx else 0

            if (currentHeight + requiredSpace <= constraints.maxHeight) {
                placeables.add(placeable)
                currentHeight += requiredSpace
            } else {
                break
            }
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            var y = 0
            placeables.forEachIndexed { index, placeable ->
                if (index > 0) y += spacingPx
                placeable.place(0, y)
                y += placeable.height
            }
        }
    }
}