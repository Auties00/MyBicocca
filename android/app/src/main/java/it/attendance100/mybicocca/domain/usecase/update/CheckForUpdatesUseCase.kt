package it.attendance100.mybicocca.domain.usecase.update

import it.attendance100.mybicocca.domain.model.update.UpdateCheckResult
import it.attendance100.mybicocca.domain.repository.UpdateRepository
import javax.inject.Inject

/**
 * Runs a manual, user-driven update check and returns its outcome — the "Check for Updates" tile
 * in the About modal is this use case's only caller, with [force] = true to bypass the daily
 * freshness window. Always announce = false: the tile shows this result directly, so the app-wide
 * snackbar would just repeat it. The two triggers that *do* want the app-wide snackbar —
 * [it.attendance100.mybicocca.data.update.UpdateChecker]'s foreground check and the periodic
 * background worker — call [UpdateRepository.checkForUpdates] directly instead of going through
 * this use case. Never throws — a failed check comes back as [UpdateCheckResult.Failed].
 */
class CheckForUpdatesUseCase @Inject constructor(
    private val repository: UpdateRepository,
) {
    suspend operator fun invoke(force: Boolean): UpdateCheckResult =
        repository.checkForUpdates(force, announce = false)
}
