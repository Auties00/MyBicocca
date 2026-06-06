package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import java.io.Closeable
import java.io.File

// In-app PDF viewer built on the platform PdfRenderer (no third-party dependency): one page
// per screen in a vertical pager so each page fits the full screen height and a pinch-zoom
// uses the whole screen. Pages render to bitmaps on demand (serialized — PdfRenderer allows
// one open page at a time). At minimum zoom, vertical swipes flip pages; zoomed in they pan.
@Composable
fun PdfViewerContent(localPath: String, modifier: Modifier = Modifier) {
    val document = remember(localPath) { runCatching { PdfDocument(localPath) }.getOrNull() }
    DisposableEffect(document) {
        onDispose { document?.close() }
    }

    if (document == null || document.pageCount == 0) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Impossibile aprire il PDF.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val density = LocalDensity.current
    val pagerState = rememberPagerState(pageCount = { document.pageCount })
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        // Render around the screen width for crispness; the page is then fit to the viewport
        // height, so zooming in has the full screen to expand into.
        val widthPx = with(density) { (maxWidth.toPx() * 1.5f).toInt() }.coerceIn(1, 2400)
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = 8.dp,
        ) { index ->
            PdfPage(document = document, index = index, widthPx = widthPx)
        }
        if (document.pageCount > 1) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.92f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${document.pageCount}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun PdfPage(document: PdfDocument, index: Int, widthPx: Int) {
    var page by remember(index, widthPx) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(index, widthPx) {
        page = runCatching { document.renderPage(index, widthPx).asImageBitmap() }.getOrNull()
    }
    val bitmap = page
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            // telephoto's zoomable gives pinch + double-tap zoom and pan; at min zoom it forwards
            // vertical swipes to the pager so paging still works.
            val zoomState = rememberZoomableState()
            Image(
                bitmap = bitmap,
                contentDescription = "Pagina ${index + 1}",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .zoomable(zoomState),
            )
        } else {
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
        }
    }
}

private class PdfDocument(path: String) : Closeable {
    private val descriptor = ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
    private val renderer = PdfRenderer(descriptor)
    private val mutex = Mutex()

    val pageCount: Int = renderer.pageCount

    suspend fun renderPage(index: Int, widthPx: Int): Bitmap = withContext(Dispatchers.IO) {
        mutex.withLock {
            renderer.openPage(index).use { page ->
                val heightPx = (widthPx * page.height.toFloat() / page.width).toInt().coerceAtLeast(1)
                val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap
            }
        }
    }

    override fun close() {
        runCatching { renderer.close() }
        runCatching { descriptor.close() }
    }
}
