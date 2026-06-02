package it.attendance100.mybicocca.ui.screen.registry.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import it.attendance100.mybicocca.ui.component.SectionHeader
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
    val palette = category.colors(MaterialTheme.colorScheme)
    SectionHeader(
        title = category.label,
        accent = palette.accent,
        glyph = category.glyph().materialShape(),
        modifier = modifier,
    )
}
