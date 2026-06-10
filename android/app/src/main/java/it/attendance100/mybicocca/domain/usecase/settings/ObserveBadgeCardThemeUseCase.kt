package it.attendance100.mybicocca.domain.usecase.settings

import it.attendance100.mybicocca.domain.model.settings.BadgeCardTheme
import it.attendance100.mybicocca.domain.repository.AppearanceSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams the chosen student-badge finish; drives the profile badge and its picker in the Aspetto page. */
class ObserveBadgeCardThemeUseCase @Inject constructor(
    private val repository: AppearanceSettingsRepository,
) {
    operator fun invoke(): Flow<BadgeCardTheme> = repository.observeBadgeCardTheme()
}
