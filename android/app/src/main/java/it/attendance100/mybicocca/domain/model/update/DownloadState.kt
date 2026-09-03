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
