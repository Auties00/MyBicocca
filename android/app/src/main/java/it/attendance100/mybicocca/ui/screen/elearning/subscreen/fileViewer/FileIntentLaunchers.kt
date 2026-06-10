package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import it.attendance100.mybicocca.ui.component.file.OfficeApp
import java.io.File
import java.util.Locale

/**
 * Fires Microsoft's documented deep link — `<protocol>:ofv|u|<url>` — which forces view-only
 * mode and lets the Office app download the (tokenized) URL itself. The intent targets the
 * package explicitly so a rogue app registering the scheme can't intercept the token-bearing
 * URL.
 */
internal fun launchOfficeUri(context: Context, uri: String, app: OfficeApp): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        .setPackage(app.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return runCatching { context.startActivity(intent) }.isSuccess
}

internal fun launchPlayStore(context: Context, packageName: String): Boolean {
    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val web = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return runCatching { context.startActivity(market) }
        .recoverCatching { context.startActivity(web) }
        .isSuccess
}

internal fun isPackageInstalled(context: Context, packageName: String): Boolean =
    runCatching { context.packageManager.getPackageInfo(packageName, 0) }.isSuccess

/**
 * Shares a downloaded file through the Android share sheet. The chooser is launched from the
 * Activity context without FLAG_ACTIVITY_NEW_TASK: a new task would play a task-transition
 * animation that shifts the viewer chrome.
 */
internal fun shareFile(context: Context, localPath: String, fileName: String, mimeType: String?): Boolean {
    val file = File(localPath)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val resolvedMime = mimeType
        ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase(Locale.ROOT))
        ?: "*/*"
    val send = Intent(Intent.ACTION_SEND)
        .setType(resolvedMime)
        .putExtra(Intent.EXTRA_STREAM, uri)
        .putExtra(Intent.EXTRA_TITLE, fileName)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    val chooser = Intent.createChooser(send, fileName)
    return runCatching { context.startActivity(chooser) }.isSuccess
}

internal fun launchExternalViewer(context: Context, localPath: String, mimeType: String?): Boolean {
    val file = File(localPath)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val resolvedMime = mimeType
        ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            file.extension.lowercase(Locale.ROOT),
        )
        ?: "*/*"
    val intent = Intent(Intent.ACTION_VIEW)
        .setDataAndType(uri, resolvedMime)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    return try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
