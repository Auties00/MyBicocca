package it.attendance100.mybicocca.ui.screen.settings.state

import androidx.compose.ui.graphics.vector.ImageVector

/** A single tappable row inside a settings group (icon · title · subtitle · trailing). */
data class SettingsEntry(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    /**
     * Whether tapping leaves the app's own navigation (in-app browser / external site);
     * the tile then shows a link icon in place of the chevron.
     */
    val external: Boolean = false,
    /** Non-null renders a switch in place of the chevron; tapping the row toggles it. */
    val switch: Boolean? = null,
    val onClick: () -> Unit,
)

/** A directory group heading a cluster of related settings sub-pages. */
data class SettingsEntryGroup(
    val name: String,
    val caption: String,
    val entries: List<SettingsEntry>,
)
