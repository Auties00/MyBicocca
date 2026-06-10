package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.settings.AppTheme
import it.attendance100.mybicocca.domain.model.settings.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Persisted appearance preferences (light/dark behavior and color palette).
 *
 * The observe methods are hot flows over the settings DataStore: they emit the current value
 * immediately and again on every change, so both the activity-level theming and the
 * Impostazioni > Aspetto page stay live. The setters suspend until the value is persisted.
 */
interface AppearanceSettingsRepository {
    fun observeThemeMode(): Flow<ThemeMode>
    fun observeAppTheme(): Flow<AppTheme>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setAppTheme(theme: AppTheme)
}
