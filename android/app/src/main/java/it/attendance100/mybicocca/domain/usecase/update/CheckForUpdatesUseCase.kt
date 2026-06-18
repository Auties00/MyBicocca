package it.attendance100.mybicocca.domain.usecase.update

import it.attendance100.mybicocca.domain.model.update.UpdateCheckResult
import it.attendance100.mybicocca.domain.repository.UpdateRepository
import javax.inject.Inject

/**
 * Runs an update check and returns its outcome. Triggered by the "Check for Updates" tile in the
 * About modal with [force] = true to bypass the daily freshness window, and by the foreground
 * background checker with false so re-foregrounding within the day is a no-op. Never throws — a
 * failed check comes back as [UpdateCheckResult.Failed].
 */
class CheckForUpdatesUseCase @Inject constructor(
    private val repository: UpdateRepository,
) {
    suspend operator fun invoke(force: Boolean): UpdateCheckResult =
        repository.checkForUpdates(force)
}
