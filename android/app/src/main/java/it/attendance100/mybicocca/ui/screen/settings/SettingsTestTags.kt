package it.attendance100.mybicocca.ui.screen.settings

/**
 * Stable `testTag` identifiers for [SettingsScreen] and its directory tiles, referenced by both
 * the screen and its UI tests so a user-visible copy change never breaks a test and a tag rename
 * is a compile error rather than a silently missed node. The screen applies [ROOT]; each directory
 * row is tagged with [entry] keyed by the [it.attendance100.mybicocca.ui.screen.settings.state.SettingsEntry.id]
 * so a test can locate a specific tile (Aspetto, Lingua, Sicurezza, …) by id rather than by Italian text.
 */
object SettingsTestTags {
    const val ROOT = "settings:root"

    /** Tag of a single directory row, keyed by the entry id (e.g. `appearance`, `language`, `about`). */
    fun entry(id: String): String = "settings:entry:$id"
}
