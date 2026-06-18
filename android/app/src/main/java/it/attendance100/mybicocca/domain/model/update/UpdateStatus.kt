package it.attendance100.mybicocca.domain.model.update

/**
 * The durable result of the last update check, persisted and observed so the Settings "Check
 * for Updates" tile reflects an available update across app restarts and regardless of which
 * trigger (daily background check or the manual button) produced it.
 */
sealed interface UpdateStatus {
    /** No check has completed yet (fresh install, or the first check is still in flight). */
    data object Unknown : UpdateStatus

    /** The latest release is not newer than the installed build. */
    data object UpToDate : UpdateStatus

    /** A newer release exists; [release] carries the version and the page to open. */
    data class UpdateAvailable(val release: AppRelease) : UpdateStatus
}

/**
 * The one-shot outcome of an explicit [it.attendance100.mybicocca.domain.usecase.update.CheckForUpdatesUseCase]
 * call, returned to the manual "Check for Updates" action so it can show the right snackbar.
 * Distinct from [UpdateStatus] because the manual flow needs to tell a failed check apart from a
 * successful "up to date" one, while the persisted status only cares about the latter.
 */
sealed interface UpdateCheckResult {
    data object UpToDate : UpdateCheckResult
    data class UpdateAvailable(val release: AppRelease) : UpdateCheckResult
    data class Failed(val cause: Throwable) : UpdateCheckResult
}
