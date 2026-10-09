package it.attendance100.mybicocca.domain.model.update

import it.attendance100.mybicocca.core.text.UiText
import java.io.File

/**
 * Where the update download has got to.
 *
 * A domain model rather than a detail of the downloader, because the screens that render it and
 * the workers that drive it sit on opposite sides of the app and neither should have to reach
 * into the other's layer to name it.
 */
sealed interface DownloadState {
    data object Idle : DownloadState

    /**
     * Asked for, not started yet. Downloads run inside a foreground service that WorkManager
     * starts, so there is a gap between the tap and the first byte — and if the request is waiting
     * on a connection, that gap is open-ended. Without a state of its own the UI would sit on
     * [Idle] showing an untouched Download button, or lie with a 0% bar that can never move.
     */
    data object Enqueued : DownloadState

    data class Downloading(val progress: Int) : DownloadState
    data class Success(val file: File) : DownloadState

    /** The system installer was dismissed without installing. [file] is still downloaded and valid. */
    data class InstallDeclined(val file: File) : DownloadState
    data class Error(val message: UiText) : DownloadState
}

/**
 * The APK sitting on disk waiting to be installed, or null. A declined install counts: the file is
 * still downloaded and verified, the user just dismissed the system dialog.
 */
val DownloadState.readyToInstall: File?
    get() = when (this) {
        is DownloadState.Success -> file
        is DownloadState.InstallDeclined -> file
        else -> null
    }

fun DownloadState.isReadyToInstall(): Boolean = readyToInstall != null

/** Asked for and not yet finished, queued included — the window in which a new request is ignored. */
val DownloadState.isActive: Boolean
    get() = this is DownloadState.Enqueued || this is DownloadState.Downloading
