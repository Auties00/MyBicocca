package it.attendance100.mybicocca.data.daos

import androidx.room.*
import it.attendance100.mybicocca.data.entities.*
import kotlinx.coroutines.flow.*

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