package it.attendance100.mybicocca.domain.model.settings

/**
 * The user's light/dark preference for the whole app.
 *
 * App-local preference (not sourced from any university platform), persisted in the shared
 * settings DataStore and edited in the Impostazioni > Aspetto page. [MyBicoccaActivity] resolves
 * it against the system setting to pick the active color scheme: [System] follows the OS,
 * [Light] and [Dark] force the respective scheme regardless of the OS.
 */
enum class ThemeMode { System, Light, Dark }
