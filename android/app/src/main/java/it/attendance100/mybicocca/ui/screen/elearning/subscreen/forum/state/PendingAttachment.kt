package it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.state

import android.net.Uri

/**
 * A file the user picked in the composer, before it is uploaded on submit. Bytes are read lazily
 * from the content Uri at upload time so large picks don't sit in memory while composing.
 */
data class PendingAttachment(
    val uri: Uri,
    val fileName: String,
    val mimeType: String?,
    val sizeBytes: Long?,
)
