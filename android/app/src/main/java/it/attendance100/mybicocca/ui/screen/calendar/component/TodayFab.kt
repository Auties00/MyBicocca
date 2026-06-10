package it.attendance100.mybicocca.ui.screen.calendar.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import it.attendance100.mybicocca.ui.screen.calendar.ext.rememberCurrentTime
import it.attendance100.mybicocca.ui.screen.calendar.ext.visibleWeekDays
import it.attendance100.mybicocca.ui.screen.calendar.state.CalendarViewMode
import java.time.LocalDate
import java.time.YearMonth

private val BottomEdgePadding = 16.dp
private val EndEdgePadding = 16.dp

/**
 * "Vai a oggi" extended FAB of the calendar tab. It renders in its own popup window
 * pinned to the bottom-end corner above the navigation bar, floating over every view mode
 * without being clipped by their scrolling content; because a popup escapes all
 * in-content covers, the caller decides when it may show at all.
 *
 * It appears — scale plus fade on the expressive motion scheme — only while the visible
 * day, week or month excludes today, and in month mode it additionally shrinks and fades
 * proportionally to [agendaProgress] so it gets out of the agenda sheet's way as the
 * sheet expands.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TodayFab(
    viewMode: CalendarViewMode,
    selectedDay: LocalDate,
    weekStart: LocalDate,
    selectedMonth: YearMonth,
    agendaProgress: Float,
    bottomNavBarPadding: PaddingValues,
    onJumpToToday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val now by rememberCurrentTime()
    val today = now.toLocalDate()

    val needsJump = when (viewMode) {
        CalendarViewMode.DAY -> selectedDay != today
        CalendarViewMode.WEEK -> today !in visibleWeekDays(weekStart)
        CalendarViewMode.MONTH -> selectedMonth != YearMonth.from(today)
    }
    val agendaInverse = if (viewMode == CalendarViewMode.MONTH) {
        (1f - agendaProgress).coerceIn(0f, 1f)
    } else 1f

    val density = LocalDensity.current
    val bottomMarginPx = with(density) {
        (bottomNavBarPadding.calculateBottomPadding() + BottomEdgePadding).toPx().toInt()
    }
    val endMarginPx = with(density) { EndEdgePadding.toPx().toInt() }
    val positionProvider = remember(bottomMarginPx, endMarginPx) {
        FabPositionProvider(endMarginPx, bottomMarginPx)
    }
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    Popup(
        popupPositionProvider = positionProvider,
        properties = PopupProperties(focusable = false, clippingEnabled = false),
    ) {
        AnimatedVisibility(
            visible = needsJump,
            enter = scaleIn(animationSpec = spatialSpec) + fadeIn(animationSpec = effectsSpec),
            exit = scaleOut(animationSpec = spatialSpec) + fadeOut(animationSpec = effectsSpec),
            modifier = modifier,
        ) {
            Box(
                modifier = Modifier.graphicsLayer {
                    scaleX = agendaInverse
                    scaleY = agendaInverse
                    alpha = agendaInverse
                },
            ) {
                ExtendedFloatingActionButton(
                    onClick = onJumpToToday,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Today,
                            contentDescription = null,
                        )
                    },
                    text = { Text("Vai a oggi") },
                )
            }
        }
    }
}

private class FabPositionProvider(
    private val endMarginPx: Int,
    private val bottomMarginPx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset = IntOffset(
        x = windowSize.width - popupContentSize.width - endMarginPx,
        y = windowSize.height - popupContentSize.height - bottomMarginPx,
    )
}
