package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes

/**
 * Stable `testTag` identifiers for the Tasse sheet ([TaxesPage] and its hero detail overlay),
 * referenced by both the screen and its UI tests so a user-visible copy change never breaks a
 * test and a tag rename is a compile error rather than a silently missed node. The screen applies
 * them with `Modifier.testTag`; tests anchor on the constants instead of on Italian text.
 *
 * [INVOICE_CARD_PREFIX] is concatenated with an invoice id to address a specific fattura row, and
 * the detail overlay's pagoPA actions are tagged so a behaviour test can drive the payment flow.
 */
object TaxesTestTags {
    const val ROOT = "taxes:root"
    const val STATE_LOADING = "taxes:state:loading"
    const val STATE_ERROR = "taxes:state:error"
    const val STATE_EMPTY = "taxes:state:empty"
    const val STATE_CONTENT = "taxes:state:content"
    const val FILTER_SWITCH = "taxes:filterSwitch"
    const val INVOICE_CARD_PREFIX = "taxes:invoice:"
    const val DETAIL_ROOT = "taxes:detail:root"
    const val DETAIL_PAY_BUTTON = "taxes:detail:pay"
    const val DETAIL_CHECK_STATUS_BUTTON = "taxes:detail:checkStatus"
    const val DETAIL_CLOSE_BUTTON = "taxes:detail:close"

    fun invoiceCard(id: Long): String = "$INVOICE_CARD_PREFIX$id"
}
