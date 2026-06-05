package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component

import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// Course html resources are mostly small standalone snippets; render them in a WebView
// with JS off. The elearning base url lets same-host relative references resolve, though
// token-protected sub-resources will simply not load — acceptable for these files.
@Composable
fun HtmlViewerContent(localPath: String, modifier: Modifier = Modifier) {
    val html by produceState<String?>(initialValue = null, localPath) {
        value = withContext(Dispatchers.IO) {
            runCatching { File(localPath).readText() }.getOrNull()
        }
    }
    val current = html
    if (current == null) {
        ViewerLoading(modifier = modifier)
        return
    }
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = false
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                "https://elearning.unimib.it/",
                current,
                "text/html",
                "utf-8",
                null,
            )
        },
    )
}
