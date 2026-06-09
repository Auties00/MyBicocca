package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component

import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

enum class HtmlThemeMode(val label: String) {
    System("Sistema"),
    Light("Chiaro"),
    Dark("Scuro");

    fun next(): HtmlThemeMode = entries[(ordinal + 1) % entries.size]
}

private fun injectThemeCss(html: String, mode: HtmlThemeMode): String {
    if (mode == HtmlThemeMode.Light) return html

    val cssRules = """
        body { background-color: #121212 !important; color: #e0e0e0 !important; }
        h1, h2, h3, h4, h5, h6, p, span, div, li, td, th { color: #e0e0e0 !important; background-color: transparent !important; }
        a, a span { color: #bb86fc !important; }
        pre, code, blockquote { background-color: #2d2d2d !important; color: #e0e0e0 !important; border-color: #444 !important; }
        table, th, td { border-color: #555 !important; }
        img, video, iframe { opacity: 0.85 !important; }
    """.trimIndent()

    val styleTag = when (mode) {
        HtmlThemeMode.Dark -> "<style>\n$cssRules\n</style>"
        HtmlThemeMode.System -> "<style>\n@media (prefers-color-scheme: dark) {\n$cssRules\n}\n</style>"
    }

    val headRegex = Regex("<head(\\s[^>]*)?>", RegexOption.IGNORE_CASE)
    val match = headRegex.find(html)
    return if (match != null) {
        val insertAt = match.range.last + 1
        html.substring(0, insertAt) + "\n$styleTag" + html.substring(insertAt)
    } else {
        "$styleTag\n$html"
    }
}

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
    var themeMode by remember { mutableStateOf(HtmlThemeMode.System) }

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

    // Reactively compute the final HTML string whenever the theme or file content changes
    val finalHtml = remember(current, themeMode) {
        injectThemeCss(current, themeMode)
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
                    finalHtml,
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
            ViewerAction(
                icon = when (themeMode) {
                    HtmlThemeMode.System -> Icons.Outlined.SettingsBrightness
                    HtmlThemeMode.Light -> Icons.Outlined.LightMode
                    HtmlThemeMode.Dark -> Icons.Outlined.DarkMode
                },
                label = "Tema: ${themeMode.label}",
                onClick = { themeMode = themeMode.next() },
            )
        )
    }
}
