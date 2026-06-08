package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component

import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HtmlViewerContent(
    localPath: String,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    onOpenInBrowser: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
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
        ViewerBottomBar(
            primary = ViewerAction(
                icon = Icons.Outlined.Download,
                label = "Scarica",
                onClick = onDownload,
            ),
            ViewerAction(
                icon = Icons.Outlined.Share,
                label = "Condividi",
                onClick = onShare,
            ),
            ViewerAction(
                icon = Icons.AutoMirrored.Outlined.OpenInNew,
                label = "Apri nel browser",
                onClick = onOpenInBrowser,
            ),
        )
    }
}
