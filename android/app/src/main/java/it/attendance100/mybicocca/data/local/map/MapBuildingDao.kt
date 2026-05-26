package it.attendance100.mybicocca.data.local.map

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MapBuildingDao {

    @Query("SELECT * FROM map_buildings ORDER BY name")
    fun observeAll(): Flow<List<MapBuildingEntity>>

    @Query("SELECT COUNT(*) FROM map_buildings")
    suspend fun count(): Int

    @Upsert
    suspend fun upsertAll(rows: List<MapBuildingEntity>)
}
