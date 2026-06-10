package it.attendance100.mybicocca.ui.screen.map.state

/** One-shot map signals, delivered exactly once and never replayed across rotation. */
sealed interface MapOneShotEvent {
    /** The room-list refresh for the selected building failed. */
    data class RefreshFailed(val cause: Throwable) : MapOneShotEvent
}
