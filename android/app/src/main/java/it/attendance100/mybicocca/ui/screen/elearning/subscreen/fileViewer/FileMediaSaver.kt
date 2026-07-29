package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Storage and MediaStore helper for saving files to Downloads / Gallery without leaking Context into [FileViewerViewModel].
 */
@Suppress("RedundantSuspendModifier")
@Singleton
class FileMediaSaver @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun saveToDownloads(source: File, fileName: String, mimeType: String?): File {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToDownloadsQ(source, fileName, mimeType)
        } else {
            saveToDownloadsLegacy(source, fileName)
        }
    }

    suspend fun saveToGallery(source: File, fileName: String, mimeType: String?): File {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToGalleryQ(source, fileName, mimeType)
        } else {
            saveToGalleryLegacy(source, fileName)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveToDownloadsQ(source: File, fileName: String, mimeType: String?): File {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            mimeType?.let { put(MediaStore.Downloads.MIME_TYPE, it) }
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: error("Impossibile creare il file in Download")
        resolver.openOutputStream(uri)?.use { out ->
            source.inputStream().use { input -> input.copyTo(out) }
        }
        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
    }

    private fun saveToDownloadsLegacy(source: File, fileName: String): File {
        val downloads =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        downloads.mkdirs()
        val target = File(downloads, fileName)
        source.copyTo(target, overwrite = true)
        MediaScannerConnection.scanFile(context, arrayOf(target.absolutePath), null, null)
        return target
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveToGalleryQ(source: File, fileName: String, mimeType: String?): File {
        val isVideo =
            mimeType?.startsWith("video/") == true || fileName.endsWith(".mp4", ignoreCase = true)
        val collection = if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val relativeDir =
            if (isVideo) Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_PICTURES

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            mimeType?.let { put(MediaStore.MediaColumns.MIME_TYPE, it) }
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativeDir)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(collection, values) ?: error("Impossibile salvare nei media")
        resolver.openOutputStream(uri)?.use { out ->
            source.inputStream().use { input -> input.copyTo(out) }
        }
        values.clear()
        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return File(Environment.getExternalStoragePublicDirectory(relativeDir), fileName)
    }

    private fun saveToGalleryLegacy(source: File, fileName: String): File {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        dir.mkdirs()
        val target = File(dir, fileName)
        source.copyTo(target, overwrite = true)
        MediaScannerConnection.scanFile(context, arrayOf(target.absolutePath), null, null)
        return target
    }
}
