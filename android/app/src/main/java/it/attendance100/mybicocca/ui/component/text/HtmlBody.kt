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
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

// Renders teacher-authored Moodle HTML (assignment briefs, quiz questions, feedback,
// online-text submissions). Links open through the platform UriHandler.
@Composable
fun HtmlBody(
    html: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
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
        )
    }
    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium,
        color = color.takeIf { it != Color.Unspecified } ?: scheme.onSurfaceVariant,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        modifier = modifier,
    )
}
