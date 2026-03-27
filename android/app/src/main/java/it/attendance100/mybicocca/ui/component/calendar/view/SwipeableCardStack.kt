package it.attendance100.mybicocca.ui.component.calendar.view

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.EventType
import it.attendance100.mybicocca.data.model.calendar.startDateTime
import it.attendance100.mybicocca.data.model.calendar.endDateTime
import it.attendance100.mybicocca.ui.component.calendar.CalendarConfig
import it.attendance100.mybicocca.ui.component.calendar.EventStatus
import it.attendance100.mybicocca.ui.component.calendar.resolveStatus
import it.attendance100.mybicocca.ui.component.calendar.EventCard
import it.attendance100.mybicocca.ui.component.calendar.EventCardVariant
import it.attendance100.mybicocca.ui.component.calendar.EventColorBar
import it.attendance100.mybicocca.ui.component.calendar.EventInfoChip
import it.attendance100.mybicocca.ui.component.calendar.EventStatusIndicator
import it.attendance100.mybicocca.ui.component.calendar.CalendarUtils
import kotlinx.coroutines.launch

// MAIN COMPONENT

@Composable
fun SwipeableCardStack(
    events: List<CalendarEvent>,
    currentEventId: String,
    onEventSelected: (String) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    cardHeight: Dp,
    modifier: Modifier = Modifier,
) {
    if (events.isEmpty()) return

    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val colorScheme = MaterialTheme.colorScheme

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
        CalendarConfig.Dimensions.STACK_OFFSET_X.toPx() * (CalendarConfig.Dimensions.MAX_VISIBLE_STACK_CARDS - 1)
    }
    val stackExtraY = with(density) {
        CalendarConfig.Dimensions.STACK_OFFSET_Y.toPx() * (CalendarConfig.Dimensions.MAX_VISIBLE_STACK_CARDS - 1)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                if (targetState) {
                    (fadeIn(animationSpec = tween(300)) +
                            expandVertically(
                                expandFrom = Alignment.Top,
                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                            ))
                        .togetherWith(
                            fadeOut(animationSpec = tween(300)) +
                                    shrinkVertically(
                                        shrinkTowards = Alignment.Top,
                                        animationSpec = tween(300)
                                    )
                        )
                } else {
                    (fadeIn(animationSpec = tween(300)) +
                            expandVertically(
                                expandFrom = Alignment.Top,
                                animationSpec = tween(300)
                            ))
                        .togetherWith(
                            fadeOut(animationSpec = tween(300)) +
                                    shrinkVertically(
                                        shrinkTowards = Alignment.Top,
                                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                                    )
                        )
                }
            },
            label = "expand_collapse"
        ) { targetExpanded ->
            if (targetExpanded) {
                ExpandedEventsList(
                    events = sortedEvents,
                    currentIndex = currentIndex,
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
                                        val threshold = cardWidth * CalendarConfig.Gesture.SWIPE_THRESHOLD
                                        when {
                                            offsetX.value < -threshold || velocity < -CalendarConfig.Gesture.VELOCITY_THRESHOLD -> {
                                                offsetX.animateTo(
                                                    -cardWidth,
                                                    spring(dampingRatio = Spring.DampingRatioLowBouncy)
                                                )
                                                currentIndex =
                                                    (currentIndex + 1) % sortedEvents.size
                                                onEventSelected(sortedEvents[currentIndex].id)
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                offsetX.snapTo(0f)
                                            }
                                            offsetX.value > threshold || velocity > CalendarConfig.Gesture.VELOCITY_THRESHOLD -> {
                                                offsetX.animateTo(
                                                    cardWidth,
                                                    spring(dampingRatio = Spring.DampingRatioLowBouncy)
                                                )
                                                currentIndex =
                                                    (currentIndex - 1 + sortedEvents.size) % sortedEvents.size
                                                onEventSelected(sortedEvents[currentIndex].id)
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                offsetX.snapTo(0f)
                                            }

                                            else -> {
                                                offsetX.animateTo(
                                                    0f,
                                                    spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                    ) {
                        val visibleCount =
                            minOf(CalendarConfig.Dimensions.MAX_VISIBLE_STACK_CARDS, sortedEvents.size)
                        for (stackPos in (visibleCount - 1) downTo 0) {
                            val eventIndex = (currentIndex + stackPos) % sortedEvents.size
                            val event = sortedEvents[eventIndex]

                            val cardUiState by remember(stackPos, cardWidth) {
                                derivedStateOf {
                                    val isTopCard = stackPos == 0
                                    val dragVal = if (isTopCard) offsetX.value else 0f

                                    val rotationVal = if (isTopCard && cardWidth > 0) (dragVal / cardWidth) * 8f else 0f
                                    val elevationVal = if (isTopCard) 6.dp else (3 - stackPos).coerceAtLeast(1).dp

                                    Triple(dragVal, rotationVal, elevationVal)
                                }
                            }

                            val (finalDragOffset, finalRotation, finalElevation) = cardUiState

                            DeckCard(
                                event = event,
                                stackPosition = stackPos,
                                dragOffset = finalDragOffset,
                                rotation = finalRotation,
                                elevation = finalElevation,
                                cardWidth = cardWidth,
                                cardHeight = cardHeight,
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
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${currentIndex + 1}/$totalEvents",
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
            Icon(
                imageVector = Icons.Outlined.UnfoldMore,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun ExpandedEventsList(
    events: List<CalendarEvent>,
    currentIndex: Int,
    onEventSelect: (Int) -> Unit,
    onCollapse: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.calendar_overlapping_events, events.size),
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            Surface(
                modifier = Modifier.clickable(onClick = onCollapse),
                color = colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.UnfoldLess,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.calendar_collapse),
                        color = colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
        events.forEachIndexed { index, event ->
            EventCard(
                event = event,
                variant = EventCardVariant.LIST,
                isSelected = index == currentIndex,
                onClick = { onEventSelect(index) }
            )
        }
    }
}

@Composable
private fun DeckCard(
    event: CalendarEvent,
    stackPosition: Int,
    dragOffset: Float,
    rotation: Float,
    elevation: Dp,
    cardWidth: Float,
    cardHeight: Dp,
    onClick: () -> Unit,
) {
    val density = LocalDensity.current
    val colorScheme = MaterialTheme.colorScheme
    val eventColor = CalendarUtils.getEventColor(event.eventType, colorScheme.primary)
    val eventStatus = remember(event) { event.resolveStatus() }

    val baseOffsetX = with(density) { CalendarConfig.Dimensions.STACK_OFFSET_X.toPx() * stackPosition }
    val baseOffsetY = with(density) { -CalendarConfig.Dimensions.STACK_OFFSET_Y.toPx() * stackPosition }
    val baseScale = 1f - (0.03f * stackPosition)

    val finalOffsetX = if (stackPosition == 0) dragOffset else baseOffsetX
    val zIndex = (10 - stackPosition).toFloat()

    Card(
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
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = if (stackPosition > 0) BorderStroke(
            0.5.dp,
            colorScheme.outlineVariant.copy(alpha = 0.1f)
        ) else null,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxSize()) {
                EventColorBar(
                    color = eventColor,
                    status = eventStatus,
                    width = CalendarConfig.Dimensions.COLOR_BAR_WIDTH,
                    cornerRadius = 12.dp // Match medium shape
                )
                EventCardContent(
                    event = event,
                    height = cardHeight,
                    eventStatus = eventStatus,
                    eventColor = eventColor
                )
            }
            // Overlay scuro per eventi passati
            if (eventStatus == EventStatus.ENDED || eventStatus == EventStatus.CANCELLED) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color.Black.copy(alpha = 0.4f))
                )
            }
        }
    }
}

@Composable
private fun EventCardContent(
    event: CalendarEvent,
    height: Dp,
    eventStatus: EventStatus,
    eventColor: Color
) {
    val colorScheme = MaterialTheme.colorScheme
    val showCompactContent = height < CalendarConfig.Heights.COMPACT_THRESHOLD
    val durationText =
        remember(event) { CalendarUtils.formatDuration(event.startDateTime, event.endDateTime) }

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
                text = event.title,
                color = colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = if (showCompactContent) 1 else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            EventStatusIndicator(
                event = event,
                eventStatus = eventStatus,
                eventColor = eventColor
            )
        }

        if (showCompactContent) {
            EventInfoChip(
                icon = Icons.Outlined.Schedule,
                text = "${event.startDateTime.format(CalendarUtils.timeFormatter)} - ${
                    event.endDateTime.format(CalendarUtils.timeFormatter)
                }",
                color = colorScheme.outline
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EventInfoChip(
                    icon = Icons.Outlined.Schedule,
                    text = "${event.startDateTime.format(CalendarUtils.timeFormatter)} - ${
                        event.endDateTime.format(CalendarUtils.timeFormatter)
                    }",
                    color = colorScheme.outline
                )
                EventInfoChip(
                    icon = Icons.Outlined.Timer,
                    text = durationText,
                    color = colorScheme.outline
                )
            }

            CalendarUtils.formatEventLocation(event.location, event.buildingCode)?.let { location ->
                EventInfoChip(Icons.Outlined.LocationOn, location, colorScheme.outline)
            }

            if (height >= CalendarConfig.Heights.SHOW_PROFESSOR_THRESHOLD) {
                event.teacherName?.let { professor ->
                    EventInfoChip(Icons.Outlined.Person, professor, colorScheme.outline)
                }
            }
        }
    }
}

@Composable
fun VerticalClipLayout(
    modifier: Modifier = Modifier,
    spacing: Dp = 0.dp,
    content: @Composable () -> Unit,
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
