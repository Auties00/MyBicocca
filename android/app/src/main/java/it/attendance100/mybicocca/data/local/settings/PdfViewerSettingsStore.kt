package it.attendance100.mybicocca.data.local.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import it.attendance100.mybicocca.domain.model.settings.PdfPagerOrientation
import it.attendance100.mybicocca.domain.model.settings.PdfThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the PDF viewer preferences (rendering theme and scroll axis) in the shared
 * `mybicocca_settings` DataStore, both as stable lowercase strings.
 */
@Singleton
class PdfViewerSettingsStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val themeMode: Flow<PdfThemeMode> = dataStore.data.map { prefs ->
        when (prefs[THEME_MODE_KEY]) {
            LIGHT -> PdfThemeMode.Light
            DARK -> PdfThemeMode.Dark
            else -> PdfThemeMode.System
        }
    }

    val orientation: Flow<PdfPagerOrientation> = dataStore.data.map { prefs ->
        when (prefs[ORIENTATION_KEY]) {
            HORIZONTAL -> PdfPagerOrientation.Horizontal
            else -> PdfPagerOrientation.Vertical
        }
    }

    suspend fun setThemeMode(mode: PdfThemeMode) {
        dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = when (mode) {
                PdfThemeMode.System -> SYSTEM
                PdfThemeMode.Light -> LIGHT
                PdfThemeMode.Dark -> DARK
            }
        }
    }

    suspend fun setOrientation(orientation: PdfPagerOrientation) {
        dataStore.edit { prefs ->
            prefs[ORIENTATION_KEY] = when (orientation) {
                PdfPagerOrientation.Vertical -> VERTICAL
                PdfPagerOrientation.Horizontal -> HORIZONTAL
            }
        }
    }

    private companion object {
        val THEME_MODE_KEY = stringPreferencesKey("pdf_viewer_theme_mode")
        val ORIENTATION_KEY = stringPreferencesKey("pdf_viewer_orientation")
        const val SYSTEM = "system"
        const val LIGHT = "light"
        const val DARK = "dark"
        const val VERTICAL = "vertical"
        const val HORIZONTAL = "horizontal"
    }
}
