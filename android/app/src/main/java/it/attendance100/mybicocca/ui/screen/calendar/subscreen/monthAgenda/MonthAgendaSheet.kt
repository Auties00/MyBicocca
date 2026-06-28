package it.attendance100.mybicocca.ui.screen.calendar.subscreen.monthAgenda

import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
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
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.CalendarEventId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
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

private val DisplayLocale = Locale.getDefault()
private const val SheetTopMargin = 64
private const val ExpansionThreshold = 0.5f

/**
 * Bottom agenda companion of the month view: a popup-window sheet pinned to the bottom
 * edge listing the selected day's events under an "OGGI"/"SELEZIONATO" date header with
 * the event count, or a "Nessun impegno" placeholder when the day is free. Collapsed it
 * fills the space measured under the month grid; dragging raises it to roughly four
 * fifths of the screen while a second, full-window popup fades in a tap-to-dismiss scrim
 * behind it. Releases snap fully open or closed on a fast fling or at half progress.
 *
 * While expanded, tapping a row toggles it open inline into the full
 * [EventDetailContent]; while collapsed, the same tap opens the event detail sheet. The
 * inline expansion resets whenever the sheet collapses or the day changes.
 *
 * Living in popup windows, the sheet escapes every in-content cover — the caller decides
 * when it may show at all.
 *
 * @param progress caller-owned expansion fraction, 0 collapsed to 1 expanded; the sheet
 *   drives it through drags and snap animations.
 * @param presence how present month mode itself is, 0..1: the sheet fades with it and
 *   slides off the bottom edge as it approaches 0.
 * @param sheetHeight measured pixel height of the free region under the month grid that
 *   the collapsed sheet should occupy.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MonthAgendaSheet(
    selectedDay: LocalDate,
    events: List<CalendarEvent>,
    onEventClick: (CalendarEvent) -> Unit,
    elearningCoursesFor: (CalendarEvent) -> List<EnrolledCourse>,
    onOpenCourse: (CourseId) -> Unit,
    onOpenAssignment: (assignmentId: Int, courseId: Int) -> Unit,
    onOpenReservation: (CalendarEvent) -> Unit,
    progress: Animatable<Float, *>,
    presence: Float,
    bottomNavBarPadding: PaddingValues,
    sheetHeight: Int,
) {
    val haptic = rememberHapticManager()
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

    LaunchedEffect(progress) {
        var wasAtEdge = progress.value <= 0f || progress.value >= 1f
        androidx.compose.runtime.snapshotFlow { progress.value }.collect { current ->
            val isAtEdge = current <= 0f || current >= 1f
            if (wasAtEdge != isAtEdge) {
                haptic.feather()
            }
            wasAtEdge = isAtEdge
        }
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
    val totalBottomMarginPx =
        (baseBottomMarginPx - slideOffPx * (1f - presence)).coerceAtLeast(0f).roundToInt()
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
            elearningCoursesFor = elearningCoursesFor,
            onOpenCourse = onOpenCourse,
            onOpenAssignment = onOpenAssignment,
            onOpenReservation = onOpenReservation,
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

/**
 * The agenda surface itself. Its height is imposed at measure time, interpolated between
 * the collapsed and expanded bounds from [progressProvider] — read in the layout phase so
 * drag frames re-measure without recomposing — while vertical drags feed the owner's
 * progress through [onDragDelta]/[onDragStopped].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AgendaSheetSurface(
    selectedDay: LocalDate,
    events: List<CalendarEvent>,
    isToday: Boolean,
    onEventClick: (CalendarEvent) -> Unit,
    elearningCoursesFor: (CalendarEvent) -> List<EnrolledCourse>,
    onOpenCourse: (CourseId) -> Unit,
    onOpenAssignment: (assignmentId: Int, courseId: Int) -> Unit,
    onOpenReservation: (CalendarEvent) -> Unit,
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
                val heightPx =
                    lerp(collapsedPx, expandedMaxPx, progressProvider()).coerceAtLeast(0f)
                        .roundToInt()
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
                    text = stringResource(R.string.calendar_no_events),
                    color = scheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    events.forEachIndexed { index, e ->
                        AgendaRow(
                            event = e,
                            isFirst = index == 0,
                            isLast = index == events.lastIndex,
                            isInlineExpanded = expandedEventId == e.id,
                            elearningCourses = elearningCoursesFor(e),
                            onOpenCourse = onOpenCourse,
                            onOpenAssignment = onOpenAssignment,
                            onOpenReservation = onOpenReservation,
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
            text = if (isToday) stringResource(R.string.agenda_date_today) else stringResource(R.string.agenda_date_selected),
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
            text = pluralStringResource(R.plurals.agenda_event_count, eventCount, eventCount),
            color = scheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp,
        )
    }
}

/**
 * One event of the agenda list, in the segmented M3E group scheme the edifici sheet uses:
 * 28dp corners cap the group's ends, 6dp where rows touch. The expanded row morphs into a
 * fully rounded standalone card, rises one tonal step and unfolds [EventDetailContent]
 * under its header while the chevron flips; the event's accent bar stays as the leading
 * edge throughout.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AgendaRow(
    event: CalendarEvent,
    isFirst: Boolean,
    isLast: Boolean,
    isInlineExpanded: Boolean,
    elearningCourses: List<EnrolledCourse>,
    onOpenCourse: (CourseId) -> Unit,
    onOpenAssignment: (assignmentId: Int, courseId: Int) -> Unit,
    onOpenReservation: (CalendarEvent) -> Unit,
    onClick: () -> Unit,
) {
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val now by rememberCurrentTime()
    val colors = event.colorsFor(now)
    val transition = updateTransition(isInlineExpanded, label = "agenda_row")
    val topCorner by transition.animateDp(
        transitionSpec = { motion.defaultSpatialSpec() },
        label = "top_corner",
    ) { if (it || isFirst) 28.dp else 6.dp }
    val bottomCorner by transition.animateDp(
        transitionSpec = { motion.defaultSpatialSpec() },
        label = "bottom_corner",
    ) { if (it || isLast) 28.dp else 6.dp }
    val containerColor by transition.animateColor(
        transitionSpec = { motion.defaultEffectsSpec() },
        label = "container_color",
    ) { if (it) scheme.surfaceContainerHigh else scheme.surfaceContainer }
    val arrowRotation by transition.animateFloat(
        transitionSpec = { motion.defaultSpatialSpec() },
        label = "arrow",
    ) { if (it) 180f else 0f }
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = sizeSpec),
        shape = RoundedCornerShape(
            topStart = topCorner,
            topEnd = topCorner,
            bottomStart = bottomCorner,
            bottomEnd = bottomCorner,
        ),
        color = containerColor,
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
                    .background(colors.accent),
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = { haptic.tap(); onClick() })
                        .padding(start = 13.dp, top = 16.dp, bottom = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val parts = listOfNotNull(
                            event.formatTimeRange(),
                            event.locationLine().takeIf { it.isNotBlank() },
                        ).joinToString(" · ")
                        Text(
                            text = parts,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = if (isInlineExpanded) stringResource(R.string.agenda_collapse) else stringResource(
                            R.string.agenda_expand
                        ),
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .rotate(arrowRotation),
                    )
                }
                if (isInlineExpanded) {
                    EventDetailContent(
                        event = event,
                        elearningCourses = elearningCourses,
                        onOpenCourse = onOpenCourse,
                        onOpenAssignment = onOpenAssignment,
                        onOpenReservation = onOpenReservation,
                        modifier = Modifier.padding(start = 13.dp, end = 16.dp, bottom = 16.dp),
                    )
                }
            }
        }
    }
}

private fun monthName(monthValue: Int): String =
    Month.of(monthValue).getDisplayName(TextStyle.FULL_STANDALONE, DisplayLocale)
