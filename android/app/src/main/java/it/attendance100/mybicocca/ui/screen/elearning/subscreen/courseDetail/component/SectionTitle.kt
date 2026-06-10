package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Decorative list-section heading: a tertiary-tinted, letter-spaced uppercase label led by a
 * small ornament glyph, with an optional italic subtitle underneath.
 */
@Composable
fun SectionTitle(
    title: String,
    mark: String = "✿",
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 18.dp, bottom = 10.dp),
    ) {
        Text(
            text = "$mark ${title.uppercase()}",
            color = scheme.tertiary,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.4.sp,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}
