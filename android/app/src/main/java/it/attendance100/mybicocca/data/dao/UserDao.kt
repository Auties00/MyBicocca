package it.attendance100.mybicocca.data.dao

import androidx.room.*
import it.attendance100.mybicocca.domain.model.*
import kotlinx.coroutines.flow.*

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 0")
    fun getUser(): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("DELETE FROM user_profile")
    suspend fun deleteUser()

    // --- Career Stats ---
    @Query("SELECT * FROM career_stats WHERE id = 0")
    fun getCareerStats(): Flow<CareerStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCareerStats(stats: CareerStats)

    @Query("DELETE FROM career_stats")
    suspend fun deleteCareerStats()


  // --- Taxes ---
  @Query("SELECT * FROM taxes ORDER BY due_date DESC")
  fun observeTaxes(): androidx.lifecycle.LiveData<List<it.attendance100.mybicocca.domain.model.Tax>>


  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTaxes(taxes: List<it.attendance100.mybicocca.domain.model.Tax>)


  @Query("DELETE FROM taxes")
  suspend fun clearTaxes()


  // --- Alerts ---
  @Query("SELECT * FROM alerts ORDER BY date DESC")
  fun observeAlerts(): androidx.lifecycle.LiveData<List<it.attendance100.mybicocca.domain.model.Alert>>


  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAlerts(alerts: List<it.attendance100.mybicocca.domain.model.Alert>)


  @Query("DELETE FROM alerts")
  suspend fun clearAlerts()
}