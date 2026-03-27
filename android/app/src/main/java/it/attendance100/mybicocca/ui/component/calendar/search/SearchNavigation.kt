package it.attendance100.mybicocca.ui.component.calendar.search

import androidx.compose.runtime.*
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent

/**
 * Schermate disponibili nel modal di ricerca.
 * Usato per la navigazione interna con animazione slide.
 */
sealed class SearchScreen {
    data object Search : SearchScreen()
    data class EventDetail(val event: CalendarEvent) : SearchScreen()
}

/**
 * Stato di navigazione del modal di ricerca.
 * Gestisce la schermata corrente e le transizioni.
 */
@Stable
class SearchModalState {
    var currentScreen by mutableStateOf<SearchScreen>(SearchScreen.Search)
        private set

    fun navigateToEventDetail(event: CalendarEvent) {
        currentScreen = SearchScreen.EventDetail(event)
    }

    fun goBack() {
        currentScreen = SearchScreen.Search
    }

    val isOnSearch: Boolean
        get() = currentScreen == SearchScreen.Search

    val selectedEvent: CalendarEvent?
        get() = (currentScreen as? SearchScreen.EventDetail)?.event
}

@Composable
fun rememberSearchModalState(): SearchModalState {
    return remember { SearchModalState() }
}
