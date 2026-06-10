package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.WrapText
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.BoldHighlight
import dev.snipme.highlights.model.ColorHighlight
import dev.snipme.highlights.model.SyntaxLanguage
import dev.snipme.highlights.model.SyntaxTheme
import dev.snipme.highlights.model.SyntaxThemes
import it.attendance100.mybicocca.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * Read-only code/text viewer themed with the app's palette: the page surface, JetBrains Mono, a
 * line-number gutter, and syntax colours mapped onto the Material scheme (keyword = primary,
 * string = tertiary, …). One Text per line inside a LazyColumn keeps long sources virtualized,
 * and a trailing spacer keeps the last line clear of the action bar.
 *
 * Single-finger drags scroll — vertically through lines, horizontally through long lines — while
 * a two-finger pinch scales the font without stealing those drags. Text is selectable. The
 * bottom action bar offers download, share, and a word-wrap toggle (wrap replaces the horizontal
 * scroll). A loading state shows while the file is read and highlighted off the main thread.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CodeViewerContent(
    localPath: String,
    fileName: String,
    darkTheme: Boolean,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val syntaxTheme = remember(scheme) { appSyntaxTheme(scheme) }

    val lines by produceState<List<AnnotatedString>?>(initialValue = null, localPath, syntaxTheme) {
        value = withContext(Dispatchers.Default) {
            runCatching { highlightFile(localPath, fileName, syntaxTheme) }.getOrNull()
        }
    }

    val current = lines
    if (current == null) {
        ViewerLoading(modifier = modifier)
        return
    }

    val mono = remember { FontFamily(Font(R.font.jetbrains_mono_regular)) }
    val hScroll = rememberScrollState()
    var fontScale by remember { mutableFloatStateOf(1f) }
    val fontSize = (13 * fontScale).sp
    val lineHeight = (18 * fontScale).sp
    val gutterWidth = (12 + 9 * current.size.toString().length).dp
    val codeColor = scheme.onSurface
    val gutterColor = scheme.onSurfaceVariant.copy(alpha = 0.6f)

    var wordWrap by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        SelectionContainer {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scheme.surface)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            do {
                                val event = awaitPointerEvent()
                                if (event.changes.count { it.pressed } >= 2) {
                                    val zoom = event.calculateZoom()
                                    if (zoom != 1f) {
                                        fontScale = (fontScale * zoom).coerceIn(0.6f, 3f)
                                        event.changes.forEach { it.consume() }
                                    }
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    },
            ) {
                itemsIndexed(current) { index, line ->
                    Row {
                        Text(
                            text = "${index + 1}",
                            fontFamily = mono,
                            fontSize = fontSize,
                            lineHeight = lineHeight,
                            color = gutterColor,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .width(gutterWidth)
                                .padding(end = 8.dp),
                        )
                        Text(
                            text = line,
                            fontFamily = mono,
                            fontSize = fontSize,
                            lineHeight = lineHeight,
                            color = codeColor,
                            softWrap = wordWrap,
                            modifier = if (wordWrap) Modifier.padding(end = 16.dp)
                            else Modifier
                                .horizontalScroll(hScroll)
                                .padding(end = 16.dp),
                        )
                    }
                }
                item {
                    Box(modifier = Modifier.padding(bottom = 88.dp))
                }
            }
        }
        ViewerBottomBar(
            ViewerAction(
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
                icon = Icons.AutoMirrored.Outlined.WrapText,
                label = if (wordWrap) "Disabilita a capo" else "Abilita a capo",
                onClick = { wordWrap = !wordWrap },
            ),
        )
    }
}

/**
 * A syntax theme built from the app's Material scheme so the code view matches the rest of the
 * app rather than a stock IDE palette. Only the colours are overridden on a preset base.
 */
private fun appSyntaxTheme(scheme: ColorScheme): SyntaxTheme {
    fun rgb(color: Color) = color.toArgb() and 0x00FFFFFF
    return SyntaxThemes.darcula(darkMode = false).copy(
        code = rgb(scheme.onSurface),
        keyword = rgb(scheme.primary),
        string = rgb(scheme.tertiary),
        literal = rgb(scheme.secondary),
        comment = rgb(scheme.onSurfaceVariant.copy(alpha = 0.7f)),
        metadata = rgb(scheme.secondary),
        multilineComment = rgb(scheme.onSurfaceVariant.copy(alpha = 0.7f)),
        mark = rgb(scheme.primary),
        punctuation = rgb(scheme.onSurfaceVariant),
    )
}

/**
 * Caps for pathological files: beyond [MAX_HIGHLIGHT_CHARS] the text renders unhighlighted
 * rather than freezing the highlighter's regex pass; beyond [MAX_FILE_CHARS] it is truncated
 * with a marker line.
 */
private const val MAX_HIGHLIGHT_CHARS = 512 * 1024
private const val MAX_FILE_CHARS = 2 * 1024 * 1024

private fun highlightFile(
    localPath: String,
    fileName: String,
    theme: SyntaxTheme
): List<AnnotatedString> {
    var text = File(localPath).readText()
    if (text.length > MAX_FILE_CHARS) {
        text = text.take(MAX_FILE_CHARS) + "\n… (file troncato)"
    }
    text = text.replace("\t", "    ")
    val language = languageFor(fileName)
    if (language == SyntaxLanguage.DEFAULT || text.length > MAX_HIGHLIGHT_CHARS) {
        return text.lines().map { AnnotatedString(it) }
    }
    val highlights = Highlights.Builder()
        .code(text)
        .language(language)
        .theme(theme)
        .build()
        .getHighlights()
    val annotated = buildAnnotatedString {
        append(text)
        highlights.forEach { highlight ->
            val start = highlight.location.start.coerceIn(0, text.length)
            val end = highlight.location.end.coerceIn(start, text.length)
            when (highlight) {
                is ColorHighlight -> addStyle(
                    SpanStyle(color = Color(0xFF000000 or highlight.rgb.toLong())),
                    start,
                    end,
                )

                is BoldHighlight -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
            }
        }
    }
    return splitLines(annotated)
}

/** AnnotatedString has no lines(); slice on newline offsets so the spans survive the split. */
private fun splitLines(annotated: AnnotatedString): List<AnnotatedString> {
    val result = mutableListOf<AnnotatedString>()
    var start = 0
    val text = annotated.text
    while (start <= text.length) {
        val newline = text.indexOf('\n', start)
        val end = if (newline == -1) text.length else newline
        result += annotated.subSequence(start, end)
        if (newline == -1) break
        start = newline + 1
    }
    return result
}

/**
 * Maps a file extension to a highlighter grammar. Extensions without one (sql, asm, csv,
 * json, …) fall back to [SyntaxLanguage.DEFAULT]: clean monospace, no highlighting.
 */
private fun languageFor(fileName: String): SyntaxLanguage =
    when (fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)) {
        "java" -> SyntaxLanguage.JAVA
        "kt", "kts" -> SyntaxLanguage.KOTLIN
        "py", "ipynb" -> SyntaxLanguage.PYTHON
        "c" -> SyntaxLanguage.C
        "cpp", "cc", "cxx", "h", "hpp" -> SyntaxLanguage.CPP
        "cs" -> SyntaxLanguage.CSHARP
        "js" -> SyntaxLanguage.JAVASCRIPT
        "ts" -> SyntaxLanguage.TYPESCRIPT
        "sh", "bash" -> SyntaxLanguage.SHELL
        "pl" -> SyntaxLanguage.PERL
        "rb" -> SyntaxLanguage.RUBY
        "swift" -> SyntaxLanguage.SWIFT
        "go" -> SyntaxLanguage.GO
        "rs" -> SyntaxLanguage.RUST
        "dart" -> SyntaxLanguage.DART
        else -> SyntaxLanguage.DEFAULT
    }
