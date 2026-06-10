package it.attendance100.mybicocca.domain.usecase.settings

import it.attendance100.mybicocca.domain.model.settings.AppTheme
import it.attendance100.mybicocca.domain.repository.AppearanceSettingsRepository
import javax.inject.Inject

/** Persists the color palette picked in the Aspetto page. */
class SetAppThemeUseCase @Inject constructor(
    private val repository: AppearanceSettingsRepository,
) {
    suspend operator fun invoke(theme: AppTheme) = repository.setAppTheme(theme)
}
