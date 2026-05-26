package it.attendance100.mybicocca.data.local.map

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "map_room_sync_state")
data class MapRoomSyncStateEntity(
    @PrimaryKey @ColumnInfo(name = "building_code") val buildingCode: String,
    @ColumnInfo(name = "last_refreshed_at_ms") val lastRefreshedAtMs: Long,
)
