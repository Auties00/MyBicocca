package it.attendance100.mybicocca.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "map_locations")
data class MapLocation(
    @PrimaryKey @ColumnInfo(name = "name") val name: String,
	@ColumnInfo(name = "description") val description: String,
	@ColumnInfo(name = "category") val category: String,
	@ColumnInfo(name = "latitude") val latitude: Double,
	@ColumnInfo(name = "longitude") val longitude: Double
)
