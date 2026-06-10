package it.attendance100.mybicocca.data.local.map

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A campus building persisted in the map_buildings table, seeded from the bundled buildings.json
 * catalog. Keyed by the building code; not account-scoped, as campus geometry is shared.
 *
 * @property category Name of a BuildingCategory enum entry; unrecognized values degrade to the
 * generic fallback when read.
 */
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
