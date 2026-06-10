package it.attendance100.mybicocca.data.local.map

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * A room persisted in the map_rooms table, synced per building from EasyStaff with the floor
 * enriched from the room showcase. Keyed by (code, building_code) — room codes are only unique
 * within a building — and indexed by building for the per-building queries. Not account-scoped.
 *
 * @property floor Floor level, 0 being the ground floor; null when the showcase has no card for
 * the room.
 */
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
