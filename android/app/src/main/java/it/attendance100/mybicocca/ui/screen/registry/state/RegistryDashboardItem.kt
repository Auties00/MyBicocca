package it.attendance100.mybicocca.ui.screen.registry.state

import androidx.compose.ui.graphics.vector.ImageVector

enum class RegistryTileStatus {
    Normal,
    Warning,
    Important,
}

data class RegistryDashboardTile(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val status: RegistryTileStatus = RegistryTileStatus.Normal,
    val icon: ImageVector,
    val shape: RegistryAccentShape,
    val category: RegistryCategory,
    val onClick: () -> Unit,
)
