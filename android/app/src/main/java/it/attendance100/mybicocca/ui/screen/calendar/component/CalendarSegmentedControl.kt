package it.attendance100.mybicocca.ui.screen.calendar.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.ui.screen.calendar.state.CalendarViewMode
import it.attendance100.mybicocca.util.rememberHapticManager
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

private val ItalianLocale: Locale = Locale.ITALIAN
private val IndicatorEasing = CubicBezierEasing(0.5f, 0.05f, 0.1f, 1f)
private const val IndicatorDurationMs = 320
private const val DragSettleDurationMs = 200

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

    val segments = remember(selectedDay, weekStart, selectedMonth) {
        listOf(
            Segment(CalendarViewMode.DAY, primary = dayPrimary(selectedDay), secondary = "Giorno"),
            Segment(CalendarViewMode.WEEK, primary = weekPrimary(weekStart), secondary = "Settimana"),
            Segment(CalendarViewMode.MONTH, primary = monthPrimary(selectedMonth), secondary = "Mese"),
        )
    }
    val lastIndex = segments.lastIndex
    val selectedIndex = segments.indexOfFirst { it.mode == viewMode }.coerceAtLeast(0)

    val scope = rememberCoroutineScope()
    val haptic = rememberHapticManager()
    val currentMode by rememberUpdatedState(viewMode)
    val currentOnSelect by rememberUpdatedState(onSelect)

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
        val maxOffsetPx = slotWidthPx * lastIndex
        val indicatorRadiusPx = with(density) { 18.dp.toPx() }
        val offsetX = remember { Animatable(selectedIndex * slotWidthPx) }
        var rawDragX by remember { mutableFloatStateOf(offsetX.value) }
        var currentSide by remember { mutableIntStateOf(selectedIndex) }
        var isDragging by remember { mutableStateOf(false) }

        LaunchedEffect(viewMode, slotWidthPx) {
            if (!isDragging) {
                offsetX.animateTo(
                    selectedIndex * slotWidthPx,
                    tween(IndicatorDurationMs, easing = IndicatorEasing),
                )
            }
        }

        val dragModifier = Modifier.pointerInput(slotWidthPx) {
            detectHorizontalDragGestures(
                onDragStart = {
                    isDragging = true
                    rawDragX = offsetX.value
                    currentSide = (rawDragX / slotWidthPx).roundToInt().coerceIn(0, lastIndex)
                },
                onDragEnd = {
                    isDragging = false
                    val targetMode = segments[currentSide].mode
                    scope.launch {
                        offsetX.animateTo(
                            currentSide * slotWidthPx,
                            tween(DragSettleDurationMs, easing = IndicatorEasing),
                        )
                        if (targetMode != currentMode) currentOnSelect(targetMode)
                    }
                },
                onDragCancel = {
                    isDragging = false
                    val restingIndex = segments.indexOfFirst { it.mode == currentMode }
                        .coerceAtLeast(0)
                    scope.launch {
                        offsetX.animateTo(
                            restingIndex * slotWidthPx,
                            tween(DragSettleDurationMs, easing = IndicatorEasing),
                        )
                    }
                },
            ) { change, dragAmount ->
                change.consume()
                rawDragX = (rawDragX + dragAmount).coerceIn(0f, maxOffsetPx)
                scope.launch { offsetX.snapTo(rawDragX) }

                val newSide = (rawDragX / slotWidthPx).roundToInt().coerceIn(0, lastIndex)
                if (newSide != currentSide) {
                    currentSide = newSide
                    haptic.tap()
                    val targetMode = segments[currentSide].mode
                    if (targetMode != currentMode) currentOnSelect(targetMode)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .then(dragModifier)
                .drawBehind {
                    drawRoundRect(
                        color = indicatorColor,
                        topLeft = Offset(offsetX.value, 0f),
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
                    onClick = {
                        haptic.tap()
                        currentOnSelect(segment.mode)
                    },
                    modifier = Modifier.weight(1f),
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
            text = secondary.uppercase(ItalianLocale),
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
        .getDisplayName(TextStyle.SHORT_STANDALONE, ItalianLocale)
        .replaceFirstChar { it.uppercase() }
        .trimEnd('.')
    return "$dow ${date.dayOfMonth}"
}

private fun weekPrimary(weekStart: LocalDate): String =
    "${weekStart.dayOfMonth}–${weekStart.plusDays(6).dayOfMonth}"

private fun monthPrimary(yearMonth: YearMonth): String =
    yearMonth.month
        .getDisplayName(TextStyle.FULL_STANDALONE, ItalianLocale)
        .replaceFirstChar { it.uppercase() }
