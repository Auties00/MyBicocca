package it.attendance100.mybicocca.ui.component.bar

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme

/**
 * The app's main tab bar. In light theme the selection indicator is the brand primary with
 * onPrimary (white) iconography; in dark theme it switches to the primary-container role pair,
 * sidestepping the black-on-red pairing dark schemes give onPrimary.
 *
 * [hideProgress] (0 shown, 1 hidden) slides the bar down by its own measured height in the
 * graphics layer, so it clears the screen at any density without relayout of the content around it.
 */
@Composable
fun <T> MyBicoccaBottomBar(
    items: List<BottomBarItem<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    hideProgress: Float = 0f,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val itemColors = NavigationBarItemDefaults.colors(
        indicatorColor = if (dark) scheme.primaryContainer else scheme.primary,
        selectedIconColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
        selectedTextColor = scheme.onSurface,
        unselectedIconColor = scheme.onSurfaceVariant,
        unselectedTextColor = scheme.onSurfaceVariant,
    )
    val haptic = rememberHapticManager()
    NavigationBar(
        modifier = modifier.graphicsLayer { translationY = hideProgress * size.height },
        containerColor = scheme.surfaceContainer,
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.key == selected,
                onClick = {
                    haptic.tap()
                    onSelect(item.key)
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = itemColors,
            )
        }
    }
}
