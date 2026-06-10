package it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli

/**
 * Stable `testTag` identifiers for [AppelliPage] (the unified prenotazioni + booking sheet),
 * referenced by both the screen and its UI tests so a user-visible copy change never breaks a
 * test and a tag rename is a compile error rather than a silently missed node. The screen applies
 * them with `Modifier.testTag`; tests anchor on the constants instead of on Italian text.
 *
 * The root page's state markers ([STATE_LOADING], [STATE_ERROR], [STATE_EMPTY], [STATE_CONTENT])
 * are mutually exclusive: exactly one shows per the booked-list [it.attendance100.mybicocca.core.state.Loadable]
 * /[it.attendance100.mybicocca.core.state.SyncStatus] pair. Active bookings are keyed by their
 * stable identity via [item].
 */
object AppelliTestTags {
    const val ROOT = "appelli:root"
    const val STATE_LOADING = "appelli:state:loading"
    const val STATE_ERROR = "appelli:state:error"
    const val STATE_EMPTY = "appelli:state:empty"
    const val STATE_CONTENT = "appelli:state:content"
    const val PRENOTA_BUTTON = "appelli:prenotaButton"

    /** Detail page: the destructive "Cancella" action that opens the cancel confirmation. */
    const val DETAIL_CANCEL_BUTTON = "appelli:detail:cancelButton"

    /** Cancel-confirmation page actions. */
    const val CANCEL_CONFIRM = "appelli:cancelConfirm"
    const val CANCEL_KEEP = "appelli:cancelKeep"

    fun item(identityKey: String): String = "appelli:item:$identityKey"
}
