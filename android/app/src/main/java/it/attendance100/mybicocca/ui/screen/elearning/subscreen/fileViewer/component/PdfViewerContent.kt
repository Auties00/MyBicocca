package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.pdf.PdfDocument
import androidx.pdf.SandboxedPdfLoader
import androidx.pdf.compose.PdfViewer
import androidx.pdf.compose.PdfViewerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

// androidx.pdf renders in a sandboxed process and brings selection/search/link taps, but
// its backport floor is API 28. Below that (and if the sandboxed loader fails at runtime)
// a plain PdfRenderer page list takes over — no text selection, but the document shows.
@Composable
fun PdfViewerContent(localPath: String, modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= 28) {
        ModernPdfViewer(localPath = localPath, modifier = modifier)
    } else {
        LegacyPdfViewer(localPath = localPath, modifier = modifier)
    }
}

@RequiresApi(28)
@Composable
private fun ModernPdfViewer(localPath: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var document by remember(localPath) { mutableStateOf<PdfDocument?>(null) }
    var loadFailed by remember(localPath) { mutableStateOf(false) }

    LaunchedEffect(localPath) {
        runCatching { SandboxedPdfLoader(context).openDocument(Uri.fromFile(File(localPath))) }
            .onSuccess { document = it }
            .onFailure { loadFailed = true }
    }
    DisposableEffect(document) {
        onDispose { runCatching { (document as? AutoCloseable)?.close() } }
    }

    val current = document
    when {
        current != null -> {
            val state = remember { PdfViewerState() }
            PdfViewer(
                pdfDocument = current,
                state = state,
                modifier = modifier.fillMaxSize(),
            )
        }
        loadFailed -> LegacyPdfViewer(localPath = localPath, modifier = modifier)
        else -> ViewerLoading(modifier = modifier)
    }
}

@Composable
private fun LegacyPdfViewer(localPath: String, modifier: Modifier = Modifier) {
    // PdfRenderer allows a single open page at a time, so all page renders funnel
    // through one mutex.
    val session = remember(localPath) {
        runCatching {
            val descriptor = ParcelFileDescriptor.open(File(localPath), ParcelFileDescriptor.MODE_READ_ONLY)
            LegacyPdfSession(PdfRenderer(descriptor), Mutex())
        }.getOrNull()
    }
    DisposableEffect(session) {
        onDispose { runCatching { session?.renderer?.close() } }
    }
    if (session == null) {
        ViewerError(message = "Impossibile aprire il PDF.", modifier = modifier)
        return
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        // Render at 1.5x layout width so mild platform scaling doesn't blur the text.
        val targetWidthPx = (constraints.maxWidth * 1.5f).toInt().coerceAtMost(2048)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(count = session.renderer.pageCount, key = { it }) { index ->
                LegacyPdfPage(session = session, pageIndex = index, targetWidthPx = targetWidthPx)
            }
        }
    }
}

private class LegacyPdfSession(
    val renderer: PdfRenderer,
    val renderLock: Mutex,
)

@Composable
private fun LegacyPdfPage(session: LegacyPdfSession, pageIndex: Int, targetWidthPx: Int) {
    val bitmap by produceState<Bitmap?>(initialValue = null, session, pageIndex, targetWidthPx) {
        value = withContext(Dispatchers.IO) {
            session.renderLock.withLock {
                runCatching {
                    session.renderer.openPage(pageIndex).use { page ->
                        val height = (targetWidthPx.toFloat() / page.width * page.height).toInt()
                        Bitmap.createBitmap(targetWidthPx, height, Bitmap.Config.ARGB_8888).also {
                            // Pdf pages are transparent by default; render over white.
                            it.eraseColor(android.graphics.Color.WHITE)
                            page.render(it, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        }
                    }
                }.getOrNull()
            }
        }
    }
    val current = bitmap
    if (current != null) {
        Image(
            bitmap = current.asImageBitmap(),
            contentDescription = "Pagina ${pageIndex + 1}",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
        )
    } else {
        // Hold an A4-ish slot while the page renders so the scrollbar doesn't jump.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .aspectRatio(1f / 1.414f)
                .background(Color.White),
        )
    }
}
