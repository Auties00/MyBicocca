package it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher

import it.attendance100.mybicocca.domain.model.account.AccountId

/**
 * Stable `testTag` identifiers for [AccountSwitcherSheet] and its roster scene, referenced by
 * both the sheet and its UI tests so a user-visible copy change never breaks a test and a tag
 * rename is a compile error rather than a silently missed node. The roster applies them with
 * `Modifier.testTag`; tests anchor on the constants instead of on Italian text. Account rows are
 * keyed by [AccountId] via [account] so a populated roster can be asserted row by row.
 */
object AccountSwitcherTestTags {
    const val ROSTER = "accountSwitcher:roster"
    const val SETTINGS_SHORTCUT = "accountSwitcher:settingsShortcut"
    const val ADD_ACCOUNT = "accountSwitcher:addAccount"
    const val ACCOUNT_PREFIX = "accountSwitcher:account:"

    fun account(id: AccountId) = "$ACCOUNT_PREFIX${id.value}"
}
