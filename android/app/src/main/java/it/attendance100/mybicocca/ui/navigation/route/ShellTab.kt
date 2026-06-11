package it.attendance100.mybicocca.ui.navigation.route

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector
import it.attendance100.mybicocca.R

/**
 * The four core tabs of [MainShell]'s bottom bar. Declaration order is load-bearing: the shell's
 * tab pager uses the enum ordinal as its page index.
 */
enum class ShellTab(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Calendar(labelRes = R.string.nav_tab_calendar, icon = Icons.Outlined.CalendarMonth),
    Elearning(labelRes = R.string.nav_tab_elearning, icon = Icons.Outlined.School),
    Map(labelRes = R.string.nav_tab_map, icon = Icons.Outlined.Map),
    Registry(labelRes = R.string.nav_tab_registry, icon = Icons.Outlined.WorkOutline),
}
