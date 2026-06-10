package it.attendance100.mybicocca.domain.model.settings

/**
 * The selectable color palette for the whole app.
 *
 * App-local preference persisted in the shared settings DataStore and picked in the
 * Impostazioni > Aspetto page. The UI layer maps each entry to a Material color scheme:
 * [Default] is the MyBicocca brand red, [MaterialYou] derives dynamic colors from the OS
 * wallpaper palette (Android 12+, falling back to [Default] on older devices), [Oceano] is a
 * blue palette and [Bosco] a green one.
 *
 * @property displayName The label shown in the theme picker, which doubles as the palette's
 * product name (so it is not translated).
 */
enum class AppTheme(val displayName: String) {
    Default("MyBicocca"),
    MaterialYou("Material You"),
    Oceano("Oceano"),
    Bosco("Bosco"),
}
