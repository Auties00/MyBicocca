package it.attendance100.mybicocca.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.attendance100.mybicocca.domain.model.CareerStatsEntity
import it.attendance100.mybicocca.domain.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 0")
    fun getUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM user_profile")
    suspend fun deleteUser()

    // --- Career Stats ---
    @Query("SELECT * FROM career_stats WHERE id = 0")
    fun getCareerStats(): Flow<CareerStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCareerStats(stats: CareerStatsEntity)

    @Query("DELETE FROM career_stats")
    suspend fun deleteCareerStats()
}