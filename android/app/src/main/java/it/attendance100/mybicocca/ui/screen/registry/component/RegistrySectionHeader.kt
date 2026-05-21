package it.attendance100.mybicocca.ui.screen.registry.component

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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryCategory
import it.attendance100.mybicocca.ui.screen.registry.state.colors
import it.attendance100.mybicocca.ui.screen.registry.state.glyph
import it.attendance100.mybicocca.ui.screen.registry.state.label
import it.attendance100.mybicocca.ui.screen.registry.state.materialShape

@Composable
fun RegistrySectionHeader(
    category: RegistryCategory,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val palette = category.colors(scheme)
    val glyphShape = category.glyph().materialShape()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .graphicsLayer { rotationZ = -12f }
                .clip(glyphShape)
                .background(palette.accent),
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = category.label,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
            letterSpacing = (-0.5).sp,
        )
    }
}
