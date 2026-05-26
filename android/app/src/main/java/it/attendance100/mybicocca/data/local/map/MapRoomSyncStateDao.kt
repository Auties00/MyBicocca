package it.attendance100.mybicocca.data.local.map

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface MapRoomSyncStateDao {

    @Query("SELECT * FROM map_room_sync_state WHERE building_code = :buildingCode")
    suspend fun getState(buildingCode: String): MapRoomSyncStateEntity?

    @Upsert
    suspend fun upsertState(state: MapRoomSyncStateEntity)
}
