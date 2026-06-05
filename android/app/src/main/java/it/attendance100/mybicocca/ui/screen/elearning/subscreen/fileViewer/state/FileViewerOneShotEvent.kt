package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.state

sealed interface FileViewerOneShotEvent {
    data class DownloadFailed(val cause: Throwable) : FileViewerOneShotEvent

    // Deep link into the matching Office app: "<protocol>:ofv|u|<tokenized url>".
    data class LaunchOfficeUri(val uri: String, val app: OfficeApp) : FileViewerOneShotEvent

    // Hand the downloaded file to an external app via FileProvider + ACTION_VIEW.
    data class OpenWithExternalApp(val localPath: String, val mimeType: String?) : FileViewerOneShotEvent

    // A zip entry was extracted; navigate to a nested viewer for it.
    data class OpenExtractedFile(
        val fileName: String,
        val localPath: String,
        val mimeType: String?,
        val sizeBytes: Long?,
    ) : FileViewerOneShotEvent
}
