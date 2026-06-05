package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.BoldHighlight
import dev.snipme.highlights.model.ColorHighlight
import dev.snipme.highlights.model.SyntaxLanguage
import dev.snipme.highlights.model.SyntaxThemes
import it.attendance100.mybicocca.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

// IntelliJ-flavored read-only code view: Darcula highlighting (Highlights engine),
// JetBrains Mono, line-number gutter, shared horizontal scroll for long lines, pinch
// zoom. Rendering one Text per line inside a LazyColumn keeps multi-thousand-line
// sources virtualized instead of laying out one giant paragraph.
@Composable
fun CodeViewerContent(
    localPath: String,
    fileName: String,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    val lines by produceState<List<AnnotatedString>?>(initialValue = null, localPath, darkTheme) {
        value = withContext(Dispatchers.Default) {
            runCatching { highlightFile(localPath, fileName, darkTheme) }.getOrNull()
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
    val background = if (darkTheme) Color(0xFF2B2B2B) else Color(0xFFFDFDFD)
    val gutterColor = if (darkTheme) Color(0xFF606366) else Color(0xFFADADAD)
    val codeColor = if (darkTheme) Color(0xFFA9B7C6) else Color(0xFF080808)

    SelectionContainer {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(background)
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        fontScale = (fontScale * zoom).coerceIn(0.6f, 2.5f)
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
                        softWrap = false,
                        modifier = Modifier
                            .horizontalScroll(hScroll)
                            .padding(end = 16.dp),
                    )
                }
            }
            item {
                Box(modifier = Modifier.padding(bottom = 32.dp))
            }
        }
    }
}

// Cap pathological files: beyond this the tail renders unhighlighted rather than
// freezing the regex pass.
private const val MAX_HIGHLIGHT_CHARS = 512 * 1024
private const val MAX_FILE_CHARS = 2 * 1024 * 1024

private fun highlightFile(localPath: String, fileName: String, darkTheme: Boolean): List<AnnotatedString> {
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
        .theme(SyntaxThemes.darcula(darkMode = darkTheme))
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

// AnnotatedString has no lines(); slice on \n offsets so the spans survive the split.
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
        // sql, m, asm, csv, json, … — no grammar in this engine; clean monospace fallback.
        else -> SyntaxLanguage.DEFAULT
    }
