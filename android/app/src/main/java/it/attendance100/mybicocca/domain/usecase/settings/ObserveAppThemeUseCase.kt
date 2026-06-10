package it.attendance100.mybicocca.domain.usecase.settings

import it.attendance100.mybicocca.domain.model.settings.AppTheme
import it.attendance100.mybicocca.domain.repository.AppearanceSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams the selected color palette; drives both the activity-level theming and the Aspetto page. */
class ObserveAppThemeUseCase @Inject constructor(
    private val repository: AppearanceSettingsRepository,
) {
    operator fun invoke(): Flow<AppTheme> = repository.observeAppTheme()
}
