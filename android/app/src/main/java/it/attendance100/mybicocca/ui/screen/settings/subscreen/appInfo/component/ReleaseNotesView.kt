package it.attendance100.mybicocca.ui.screen.settings.subscreen.appInfo.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import it.attendance100.mybicocca.core.release.BulletItem
import it.attendance100.mybicocca.core.release.CalloutKind
import it.attendance100.mybicocca.core.release.ReleaseBlock
import it.attendance100.mybicocca.core.release.ReleaseNotes

/**
 * Renders parsed [ReleaseNotes] as a GitHub-style changelog: sized headings, bulleted items, and
 * alert callouts, with inline Markdown (bold/italic/strikethrough/underline/inline-code, plus
 * colour-styled links and @mentions) realized through [buildInlineNotes]. Links and mentions are
 * intentionally non-interactive — the release card that hosts this view owns the single tap target
 * that opens the release page.
 */
@Composable
fun ReleaseNotesView(
    notes: ReleaseNotes,
    modifier: Modifier = Modifier,
) {
    val styles = rememberInlineStyles()
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        notes.blocks.forEachIndexed { index, block ->
            when (block) {
                is ReleaseBlock.Heading -> Text(
                    text = remember(block.text, styles) { buildInlineNotes(block.text, styles) },
                    style = headingStyle(block.level),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = if (index == 0) 0.dp else 4.dp),
                )

                is ReleaseBlock.Paragraph -> Text(
                    text = remember(block.text, styles) { buildInlineNotes(block.text, styles) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                is ReleaseBlock.BulletList -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    block.items.forEach { item -> BulletRow(item = item, styles = styles) }
                }

                is ReleaseBlock.Callout -> CalloutBox(
                    kind = block.kind,
                    text = block.text,
                    version = block.version,
                    styles = styles,
                )
            }
        }
    }
}

@Composable
private fun BulletRow(item: BulletItem, styles: InlineStyles) {
    val secondary = MaterialTheme.colorScheme.onSurfaceVariant
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = secondary,
            modifier = Modifier.padding(end = 8.dp),
        )
        // The version chip (merged view only) leads the line as a hanging tag; the text wraps to its right.
        if (item.version != null) {
            VersionChip(item.version)
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = remember(item.text, styles) { buildInlineNotes(item.text, styles) },
            style = MaterialTheme.typography.bodyMedium,
            color = secondary,
            modifier = Modifier.weight(1f),
        )
    }
}

/** A compact `v1.2.3` pill marking which release a merged item or callout came from. */
@Composable
private fun VersionChip(version: String) {
    val accent = MaterialTheme.colorScheme.primary
    Text(
        text = if (version.startsWith("v")) version else "v$version",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = accent,
        modifier = Modifier
            .background(accent.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 1.dp),
    )
}

@Composable
private fun CalloutBox(kind: CalloutKind, text: String, version: String?, styles: InlineStyles) {
    val accent = calloutAccent(kind)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.10f)),
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(accent),
        )
        Column(
            modifier = Modifier.padding(
                start = 12.dp,
                end = 14.dp,
                top = 10.dp,
                bottom = 12.dp
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = calloutIcon(kind),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = calloutLabel(kind),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                )
                if (version != null) {
                    Spacer(Modifier.weight(1f))
                    VersionChip(version)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = remember(text, styles) { buildInlineNotes(text, styles) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun headingStyle(level: Int): TextStyle {
    val typography = MaterialTheme.typography
    val base = when (level) {
        1 -> typography.headlineSmall
        2 -> typography.titleLarge
        3 -> typography.titleMedium
        4 -> typography.titleSmall
        else -> typography.bodyLarge
    }
    return base.copy(fontWeight = FontWeight.Bold)
}

/** Inline span colours pulled from the theme once, so [buildInlineNotes] stays a pure function. */
data class InlineStyles(
    val linkColor: Color,
    val codeColor: Color,
    val codeBackground: Color,
)

@Composable
private fun rememberInlineStyles(): InlineStyles {
    val scheme = MaterialTheme.colorScheme
    return remember(scheme.primary, scheme.onSurface, scheme.surfaceContainerHighest) {
        InlineStyles(
            linkColor = scheme.primary,
            codeColor = scheme.onSurface,
            codeBackground = scheme.surfaceContainerHighest,
        )
    }
}

private val BoldSpan = SpanStyle(fontWeight = FontWeight.Bold)
private val ItalicSpan = SpanStyle(fontStyle = FontStyle.Italic)
private val StrikeSpan = SpanStyle(textDecoration = TextDecoration.LineThrough)
private val UnderlineSpan = SpanStyle(textDecoration = TextDecoration.Underline)

/**
 * Builds an [AnnotatedString] from one line of inline Markdown.
 * Handles `` `code` ``, `**bold**`, `*italic*`, `~~strike~~`, the `<u>/<b>/<i>/<del>/<s>/<em>/<strong>/<ins>/<br>` HTML tags
 * GitHub allows inline, `[text](url)` links and `@mentions` (both colour-styled, never clickable).
 *
 * Single pass: literal characters are appended and emphasis is recorded as style ranges via
 * [AnnotatedString.Builder.addStyle], which — unlike push/pop — tolerates the arbitrary overlap of
 * real-world notes. Unterminated markers degrade to plain text. Underscores are not treated as emphasis.
 */
fun buildInlineNotes(raw: String, styles: InlineStyles): AnnotatedString = buildAnnotatedString {
    val codeSpan = SpanStyle(
        fontFamily = FontFamily.Monospace,
        background = styles.codeBackground,
        color = styles.codeColor,
        fontSize = 0.85.em,
    )
    val linkSpan = SpanStyle(color = styles.linkColor, fontWeight = FontWeight.SemiBold)

    var bold: Int? = null
    var italic: Int? = null
    var strike: Int? = null
    var underline: Int? = null

    fun toggle(open: Int?, span: SpanStyle, set: (Int?) -> Unit) {
        if (open == null) set(length) else {
            addStyle(span, open, length)
            set(null)
        }
    }

    var i = 0
    val n = raw.length
    while (i < n) {
        when (val c = raw[i]) {
            // Code spans
            '`' -> {
                val end = raw.indexOf('`', i + 1)
                if (end > i) {
                    val start = length
                    append(raw.substring(i + 1, end))
                    addStyle(codeSpan, start, length)
                    i = end + 1
                } else {
                    append(c); i++
                }
            }

            // Links
            '[' -> {
                val close = raw.indexOf(']', i + 1)
                if (close > i && close + 1 < n && raw[close + 1] == '(') {
                    val paren = raw.indexOf(')', close + 2)
                    if (paren > close) {
                        val start = length
                        append(raw.substring(i + 1, close))
                        addStyle(linkSpan, start, length)
                        i = paren + 1
                    } else {
                        append(c); i++
                    }
                } else {
                    append(c); i++
                }
            }

            // Mentions
            '@' if (i == 0 || !raw[i - 1].isLetterOrDigit()) -> {
                var j = i + 1
                while (j < n && (raw[j].isLetterOrDigit() || raw[j] == '-')) j++
                if (j > i + 1) {
                    val start = length
                    append(raw.substring(i, j))
                    addStyle(linkSpan, start, length)
                    i = j
                } else {
                    append(c); i++
                }
            }

            // HTML tags
            '<' -> {
                val close = raw.indexOf('>', i + 1)
                if (close > i) {
                    when (raw.substring(i + 1, close).trim().lowercase()) {
                        "u", "ins" -> underline = length
                        "/u", "/ins" -> toggle(underline, UnderlineSpan) { underline = it }
                        "b", "strong" -> bold = length
                        "/b", "/strong" -> toggle(bold, BoldSpan) { bold = it }
                        "i", "em" -> italic = length
                        "/i", "/em" -> toggle(italic, ItalicSpan) { italic = it }
                        "s", "del", "strike" -> strike = length
                        "/s", "/del", "/strike" -> toggle(strike, StrikeSpan) { strike = it }
                        "br", "br/", "br /" -> append("\n")
                        else -> Unit // unknown tag: drop it
                    }
                    i = close + 1
                } else {
                    append(c); i++
                }
            }

            // Emphasis markers
            '*' if i + 1 < n && raw[i + 1] == '*' -> {
                toggle(bold, BoldSpan) { bold = it }; i += 2
            }

            // Strikethrough
            '~' if i + 1 < n && raw[i + 1] == '~' -> {
                toggle(strike, StrikeSpan) { strike = it }; i += 2
            }

            // Underline
            '*' -> {
                toggle(italic, ItalicSpan) { italic = it }; i++
            }

            else -> {
                append(c); i++
            }
        }
    }
}

private fun calloutAccent(kind: CalloutKind): Color = when (kind) {
    CalloutKind.NOTE -> Color(0xFF1F6FEB)
    CalloutKind.TIP -> Color(0xFF2DA44E)
    CalloutKind.IMPORTANT -> Color(0xFF8250DF)
    CalloutKind.WARNING -> Color(0xFFBF8700)
    CalloutKind.CAUTION -> Color(0xFFCF222E)
}

private fun calloutIcon(kind: CalloutKind): ImageVector = when (kind) {
    CalloutKind.NOTE -> Icons.Outlined.Info
    CalloutKind.TIP -> Icons.Outlined.Lightbulb
    CalloutKind.IMPORTANT -> Icons.Outlined.Campaign
    CalloutKind.WARNING -> Icons.Outlined.WarningAmber
    CalloutKind.CAUTION -> Icons.Outlined.Report
}

private fun calloutLabel(kind: CalloutKind): String = when (kind) {
    CalloutKind.NOTE -> "Note"
    CalloutKind.TIP -> "Tip"
    CalloutKind.IMPORTANT -> "Important"
    CalloutKind.WARNING -> "Warning"
    CalloutKind.CAUTION -> "Caution"
}
