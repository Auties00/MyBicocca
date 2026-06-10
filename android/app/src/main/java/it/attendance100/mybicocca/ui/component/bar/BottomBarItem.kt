package it.attendance100.mybicocca.ui.component.bar

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/** One destination in [MyBicoccaBottomBar]; [key] is the stable identity the selection is matched and reported by. */
@Immutable
data class BottomBarItem<T>(
    val key: T,
    val label: String,
    val icon: ImageVector,
)
