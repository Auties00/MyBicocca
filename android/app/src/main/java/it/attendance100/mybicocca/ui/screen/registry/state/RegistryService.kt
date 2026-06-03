package it.attendance100.mybicocca.ui.screen.registry.state

import androidx.compose.ui.graphics.vector.ImageVector

// A single tappable row inside a directory group (icon · title · subtitle · optional badge).
data class RegistryService(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badge: RegistryBadge? = null,
    // True when tapping leaves the app's own navigation (in-app browser / external site):
    // the tile then shows a link icon instead of the chevron.
    val external: Boolean = false,
    val onClick: () -> Unit,
)

// An outlined directory group heading a cluster of related services.
data class RegistryServiceGroup(
    val name: String,
    val caption: String,
    val services: List<RegistryService>,
)
