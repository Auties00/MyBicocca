package it.attendance100.mybicocca.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.attendance100.mybicocca.data.model.campus.Building
import it.attendance100.mybicocca.data.model.campus.Room
import kotlinx.coroutines.flow.Flow

@Dao
interface CampusDao {
    @Query("SELECT * FROM buildings ORDER BY name")
    fun observeBuildings(): Flow<List<Building>>

    @Upsert
    suspend fun upsertBuildings(buildings: List<Building>)

    @Query("DELETE FROM buildings")
    suspend fun deleteAllBuildings()

    @Query("SELECT * FROM rooms WHERE buildingCode = :buildingCode ORDER BY name")
    fun observeRoomsByBuilding(buildingCode: String): Flow<List<Room>>

    @Query("SELECT * FROM rooms ORDER BY name")
    fun observeAllRooms(): Flow<List<Room>>

    @Upsert
    suspend fun upsertRooms(rooms: List<Room>)

    @Query("DELETE FROM rooms WHERE buildingCode = :buildingCode")
    suspend fun deleteRoomsByBuilding(buildingCode: String)
}
