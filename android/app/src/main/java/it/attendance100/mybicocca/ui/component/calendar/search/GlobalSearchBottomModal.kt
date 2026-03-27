package it.attendance100.mybicocca.ui.component.calendar.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.ui.component.calendar.dialog.EventDetailScreen
import it.attendance100.mybicocca.ui.component.calendar.search.SearchListScreen
import it.attendance100.mybicocca.ui.component.calendar.search.SearchScreen
import it.attendance100.mybicocca.ui.component.calendar.search.rememberSearchModalState
import java.time.LocalDate

/**
 * ModalBottomSheet per la ricerca globale degli eventi.
 * Implementa navigazione interna con slide animation tra:
 * - Lista ricerca
 * - Dettaglio evento
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchBottomSheet(
    allEvents: List<CalendarEvent>,
    onDismiss: () -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    onDateClick: (LocalDate) -> Unit,
    primaryColor: Color,
    textColor: Color,
    grayColor: Color
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val navigationState = rememberSearchModalState()

    // Handle back navigation
    BackHandler(enabled = !navigationState.isOnSearch) {
        navigationState.goBack()
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!navigationState.isOnSearch) {
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
                        targetState is SearchScreen.EventDetail -> 1
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
                label = "searchScreenTransition"
            ) { screen ->
                when (screen) {
                    is SearchScreen.Search -> {
                        SearchListScreen(
                            allEvents = allEvents,
                            onEventClick = { event ->
                                navigationState.navigateToEventDetail(event)
                            },
                            onDateClick = { date ->
                                onDateClick(date)
                                onDismiss()
                            },
                            primaryColor = primaryColor,
                            textColor = textColor,
                            grayColor = grayColor,
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .padding(top = 24.dp, bottom = 16.dp)
                        )
                    }

                    is SearchScreen.EventDetail -> {
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
