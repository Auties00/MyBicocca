package it.attendance100.mybicocca.ui.component.bar

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * The app's main tab bar. In light theme the selection indicator is the brand primary with
 * onPrimary (white) iconography; in dark theme it switches to the primary-container role pair,
 * sidestepping the black-on-red pairing dark schemes give onPrimary.
 *
 * [translationY] is applied in the graphics layer so the shell can slide the whole bar
 * off-screen without triggering relayout of the content around it.
 */
@Composable
fun <T> MyBicoccaBottomBar(
    items: List<BottomBarItem<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    translationY: Float = 0f,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val itemColors = NavigationBarItemDefaults.colors(
        indicatorColor = if (dark) scheme.primaryContainer else scheme.primary,
        selectedIconColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
        selectedTextColor = scheme.onSurface,
        unselectedIconColor = scheme.onSurfaceVariant,
        unselectedTextColor = scheme.onSurfaceVariant,
    )
    NavigationBar(
        modifier = modifier.graphicsLayer { this.translationY = translationY },
        containerColor = scheme.surfaceContainer,
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.key == selected,
                onClick = { onSelect(item.key) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = itemColors,
            )
        }
    }
}
