package it.attendance100.mybicocca.domain.usecase.settings

import it.attendance100.mybicocca.domain.model.settings.ThemeMode
import it.attendance100.mybicocca.domain.repository.AppearanceSettingsRepository
import javax.inject.Inject

/** Persists the light/dark preference picked in the Aspetto page. */
class SetThemeModeUseCase @Inject constructor(
    private val repository: AppearanceSettingsRepository,
) {
    suspend operator fun invoke(mode: ThemeMode) = repository.setThemeMode(mode)
}
