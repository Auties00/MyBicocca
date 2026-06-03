package it.attendance100.mybicocca.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector

enum class ShellTab(
    val label: String,
    val icon: ImageVector,
) {
    Calendar(label = "Calendario", icon = Icons.Outlined.CalendarMonth),
    Elearning(label = "E-learning", icon = Icons.Outlined.School),
    Map(label = "Mappe", icon = Icons.Outlined.Map),
    Registry(label = "Servizi", icon = Icons.Outlined.WorkOutline),
}
