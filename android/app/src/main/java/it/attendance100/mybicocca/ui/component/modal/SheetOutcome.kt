package it.attendance100.mybicocca.ui.component.modal

/**
 * The outcome of a completed in-sheet action. Sheets surface outcomes as a dedicated result
 * page (see SheetResultPage) instead of a transient snackbar; the variant picks the icon and
 * semantic tint, and an Error can carry its [Throwable] cause for user-readable body copy.
 */
sealed interface SheetOutcome {
    val title: String

    data class Success(override val title: String, val body: String? = null) : SheetOutcome
    data class Info(override val title: String, val body: String? = null) : SheetOutcome
    data class Error(override val title: String, val cause: Throwable? = null, val body: String? = null) : SheetOutcome
}
