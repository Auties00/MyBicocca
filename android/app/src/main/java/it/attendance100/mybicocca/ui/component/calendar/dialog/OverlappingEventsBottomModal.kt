package it.attendance100.mybicocca.ui.component.calendar.dialog

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.startDateTime
import it.attendance100.mybicocca.data.model.calendar.endDateTime
import it.attendance100.mybicocca.ui.component.calendar.dialog.EventDetailScreen
import it.attendance100.mybicocca.ui.component.calendar.EventCard
import it.attendance100.mybicocca.ui.component.calendar.EventCardVariant
import it.attendance100.mybicocca.ui.component.calendar.OverlapCalculator
import it.attendance100.mybicocca.ui.component.calendar.OverlapGroup

/**
 * Schermate disponibili nel modal degli eventi sovrapposti.
 */
private sealed class OverlapScreen {
    data object List : OverlapScreen()
    data class EventDetail(val event: CalendarEvent) : OverlapScreen()
}

/**
 * Stato di navigazione del modal eventi sovrapposti.
 */
@Stable
private class OverlapModalState {
    var currentScreen by mutableStateOf<OverlapScreen>(OverlapScreen.List)
        private set

    fun navigateToEventDetail(event: CalendarEvent) {
        currentScreen = OverlapScreen.EventDetail(event)
    }

    fun goBack() {
        currentScreen = OverlapScreen.List
    }

    val isOnList: Boolean
        get() = currentScreen == OverlapScreen.List
}

@Composable
private fun rememberOverlapModalState(): OverlapModalState {
    return remember { OverlapModalState() }
}

/**
 * ModalBottomSheet per visualizzare eventi sovrapposti.
 * Implementa navigazione interna con slide animation tra:
 * - Lista eventi sovrapposti
 * - Dettaglio evento
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlappingEventsBottomSheet(
    group: OverlapGroup,
    onDismiss: () -> Unit,
    primaryColor: Color
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val navigationState = rememberOverlapModalState()
    val sortedEvents = remember(group) { group.events.sortedBy { it.startDateTime } }
    val timeSpan = remember(group) { OverlapCalculator.formatGroupTimeSpan(group) }

    // Handle back navigation
    BackHandler(enabled = !navigationState.isOnList) {
        navigationState.goBack()
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!navigationState.isOnList) {
                navigationState.goBack()
            } else {
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = { WindowInsets(0) },
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            AnimatedContent(
                targetState = navigationState.currentScreen,
                transitionSpec = {
                    val direction = when {
                        targetState is OverlapScreen.EventDetail -> 1
                        else -> -1
                    }
                    slideInHorizontally(
                        animationSpec = tween(280, easing = FastOutSlowInEasing),
                        initialOffsetX = { fullWidth -> direction * fullWidth }
                    ) + fadeIn(tween(200)) togetherWith slideOutHorizontally(
                        animationSpec = tween(280, easing = FastOutSlowInEasing),
                        targetOffsetX = { fullWidth -> -direction * fullWidth }
                    ) + fadeOut(tween(150))
                },
                label = "overlapScreenTransition"
            ) { screen ->
                when (screen) {
                    is OverlapScreen.List -> {
                        OverlappingEventsListScreen(
                            events = sortedEvents,
                            timeSpan = timeSpan,
                            onEventClick = { event ->
                                navigationState.navigateToEventDetail(event)
                            },
                            primaryColor = primaryColor,
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .padding(top = 24.dp, bottom = 24.dp)
                        )
                    }

                    is OverlapScreen.EventDetail -> {
                        EventDetailScreen(
                            event = screen.event,
                            onBack = { navigationState.goBack() },
                            primaryColor = primaryColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Lista degli eventi sovrapposti con header.
 */
@Composable
private fun OverlappingEventsListScreen(
    events: List<CalendarEvent>,
    timeSpan: String,
    onEventClick: (CalendarEvent) -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val grayColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Big Count
            Text(
                text = events.size.toString(),
                fontSize = 80.sp,
                lineHeight = 80.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                letterSpacing = (-4).sp
            )

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 12.dp)
                    .width(2.dp)
                    .background(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.extraSmall
                    )
            )

            // Text Info
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.calendar_events),
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-1).sp
                )

                Text(
                    text = timeSpan,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }

        // Event list
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            events.forEach { event ->
                EventCard(
                    event = event,
                    variant = EventCardVariant.TIMELINE,
                    onClick = { onEventClick(event) },
                    trailingContent = {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = grayColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }
        }
    }
}
