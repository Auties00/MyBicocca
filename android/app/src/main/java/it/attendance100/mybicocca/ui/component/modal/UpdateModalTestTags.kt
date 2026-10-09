package it.attendance100.mybicocca.ui.component.modal

/**
 * Stable `testTag` identifiers for [UpdateModalSheet], referenced by the sheet and its UI tests so
 * a copy change never breaks a test and a tag rename is a compile error rather than a silently
 * missed node.
 *
 * The three action tags are mutually exclusive by construction — the sheet shows the progress bar,
 * the install button, or the download button — which is what lets a test assert the state it is in
 * rather than only what it clicked.
 */
object UpdateModalTestTags {
    const val DOWNLOAD = "updateModal:download"
    const val PROGRESS = "updateModal:progress"
    const val INSTALL = "updateModal:install"

    /** "Not now", or the channel-change undo. Always present, always enabled. */
    const val LEAVE = "updateModal:leave"
}
