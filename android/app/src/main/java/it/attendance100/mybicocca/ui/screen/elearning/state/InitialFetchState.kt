package it.attendance100.mybicocca.ui.screen.elearning.state

/**
 * Lifecycle of the local course cache before its first successful population. Derived from the
 * raw pre-filter Room snapshot so a filter that matches nothing is never mistaken for a cold
 * cache. Drives the home tab's full-screen loading/error swap (filter bar hidden) — once
 * [Settled], the regular content/empty states take over.
 */
sealed interface InitialFetchState {
    data object InProgress : InitialFetchState
    data class Failed(val cause: Throwable) : InitialFetchState
    data object Settled : InitialFetchState
}
