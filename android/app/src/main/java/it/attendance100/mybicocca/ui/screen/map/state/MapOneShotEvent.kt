package it.attendance100.mybicocca.ui.screen.map.state

sealed interface MapOneShotEvent {
    data class RefreshFailed(val cause: Throwable) : MapOneShotEvent
}
