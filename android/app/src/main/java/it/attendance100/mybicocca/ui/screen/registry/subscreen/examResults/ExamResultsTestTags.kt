package it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults

/**
 * Stable `testTag` identifiers for [ExamResultsPage] (the Esiti sheet), referenced by both the
 * screen and its UI tests so a user-visible copy change never breaks a test and a tag rename is a
 * compile error rather than a silently missed node. The page applies them with `Modifier.testTag`;
 * tests anchor on the constants instead of on Italian text.
 *
 * The root page's state markers ([STATE_LOADING], [STATE_ERROR], [STATE_EMPTY], [STATE_CONTENT])
 * are mutually exclusive per the outcome list's
 * [it.attendance100.mybicocca.core.state.Loadable]/[it.attendance100.mybicocca.core.state.SyncStatus]
 * pair. Pending and archived outcome rows are keyed by their stable identity via [item]. The detail
 * page's accept/reject pair and the reject-confirmation pair carry their own tags.
 */
object ExamResultsTestTags {
    const val ROOT = "esiti:root"
    const val STATE_LOADING = "esiti:state:loading"
    const val STATE_ERROR = "esiti:state:error"
    const val STATE_EMPTY = "esiti:state:empty"
    const val STATE_CONTENT = "esiti:state:content"

    const val ACCEPT_BUTTON = "esiti:detail:acceptButton"
    const val REJECT_BUTTON = "esiti:detail:rejectButton"

    const val REJECT_CONFIRM = "esiti:rejectConfirm"
    const val REJECT_KEEP = "esiti:rejectKeep"

    fun item(identity: String): String = "esiti:item:$identity"
}
