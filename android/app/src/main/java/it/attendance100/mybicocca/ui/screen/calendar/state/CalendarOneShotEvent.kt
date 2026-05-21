package it.attendance100.mybicocca.ui.screen.calendar.state

sealed interface CalendarOneShotEvent {
    data class RefreshFailed(val cause: Throwable) : CalendarOneShotEvent
    data object RequireSignIn : CalendarOneShotEvent
}
