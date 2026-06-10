package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.local.settings.AppearanceSettingsStore
import it.attendance100.mybicocca.domain.model.settings.AppTheme
import it.attendance100.mybicocca.domain.model.settings.ThemeMode
import it.attendance100.mybicocca.domain.repository.AppearanceSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Delegates to [AppearanceSettingsStore], the DataStore wrapper owning the persisted keys. */
@Singleton
class AppearanceSettingsRepositoryImpl @Inject constructor(
    private val store: AppearanceSettingsStore,
) : AppearanceSettingsRepository {

    override fun observeThemeMode(): Flow<ThemeMode> = store.themeMode

    override fun observeAppTheme(): Flow<AppTheme> = store.appTheme

    override suspend fun setThemeMode(mode: ThemeMode) = store.setThemeMode(mode)

    override suspend fun setAppTheme(theme: AppTheme) = store.setAppTheme(theme)
}
