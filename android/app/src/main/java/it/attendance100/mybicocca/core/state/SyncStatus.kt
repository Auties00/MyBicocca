package it.attendance100.mybicocca.core.state

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Refreshing : SyncStatus
    data class Failed(val cause: Throwable) : SyncStatus
}
