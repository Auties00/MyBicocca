package it.attendance100.mybicocca.ui.screen.registry.subscreen.refunds

/**
 * Stable `testTag` identifiers for the Rimborsi sheet ([RefundsListPage]), referenced by both the
 * screen and its UI tests so a user-visible copy change never breaks a test and a tag rename is a
 * compile error rather than a silently missed node. The screen applies them with `Modifier.testTag`;
 * tests anchor on the constants instead of on Italian text.
 *
 * [ROW_PREFIX] is concatenated with a refund's stable key (`refundKey()`) to address a specific
 * refund row in the list.
 */
object RefundsTestTags {
    const val ROOT = "refunds:root"
    const val STATE_LOADING = "refunds:state:loading"
    const val STATE_ERROR = "refunds:state:error"
    const val STATE_EMPTY = "refunds:state:empty"
    const val STATE_CONTENT = "refunds:state:content"
    const val ROW_PREFIX = "refunds:row:"

    fun row(key: Long): String = "$ROW_PREFIX$key"
}
