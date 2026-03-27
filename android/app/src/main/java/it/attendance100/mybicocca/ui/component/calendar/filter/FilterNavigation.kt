package it.attendance100.mybicocca.ui.component.calendar.filter

import androidx.compose.runtime.*

/**
 * Schermate disponibili nel modal dei filtri.
 * Usato per la navigazione interna con animazione slide.
 */
sealed class FilterScreen {
    data object Main : FilterScreen()
    data object Location : FilterScreen()
}

/**
 * Stato di navigazione del modal filtri.
 * Gestisce la schermata corrente e le transizioni.
 */
@Stable
class FilterModalState {
    var currentScreen by mutableStateOf<FilterScreen>(FilterScreen.Main)
        private set
    fun navigateTo(screen: FilterScreen) {
        currentScreen = screen
    }

    fun goBack() {
        currentScreen = FilterScreen.Main
    }

    val isOnMain: Boolean
        get() = currentScreen == FilterScreen.Main
}

@Composable
fun rememberFilterModalState(): FilterModalState {
    return remember { FilterModalState() }
}
