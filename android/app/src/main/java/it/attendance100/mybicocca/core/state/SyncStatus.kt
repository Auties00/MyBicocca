package it.attendance100.mybicocca.core.state

/**
 * State of a background refresh against the remote platforms, kept independent of the data
 * stream: screens keep rendering cached data whatever this value is.
 *
 * ViewModels expose it as its own `StateFlow`, translating a throwing repository refresh into
 * [Failed] via `runCatching`. The UI maps [Refreshing] to pull-to-refresh spinners or progress
 * indicators over the (possibly stale) content, and [Failed] to a banner or snackbar that never
 * discards what is shown.
 */
sealed interface SyncStatus {
    /** No refresh in flight and the last one did not fail. */
    data object Idle : SyncStatus

    /** A refresh is in flight; data on screen may be stale until it lands. */
    data object Refreshing : SyncStatus

    /** The last refresh threw; [cause] feeds the user-facing error message mapping. */
    data class Failed(val cause: Throwable) : SyncStatus
}
