package it.attendance100.mybicocca.domain.usecase.update

import it.attendance100.mybicocca.domain.model.update.UpdateCheckResult
import it.attendance100.mybicocca.domain.repository.UpdateRepository
import javax.inject.Inject

/**
 * Runs the manual "Check for Updates" tile's check. Never announces app-wide — the tile shows
 * this result directly. Never throws; a failed check comes back as [UpdateCheckResult.Failed].
 */
class CheckForUpdatesUseCase @Inject constructor(
    private val repository: UpdateRepository,
) {
    suspend operator fun invoke(force: Boolean): UpdateCheckResult =
        repository.checkForUpdates(force, announce = false)
}
