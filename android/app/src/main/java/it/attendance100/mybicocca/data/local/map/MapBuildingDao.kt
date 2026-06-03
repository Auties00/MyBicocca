package it.attendance100.mybicocca.data.local.map

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MapBuildingDao {

    // Numbered codes are zero-padded (U01..U38) so a plain string sort is numerically correct;
    // the GLOB term keeps the non-numbered sites (U-BG, U-MB) after them instead of first.
    @Query("SELECT * FROM map_buildings ORDER BY code GLOB 'U[0-9]*' DESC, code")
    fun observeAll(): Flow<List<MapBuildingEntity>>

    @Query("SELECT COUNT(*) FROM map_buildings")
    suspend fun count(): Int

    @Upsert
    suspend fun upsertAll(rows: List<MapBuildingEntity>)
}
