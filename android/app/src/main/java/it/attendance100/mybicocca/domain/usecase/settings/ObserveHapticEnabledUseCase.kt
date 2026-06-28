package it.attendance100.mybicocca.domain.usecase.settings

import it.attendance100.mybicocca.domain.repository.HapticSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams the haptic-feedback enabled preference; drives the activity-level gate in [HapticManager]. */
class ObserveHapticEnabledUseCase @Inject constructor(
    private val repository: HapticSettingsRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeHapticsEnabled()
}
