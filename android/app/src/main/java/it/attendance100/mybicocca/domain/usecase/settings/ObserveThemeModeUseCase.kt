package it.attendance100.mybicocca.domain.usecase.settings

import it.attendance100.mybicocca.domain.model.settings.ThemeMode
import it.attendance100.mybicocca.domain.repository.AppearanceSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams the light/dark preference; drives both the activity-level theming and the Aspetto page. */
class ObserveThemeModeUseCase @Inject constructor(
    private val repository: AppearanceSettingsRepository,
) {
    operator fun invoke(): Flow<ThemeMode> = repository.observeThemeMode()
}
