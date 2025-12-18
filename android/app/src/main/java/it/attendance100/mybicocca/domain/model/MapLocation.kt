package it.attendance100.mybicocca.domain.model

import androidx.room.*

@Entity(tableName = "map_locations")
data class MapLocation(
	@PrimaryKey val id: String,
	@ColumnInfo(name = "name") val name: String,
	@ColumnInfo(name = "description") val description: String?,
	@ColumnInfo(name = "category") val category: String,
	@ColumnInfo(name = "latitude") val latitude: Double,
	@ColumnInfo(name = "longitude") val longitude: Double,
	@ColumnInfo(name = "building") val building: String? = null,
	@ColumnInfo(name = "floor") val floor: String? = null,
)
