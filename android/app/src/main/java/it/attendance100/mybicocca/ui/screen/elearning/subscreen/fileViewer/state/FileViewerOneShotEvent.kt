package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.state

import it.attendance100.mybicocca.ui.component.file.OfficeApp

/**
 * One-shot effects emitted by the file-viewer ViewModel for the hosting UI to execute: intent
 * launches, navigation into a nested viewer, and result feedback. Delivered through a Channel so
 * each fires exactly once and never replays across configuration changes.
 */
sealed interface FileViewerOneShotEvent {
    data class DownloadFailed(val cause: Throwable) : FileViewerOneShotEvent

    /** Deep link into the matching Office app: `<protocol>:ofv|u|<tokenized url>`. */
    data class LaunchOfficeUri(val uri: String, val app: OfficeApp) : FileViewerOneShotEvent

    /** Hand the downloaded file to an external app via FileProvider + ACTION_VIEW. */
    data class OpenWithExternalApp(val localPath: String, val mimeType: String?) : FileViewerOneShotEvent

    /** Share the downloaded file via the Android share sheet (FileProvider + ACTION_SEND). */
    data class ShareFile(val localPath: String, val fileName: String, val mimeType: String?) : FileViewerOneShotEvent

    /**
     * A zip entry was extracted; navigate to a nested viewer for it. [forceChooser] is set by a
     * long-press on the entry, to re-show the in-app/external chooser even when remembered.
     */
    data class OpenExtractedFile(
        val fileName: String,
        val localPath: String,
        val mimeType: String?,
        val sizeBytes: Long?,
        val forceChooser: Boolean = false,
    ) : FileViewerOneShotEvent

    /** The file was written to Downloads or the gallery. */
    data object FileSaved : FileViewerOneShotEvent
}
