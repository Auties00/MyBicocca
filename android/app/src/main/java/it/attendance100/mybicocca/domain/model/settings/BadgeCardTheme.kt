package it.attendance100.mybicocca.domain.model.settings

/**
 * The selectable visual finish for the flippable student ID badge on the profile page.
 *
 * App-local preference (not sourced from any university platform), persisted in the shared
 * settings DataStore and picked in the Impostazioni > Aspetto page. The profile badge maps each
 * entry to a palette: [Default] reproduces the physical red Bicocca card, [White] is the pale
 * variant.
 *
 * @property displayName The label shown in the badge picker, which doubles as the finish's product
 * name (so it is not translated).
 */
enum class BadgeCardTheme(val displayName: String) {
    Default("Standard"),
    White("White"),
}
