package it.attendance100.mybicocca.data.local.map

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "map_rooms",
    primaryKeys = ["code", "building_code"],
    indices = [Index("building_code")],
)
data class MapRoomEntity(
    val code: String,
    @ColumnInfo(name = "building_code") val buildingCode: String,
    val name: String,
    val capacity: Int?,
    val floor: Int?,
)
