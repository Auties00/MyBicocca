package it.attendance100.mybicocca.ui.screen.registry.subscreen.certificates

/**
 * Stable `testTag` identifiers for the Certificati sheet ([CertificatesPage]), referenced by both
 * the screen and its UI tests so a user-visible copy change never breaks a test and a tag rename is
 * a compile error rather than a silently missed node. The screen applies them with
 * `Modifier.testTag`; tests anchor on the constants instead of on Italian text.
 *
 * [TILE_PREFIX] is concatenated with a certificate id to address a specific certificate row; tapping
 * one is the page's primary action (download + open).
 */
object CertificatesTestTags {
    const val ROOT = "certificates:root"
    const val STATE_LOADING = "certificates:state:loading"
    const val STATE_ERROR = "certificates:state:error"
    const val STATE_EMPTY = "certificates:state:empty"
    const val STATE_CONTENT = "certificates:state:content"
    const val RESULT_PAGE = "certificates:resultPage"
    const val TILE_PREFIX = "certificates:tile:"

    fun tile(id: String): String = "$TILE_PREFIX$id"
}
