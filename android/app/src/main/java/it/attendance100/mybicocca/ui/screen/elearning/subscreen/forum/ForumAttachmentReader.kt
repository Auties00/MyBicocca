package it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encapsulates ContentResolver reads for forum attachments so that [ForumSheetViewModel]
 * does not leak an Android [Context].
 */
@Singleton
class ForumAttachmentReader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun readBytes(uri: Uri): ByteArray? {
        return context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
    }
}
