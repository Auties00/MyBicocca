package it.attendance100.mybicocca.domain.usecase.settings

import it.attendance100.mybicocca.domain.model.settings.BadgeCardTheme
import it.attendance100.mybicocca.domain.repository.AppearanceSettingsRepository
import javax.inject.Inject

/** Persists the student-badge finish picked in the Aspetto page. */
class SetBadgeCardThemeUseCase @Inject constructor(
    private val repository: AppearanceSettingsRepository,
) {
    suspend operator fun invoke(theme: BadgeCardTheme) = repository.setBadgeCardTheme(theme)
}
