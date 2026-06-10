package it.attendance100.mybicocca.data.local.map

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Timestamp of the last completed room sync, one row per building keyed by building code; drives
 * the TTL check that skips re-fetching a building's rooms while the cached copy is fresh.
 *
 * @property lastRefreshedAtMs Wall-clock epoch milliseconds of the last completed sync.
 */
@Entity(tableName = "map_room_sync_state")
data class MapRoomSyncStateEntity(
    @PrimaryKey @ColumnInfo(name = "building_code") val buildingCode: String,
    @ColumnInfo(name = "last_refreshed_at_ms") val lastRefreshedAtMs: Long,
)
