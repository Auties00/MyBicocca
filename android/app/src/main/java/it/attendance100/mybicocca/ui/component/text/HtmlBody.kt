package it.attendance100.mybicocca.ui.component.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

// Renders teacher-authored Moodle HTML (assignment briefs, quiz questions, feedback,
// online-text submissions, section/module descriptions). Links open through the platform
// UriHandler, so a link inside a clickable row still routes to the browser on tap while the
// rest of the row triggers its own onClick.
@Composable
fun HtmlBody(
    html: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    // Default reproduces the original body style (14sp / 22sp). Callers can pass a compact
    // style (e.g. labelSmall) to render a description preview inside a dense list row.
    style: TextStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 22.sp),
    maxLines: Int = Int.MAX_VALUE,
) {
    val scheme = MaterialTheme.colorScheme
    val annotated = remember(html, scheme.primary) {
        AnnotatedString.fromHtml(
            htmlString = html,
            linkStyles = TextLinkStyles(
                style = SpanStyle(
                    color = scheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                ),
            ),
        ).trimWhitespace()
    }
    Text(
        text = annotated,
        style = style,
        color = color.takeIf { it != Color.Unspecified } ?: scheme.onSurfaceVariant,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

// fromHtml keeps the wrapping block's leading/trailing whitespace (and stray &nbsp;/<br>),
// which renders as blank space above and below the text. Trim to the first/last non-whitespace
// character, preserving link annotations inside the kept range. isWhitespace() also covers the
// non-breaking space U+00A0 that &nbsp; decodes to.
private fun AnnotatedString.trimWhitespace(): AnnotatedString {
    val start = text.indexOfFirst { !it.isWhitespace() }
    if (start == -1) return AnnotatedString("")
    val end = text.indexOfLast { !it.isWhitespace() }
    return if (start == 0 && end == text.lastIndex) this else subSequence(start, end + 1)
}
