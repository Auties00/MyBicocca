package it.attendance100.mybicocca.data.local.map

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "map_buildings")
data class MapBuildingEntity(
    @PrimaryKey val code: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val address: String?,
    val city: String?,
)
