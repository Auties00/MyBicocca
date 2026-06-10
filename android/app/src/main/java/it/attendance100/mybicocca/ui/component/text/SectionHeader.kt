package it.attendance100.mybicocca.ui.component.text

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * App-wide section header: an optional 30dp rotated accent glyph followed by a [headlineSmall] Bold
 * title. Pass a null [glyph] for a title-only header. Shared by the Registry dashboard and Profile —
 * use it wherever an in-content section needs a header.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    accent: Color = Color.Unspecified,
    glyph: Shape? = null,
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (glyph != null) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .graphicsLayer { rotationZ = -12f }
                    .clip(glyph)
                    .background(accent),
            )
            Spacer(Modifier.width(14.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
            letterSpacing = (-0.5).sp,
        )
    }
}
