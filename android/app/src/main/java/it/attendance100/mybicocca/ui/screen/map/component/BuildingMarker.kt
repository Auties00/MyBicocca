package it.attendance100.mybicocca.ui.screen.map.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.map.BuildingCategory

// Rasterized into a Google Maps marker via MarkerComposable. A branded circular pin carrying
// the building's category glyph; the selected pin grows and swaps to the container color.
@Composable
fun BuildingMarker(
    category: BuildingCategory,
    selected: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    val container = if (selected) scheme.primaryContainer else scheme.primary
    val content = if (selected) scheme.onPrimaryContainer else scheme.onPrimary
    val diameter = if (selected) 48.dp else 38.dp

    Surface(
        shape = CircleShape,
        color = container,
        contentColor = content,
        border = androidx.compose.foundation.BorderStroke(2.dp, scheme.surface),
        shadowElevation = 4.dp,
    ) {
        Box(
            modifier = Modifier.size(diameter).padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = category.icon, contentDescription = null)
        }
    }
}
