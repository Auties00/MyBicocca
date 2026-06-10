package it.attendance100.mybicocca.data.local.file

import android.content.Context
import android.provider.OpenableColumns
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionFile
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionFileMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves picked `content://` uris into display metadata and, on demand, their bytes. Keeps the
 * ContentResolver out of ViewModels and off the main thread; the UI reaches it through the
 * assignment repository's probe/read methods.
 */
@Singleton
class ContentUriReader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** Cheap provider query — display name, MIME type and size — with no content read. */
    suspend fun probe(uri: String): SubmissionFileMetadata = withContext(Dispatchers.IO) {
        val parsed = uri.toUri()
        val resolver = context.contentResolver
        var name: String? = null
        var size: Long? = null
        resolver.query(parsed, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIdx >= 0 && !cursor.isNull(nameIdx)) name = cursor.getString(nameIdx)
                if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) size = cursor.getLong(sizeIdx)
            }
        }
        SubmissionFileMetadata(
            fileName = name ?: parsed.lastPathSegment?.substringAfterLast('/') ?: "file",
            mimeType = resolver.getType(parsed),
            sizeBytes = size,
        )
    }

    /** Reads the full content into memory for upload. */
    suspend fun read(uri: String): SubmissionFile = withContext(Dispatchers.IO) {
        val meta = probe(uri)
        val bytes = context.contentResolver.openInputStream(uri.toUri())?.use { it.readBytes() }
            ?: error("Cannot open $uri")
        SubmissionFile(
            fileName = meta.fileName,
            mimeType = meta.mimeType,
            sizeBytes = meta.sizeBytes ?: bytes.size.toLong(),
            bytes = bytes,
        )
    }
}
