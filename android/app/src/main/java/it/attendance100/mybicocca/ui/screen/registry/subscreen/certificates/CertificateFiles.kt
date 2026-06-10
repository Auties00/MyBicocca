package it.attendance100.mybicocca.ui.screen.registry.subscreen.certificates

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import it.attendance100.mybicocca.domain.model.document.Certificate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

private const val CERTIFICATES_DIR = "certificati"

/**
 * Predictable per-certificate cache path, so an already-downloaded PDF opens straight
 * from disk without a re-fetch. Same scheme as the elearning file cache: a SHA-256
 * prefix of the opaque CertificateId (an Esse3 request path, unsafe as a file name) keys
 * a folder under cacheDir, holding the human-named PDF.
 */
internal fun certificateFile(context: Context, certificate: Certificate): File {
    val dir = File(context.cacheDir, "$CERTIFICATES_DIR/${key(certificate)}")
    return File(dir, certificateFileName(certificate))
}

internal fun isCertificateDownloaded(context: Context, certificate: Certificate): Boolean {
    val file = certificateFile(context, certificate)
    return file.exists() && file.length() > 0
}

/**
 * Writes the PDF to its predictable path, staging through a .part rename so a crash
 * mid-write never leaves a truncated file that reads as a cache hit.
 */
internal suspend fun writeCertificate(context: Context, certificate: Certificate, bytes: ByteArray): File =
    withContext(Dispatchers.IO) {
        val file = certificateFile(context, certificate)
        file.parentFile?.mkdirs()
        val temp = File(file.parentFile, "${file.name}.part")
        temp.writeBytes(bytes)
        if (!temp.renameTo(file)) {
            temp.delete()
            error("Impossibile salvare il certificato.")
        }
        file
    }

/**
 * Hands a cached certificate PDF to an external viewer via FileProvider ACTION_VIEW;
 * false when no installed app can handle it.
 */
internal fun openCertificate(context: Context, file: File): Boolean {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_VIEW)
        .setDataAndType(uri, "application/pdf")
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    return runCatching { context.startActivity(intent) }.isSuccess
}

private fun key(certificate: Certificate): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(certificate.id.value.toByteArray())
    return digest.joinToString("") { "%02x".format(it) }.take(16)
}

/** Sanitizes the description into a stable .pdf file name for the cache and the viewer title. */
private fun certificateFileName(certificate: Certificate): String {
    val base = certificate.description.ifBlank { "certificato" }
        .replace(Regex("[^A-Za-z0-9 _-]"), "")
        .trim()
        .replace(Regex("\\s+"), "_")
        .take(80)
        .ifBlank { "certificato" }
    val suffix = certificate.solarYear?.let { "_$it" }.orEmpty()
    return "$base$suffix.pdf"
}
