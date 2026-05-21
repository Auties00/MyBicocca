package it.attendance100.mybicocca.ui.screen.calendar.subscreen.monthAgenda

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.CalendarEventId
import it.attendance100.mybicocca.ui.screen.calendar.ext.colorsFor
import it.attendance100.mybicocca.ui.screen.calendar.ext.formatTimeRange
import it.attendance100.mybicocca.ui.screen.calendar.ext.locationLine
import it.attendance100.mybicocca.ui.screen.calendar.ext.rememberCurrentTime
import it.attendance100.mybicocca.ui.screen.calendar.subscreen.eventDetail.EventDetailContent
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

private val ItalianLocale = Locale.ITALIAN
private const val SheetTopMargin = 64
private const val ExpansionThreshold = 0.5f

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MonthAgendaSheet(
    selectedDay: LocalDate,
    events: List<CalendarEvent>,
    onEventClick: (CalendarEvent) -> Unit,
    progress: Animatable<Float, *>,
    presence: Float,
    bottomNavBarPadding: PaddingValues,
    sheetHeight: Int,
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val coroutineScope = rememberCoroutineScope()
    val scheme = MaterialTheme.colorScheme
    val today by rememberCurrentTime()
    val isToday = selectedDay == today.toLocalDate()

    val collapsedPx = sheetHeight.toFloat() - SheetTopMargin
    val expandedMaxPx = with(density) { (configuration.screenHeightDp.dp * 0.79f).toPx() }
    val bottomNavPx = with(density) { bottomNavBarPadding.calculateBottomPadding().toPx() }
    val slideOffPx = with(density) { 60.dp.toPx() }
    val rangePx = (expandedMaxPx - collapsedPx).coerceAtLeast(1f)

    val isSheetExpanded = progress.value >= ExpansionThreshold
    var expandedEventId by remember { mutableStateOf<CalendarEventId?>(null) }
    LaunchedEffect(isSheetExpanded, selectedDay) {
        if (!isSheetExpanded) expandedEventId = null
    }

    val scrimColor = scheme.scrim
    Popup(
        popupPositionProvider = FullScreenPositionProvider,
        properties = PopupProperties(focusable = false, clippingEnabled = false),
    ) {
        if (progress.value > 0.005f && presence > 0.005f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = progress.value * 0.32f * presence }
                    .background(scrimColor)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) {
                        coroutineScope.launch { progress.animateTo(0f) }
                    },
            )
        }
    }

    val baseBottomMarginPx = lerp(bottomNavPx, 0f, progress.value)
    val totalBottomMarginPx = (baseBottomMarginPx - slideOffPx * (1f - presence)).roundToInt()
    val positionProvider = remember(totalBottomMarginPx) {
        AgendaPositionProvider(bottomMarginPx = totalBottomMarginPx)
    }

    Popup(
        popupPositionProvider = positionProvider,
        properties = PopupProperties(focusable = false, clippingEnabled = false),
    ) {
        AgendaSheetSurface(
            selectedDay = selectedDay,
            events = events,
            isToday = isToday,
            onEventClick = onEventClick,
            isSheetExpanded = isSheetExpanded,
            expandedEventId = expandedEventId,
            onToggleExpand = { id ->
                expandedEventId = if (expandedEventId == id) null else id
            },
            collapsedPx = collapsedPx,
            expandedMaxPx = expandedMaxPx,
            progressProvider = { progress.value },
            presence = presence,
            onDragDelta = { delta ->
                coroutineScope.launch {
                    val next = (progress.value - delta / rangePx).coerceIn(0f, 1f)
                    progress.snapTo(next)
                }
            },
            onDragStopped = { velocity ->
                coroutineScope.launch {
                    val target = when {
                        velocity < -800f -> 1f
                        velocity > 800f -> 0f
                        progress.value > 0.5f -> 1f
                        else -> 0f
                    }
                    progress.animateTo(target)
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AgendaSheetSurface(
    selectedDay: LocalDate,
    events: List<CalendarEvent>,
    isToday: Boolean,
    onEventClick: (CalendarEvent) -> Unit,
    isSheetExpanded: Boolean,
    expandedEventId: CalendarEventId?,
    onToggleExpand: (CalendarEventId) -> Unit,
    collapsedPx: Float,
    expandedMaxPx: Float,
    progressProvider: () -> Float,
    presence: Float,
    onDragDelta: (Float) -> Unit,
    onDragStopped: (Float) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val draggableState = rememberDraggableState(onDelta = onDragDelta)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .layout { measurable, constraints ->
                val heightPx = lerp(collapsedPx, expandedMaxPx, progressProvider()).roundToInt()
                val placeable = measurable.measure(
                    constraints.copy(minHeight = heightPx, maxHeight = heightPx),
                )
                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }
            .graphicsLayer { alpha = presence }
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStopped = { velocity -> onDragStopped(velocity) },
            ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = scheme.surfaceContainerLow,
        tonalElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
        ) {
            DragHandle()
            DateLabel(selectedDay = selectedDay, eventCount = events.size, isToday = isToday)
            Spacer(Modifier.height(10.dp))

            if (events.isEmpty()) {
                Text(
                    text = "Nessun impegno",
                    color = scheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    events.forEach { e ->
                        AgendaRow(
                            event = e,
                            isInlineExpanded = expandedEventId == e.id,
                            progressProvider = progressProvider,
                            onClick = {
                                if (isSheetExpanded) onToggleExpand(e.id)
                                else onEventClick(e)
                            },
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

private class AgendaPositionProvider(
    private val bottomMarginPx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset = IntOffset(
        x = (windowSize.width - popupContentSize.width) / 2,
        y = windowSize.height - popupContentSize.height - bottomMarginPx,
    )
}

private val FullScreenPositionProvider = object : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset = IntOffset.Zero
}

@Composable
private fun DragHandle(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(scheme.outlineVariant),
        )
    }
}

@Composable
private fun DateLabel(selectedDay: LocalDate, eventCount: Int, isToday: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = if (isToday) "OGGI" else "SELEZIONATO",
            color = scheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp,
        )
        Text(
            text = "%d %s".format(selectedDay.dayOfMonth, monthName(selectedDay.monthValue)),
            color = scheme.onSurface,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.2).sp,
        )
        Text(
            text = if (eventCount == 1) "1 evento" else "$eventCount eventi",
            color = scheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp,
        )
    }
}

@Composable
private fun AgendaRow(
    event: CalendarEvent,
    isInlineExpanded: Boolean,
    progressProvider: () -> Float,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val now by rememberCurrentTime()
    val colors = event.colorsFor(now)
    val arrowRotation by animateFloatAsState(
        targetValue = if (isInlineExpanded) 180f else 0f,
        label = "agenda_row_arrow_rotation",
    )
    val tapExpansion = animateFloatAsState(
        targetValue = if (isInlineExpanded) 1f else 0f,
        label = "agenda_row_inline_expansion",
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = scheme.surface,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .heightIn(min = 42.dp)
                    .background(colors.accent),
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onClick)
                        .padding(start = 10.dp, top = 8.dp, bottom = 8.dp, end = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = event.title,
                            color = scheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val parts = listOfNotNull(
                            event.formatTimeRange(),
                            event.locationLine().takeIf { it.isNotBlank() },
                        ).joinToString(" · ")
                        Text(
                            text = parts,
                            color = scheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .rotate(arrowRotation),
                    )
                }
                if (isInlineExpanded || tapExpansion.value > 0.001f) {
                    Box(
                        modifier = Modifier
                            .padding(start = 10.dp, end = 10.dp, bottom = 8.dp)
                            .graphicsLayer {
                                alpha = tapExpansion.value * cardExpansionFactor(progressProvider())
                            }
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints.copy(minHeight = 0))
                                val expansion = tapExpansion.value * cardExpansionFactor(progressProvider())
                                val h = (placeable.height * expansion).roundToInt()
                                layout(placeable.width, h) { placeable.place(0, 0) }
                            },
                    ) {
                        EventDetailContent(
                            event = event,
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun monthName(monthValue: Int): String =
    Month.of(monthValue).getDisplayName(TextStyle.FULL_STANDALONE, ItalianLocale)

private fun cardExpansionFactor(progress: Float): Float {
    if (progress <= ExpansionThreshold) return 0f
    return ((progress - ExpansionThreshold) / (1f - ExpansionThreshold)).coerceIn(0f, 1f)
}
