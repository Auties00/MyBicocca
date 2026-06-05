package it.attendance100.mybicocca.data.local.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import it.attendance100.mybicocca.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode { System, Light, Dark }

/**
 * Persists the appearance preferences (theme mode). Backed by the shared `mybicocca_settings` DataStore, mirroring [SecuritySettingsStore].
 */
@Singleton
class AppearanceSettingsStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        when (prefs[THEME_MODE_KEY]) {
            LIGHT -> ThemeMode.Light
            DARK -> ThemeMode.Dark
            else -> ThemeMode.System
        }
    }

    val appTheme: Flow<AppTheme> = dataStore.data.map { prefs ->
        val stored = prefs[APP_THEME_KEY]
        AppTheme.entries.firstOrNull { it.name == stored } ?: AppTheme.Default
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = when (mode) {
                ThemeMode.System -> SYSTEM
                ThemeMode.Light -> LIGHT
                ThemeMode.Dark -> DARK
            }
        }
    }

    suspend fun setAppTheme(theme: AppTheme) {
        dataStore.edit { prefs -> prefs[APP_THEME_KEY] = theme.name }
    }

    private companion object {
        val THEME_MODE_KEY = stringPreferencesKey("appearance_theme_mode")
        val APP_THEME_KEY = stringPreferencesKey("appearance_app_theme")
        const val SYSTEM = "system"
        const val LIGHT = "light"
        const val DARK = "dark"
    }
}
