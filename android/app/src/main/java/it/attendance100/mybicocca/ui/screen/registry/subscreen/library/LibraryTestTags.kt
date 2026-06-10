package it.attendance100.mybicocca.ui.screen.registry.subscreen.library

/**
 * Stable `testTag` identifiers for [LibraryPage] and its in-sheet pages, referenced by both the
 * screen and its UI tests so a user-visible copy change never breaks a test and a tag rename is a
 * compile error rather than a silently missed node. The screen applies them with `Modifier.testTag`;
 * tests anchor on the constants instead of on Italian text.
 */
object LibraryTestTags {
    const val ROOT = "library:root"

    const val HOME_PAGE = "library:page:home"
    const val HOME_VERIFY_PROMPT = "library:home:verifyPrompt"
    const val HOME_SYNCING = "library:home:syncing"
    const val HOME_EMPTY = "library:home:empty"
    const val HOME_LIST = "library:home:list"
    const val HOME_FOOTER = "library:home:footer"

    const val LIBRARIES_LOADING = "library:libraries:loading"
    const val LIBRARIES_ERROR = "library:libraries:error"
    const val LIBRARIES_EMPTY = "library:libraries:empty"

    /** Per-library card in the bookable libraries list, keyed by the library id. */
    fun library(id: String): String = "library:card:$id"
}
