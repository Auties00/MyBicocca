package it.attendance100.mybicocca.ui.screen.registry.subscreen.titles

/**
 * Stable `testTag` identifiers for the Titoli sheet ([TitlesListPage]), referenced by both the
 * screen and its UI tests so a user-visible copy change never breaks a test and a tag rename is a
 * compile error rather than a silently missed node. The screen applies them with `Modifier.testTag`;
 * tests anchor on the constants instead of on Italian text.
 *
 * [ROW_PREFIX] is concatenated with a title id to address a specific qualification row.
 */
object TitlesTestTags {
    const val ROOT = "titles:root"
    const val STATE_LOADING = "titles:state:loading"
    const val STATE_ERROR = "titles:state:error"
    const val STATE_EMPTY = "titles:state:empty"
    const val STATE_CONTENT = "titles:state:content"
    const val ROW_PREFIX = "titles:row:"

    fun row(id: String): String = "$ROW_PREFIX$id"
}
