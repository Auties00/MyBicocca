package it.attendance100.mybicocca.data.local.map

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MapRoomDao {

    @Query("SELECT * FROM map_rooms WHERE building_code = :buildingCode ORDER BY name")
    fun observeForBuilding(buildingCode: String): Flow<List<MapRoomEntity>>

    @Upsert
    suspend fun upsertAll(rows: List<MapRoomEntity>)

    @Query("DELETE FROM map_rooms WHERE building_code = :buildingCode")
    suspend fun deleteForBuilding(buildingCode: String)

    @Transaction
    suspend fun replaceForBuilding(buildingCode: String, rows: List<MapRoomEntity>) {
        deleteForBuilding(buildingCode)
        if (rows.isNotEmpty()) upsertAll(rows)
    }
}
