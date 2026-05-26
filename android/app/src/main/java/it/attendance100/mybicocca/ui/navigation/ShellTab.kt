package it.attendance100.mybicocca.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector

enum class ShellTab(
    val label: String,
    val searchPlaceholder: String,
    val icon: ImageVector,
) {
    Calendar(label = "Calendario", searchPlaceholder = "Cerca lezioni", icon = Icons.Outlined.CalendarMonth),
    Elearning(label = "E-learning", searchPlaceholder = "Cerca corsi", icon = Icons.Outlined.School),
    Map(label = "Mappe", searchPlaceholder = "Cerca aule", icon = Icons.Outlined.Map),
    Registry(label = "Servizi", searchPlaceholder = "Cerca", icon = Icons.Outlined.WorkOutline),
}
