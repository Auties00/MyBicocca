package it.attendance100.mybicocca.ui.screen.segreterie

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import it.attendance100.mybicocca.data.model.document.AppDocument
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import java.io.File

sealed interface SegreterieActionEvent {
    data class OpenDocument(val document: AppDocument) : SegreterieActionEvent
    data class OpenUrl(val url: String) : SegreterieActionEvent
    data class ShowMessage(val message: String) : SegreterieActionEvent
}

@Composable
fun SegreterieActionEventEffect(
    events: Flow<SegreterieActionEvent>,
) {
    val context = LocalContext.current

    LaunchedEffect(events, context) {
        events.collectLatest { event ->
            when (event) {
                is SegreterieActionEvent.OpenDocument -> {
                    context.openDocument(event.document)
                }

                is SegreterieActionEvent.OpenUrl -> {
                    context.openUrl(event.url)
                }

                is SegreterieActionEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

private fun Context.openDocument(document: AppDocument) {
    try {
        val file = writeDocument(document)
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, document.mimeType)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(contentResolver, document.fileName, uri)
        }
        startActivity(Intent.createChooser(intent, document.fileName).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, "Nessuna app disponibile per aprire il documento", Toast.LENGTH_SHORT)
            .show()
    } catch (_: Exception) {
        Toast.makeText(this, "Impossibile aprire il documento", Toast.LENGTH_SHORT).show()
    }
}

private fun Context.openUrl(url: String) {
    if (url.isBlank()) {
        Toast.makeText(this, "Link non disponibile", Toast.LENGTH_SHORT).show()
        return
    }

    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, "Nessuna app disponibile per aprire il link", Toast.LENGTH_SHORT)
            .show()
    }
}

private fun Context.writeDocument(document: AppDocument): File {
    val directory = File(cacheDir, "segreterie-documents").apply { mkdirs() }
    val sanitizedName = document.fileName
        .ifBlank { "documento.pdf" }
        .replace(Regex("[^A-Za-z0-9._-]"), "_")
    return File(directory, sanitizedName).apply {
        writeBytes(document.bytes)
    }
}
