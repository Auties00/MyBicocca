package it.attendance100.mybicocca.ui.component.calendar.header

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.screen.calendar.CalendarViewMode
import it.attendance100.mybicocca.ui.component.calendar.CalendarUtils
import java.time.LocalDate
import java.time.YearMonth
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarTopBar(
    currentMonth: YearMonth,
    viewMode: CalendarViewMode,
    onViewModeChange: (CalendarViewMode) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit,
    onMonthClick: () -> Unit,
    primaryColor: Color
) {
    val locale = Locale.getDefault()
    val monthYearFormatter = CalendarUtils.monthYearFormatter(locale)
    val textColor = MaterialTheme.colorScheme.onSurface

    CenterAlignedTopAppBar(
        title = {
            MonthNavigation(
                currentMonth = currentMonth,
                monthYearFormatter = monthYearFormatter,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onClick = onMonthClick,
                textColor = textColor
            )
        },
        navigationIcon = {
            TodayButton(
                onClick = onToday,
                primaryColor = primaryColor
            )
        },
        actions = {
            ViewModeToggle(
                currentMode = viewMode,
                onModeChange = onViewModeChange,
                primaryColor = primaryColor
            )
        },
        windowInsets = WindowInsets(0),
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun TodayButton(
    onClick: () -> Unit,
    primaryColor: Color
) {
    FilledTonalIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = primaryColor.copy(alpha = 0.12f),
            contentColor = primaryColor
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.CalendarToday,
                contentDescription = stringResource(R.string.calendar_today)
            )
            Text(
                text = LocalDate.now().dayOfMonth.toString(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 7.dp)
            )
        }
    }
}

@Composable
private fun MonthNavigation(
    currentMonth: YearMonth,
    monthYearFormatter: java.time.format.DateTimeFormatter,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onClick: () -> Unit,
    textColor: Color
) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                Icons.Default.ChevronLeft,
                stringResource(R.string.calendar_previous_month),
                tint = textColor
            )
        }
        Text(
            text = currentMonth.format(monthYearFormatter).replaceFirstChar { it.uppercase() },
            color = textColor,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        IconButton(onClick = onNextMonth) {
            Icon(
                Icons.Default.ChevronRight,
                stringResource(R.string.calendar_next_month),
                tint = textColor
            )
        }
    }
}

/**
 * Toggle per cambiare modalità vista con animazione rotazione.
 */
@Composable
fun ViewModeToggle(
    currentMode: CalendarViewMode,
    onModeChange: (CalendarViewMode) -> Unit,
    primaryColor: Color
) {
    var isAnimating by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isAnimating) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        finishedListener = { isAnimating = false },
        label = "viewModeRotation"
    )

    FilledTonalIconButton(
        onClick = {
            isAnimating = true
            val nextMode = when (currentMode) {
                CalendarViewMode.LIST -> CalendarViewMode.WEEK
                CalendarViewMode.WEEK -> CalendarViewMode.MONTH
                CalendarViewMode.MONTH -> CalendarViewMode.LIST
            }
            onModeChange(nextMode)
        },
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = primaryColor.copy(alpha = 0.12f),
            contentColor = primaryColor
        )
    ) {
        Icon(
            imageVector = when (currentMode) {
                CalendarViewMode.LIST -> Icons.Outlined.ViewAgenda
                CalendarViewMode.WEEK -> Icons.Outlined.CalendarViewWeek
                CalendarViewMode.MONTH -> Icons.Outlined.CalendarMonth
            },
            contentDescription = stringResource(R.string.calendar_view_mode),
            modifier = Modifier.graphicsLayer {
                rotationY = rotation
            }
        )
    }
}
