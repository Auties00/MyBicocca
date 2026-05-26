package it.attendance100.mybicocca.ui.screen.map.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.School
import androidx.compose.ui.graphics.vector.ImageVector
import it.attendance100.mybicocca.domain.model.map.BuildingCategory

val BuildingCategory.label: String
    get() = when (this) {
        BuildingCategory.TEACHING -> "Didattica"
        BuildingCategory.LIBRARY -> "Biblioteca"
        BuildingCategory.CANTEEN -> "Mensa"
        BuildingCategory.ADMIN -> "Segreterie"
        BuildingCategory.SPORT -> "Sport"
        BuildingCategory.OTHER -> "Altro"
    }

val BuildingCategory.icon: ImageVector
    get() = when (this) {
        BuildingCategory.TEACHING -> Icons.Outlined.School
        BuildingCategory.LIBRARY -> Icons.Outlined.LocalLibrary
        BuildingCategory.CANTEEN -> Icons.Outlined.Restaurant
        BuildingCategory.ADMIN -> Icons.Outlined.Business
        BuildingCategory.SPORT -> Icons.Outlined.FitnessCenter
        BuildingCategory.OTHER -> Icons.Outlined.Place
    }
