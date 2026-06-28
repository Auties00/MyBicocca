package it.attendance100.mybicocca.domain.usecase.settings

import it.attendance100.mybicocca.domain.repository.HapticSettingsRepository
import javax.inject.Inject

/** Persists the haptic-feedback enabled preference picked on the Impostazioni > Vibrazione page. */
class SetHapticEnabledUseCase @Inject constructor(
    private val repository: HapticSettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setHapticsEnabled(enabled)
}
