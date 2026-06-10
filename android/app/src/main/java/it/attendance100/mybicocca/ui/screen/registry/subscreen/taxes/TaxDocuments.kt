package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Opens an external URL (e.g. the pagoPA payment portal redirect) in the device's default
 * handler, the Intent.ACTION_VIEW idiom used across the app.
 */
fun openExternalUrl(context: Context, url: String) {
    context.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}

/**
 * Writes the PDF bytes to the app cache and hands them to an external viewer via the app
 * FileProvider. The write happens off the main thread; the launch is on it.
 */
suspend fun openPdfDocument(context: Context, bytes: ByteArray, fileName: String) {
    val file = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "taxes").apply { mkdirs() }
        File(dir, fileName).apply { writeBytes(bytes) }
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
