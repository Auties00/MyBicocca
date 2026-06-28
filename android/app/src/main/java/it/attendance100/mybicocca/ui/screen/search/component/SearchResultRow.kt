package it.attendance100.mybicocca.ui.screen.search.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.search.matchHighlightRanges

/**
 * One result segment of a category group. Tonal ladder: plain surface (overlay) ->
 * surfaceContainerLow (row) -> surfaceContainerHigh (leading icon bed). Action rows colour
 * the icon bed with primaryContainer — the "this DOES something" cue.
 */
@Composable
fun SearchResultRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    query: String,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = { haptic.tap(); onClick() },
        shape = shape,
        color = scheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (accent) scheme.primaryContainer else scheme.surfaceContainerHigh,
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (accent) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
            ) {
                Text(
                    text = highlightedTitle(title, query, scheme.primary),
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * Title text whose query-matched chars light up (bold + primary) so the user sees WHY the
 * row matched — essential when typo and abbreviation tiers can surface non-obvious hits.
 */
@Composable
fun highlightedTitle(title: String, query: String, highlightColor: Color): AnnotatedString =
    remember(title, query, highlightColor) {
        val ranges = if (query.isBlank()) null else matchHighlightRanges(query, title)
        if (ranges.isNullOrEmpty()) {
            AnnotatedString(title)
        } else {
            buildAnnotatedString {
                append(title)
                val style = SpanStyle(fontWeight = FontWeight.Bold, color = highlightColor)
                for (range in ranges) {
                    val start = range.first.coerceIn(0, title.length)
                    val end = (range.last + 1).coerceIn(start, title.length)
                    if (end > start) addStyle(style, start, end)
                }
            }
        }
    }
