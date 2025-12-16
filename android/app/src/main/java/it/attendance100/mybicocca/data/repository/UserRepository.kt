package it.attendance100.mybicocca.data.repository

import android.util.Log
import it.attendance100.mybicocca.data.local.dao.UserDao
import it.attendance100.mybicocca.data.local.entity.toEntity
import it.attendance100.mybicocca.domain.datasource.UserDataSource
import it.attendance100.mybicocca.domain.model.CareerStats
import it.attendance100.mybicocca.domain.model.User
import it.attendance100.mybicocca.domain.repository.UserRepository
import it.attendance100.mybicocca.util.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: UserDataSource, // Remote
    private val dao: UserDao,         // Local
    private val networkMonitor: NetworkMonitor,
) : UserRepository {

    /**
     * Retrieves the user from the Database.
     * Returns a Flow that immediately emits cached data if available.
     */
    override fun getUser(): Flow<User?> {
        return dao.getUser().map { entity ->
            if (entity != null) {
                Log.v("UserRepository", "  Emitting cached User data from Room DB")
            } else {
                Log.v("UserRepository", "  Room DB has no User data")
            }
            entity?.toDomain()
        }
    }

    override fun getCareerStats(): Flow<CareerStats?> {
        return dao.getCareerStats().map { entity ->
            if (entity != null) {
                Log.v("UserRepository", "  Emitting cached CareerStats from Room DB")
            } else {
                Log.v("UserRepository", "  Room DB has no CareerStats data")
            }
            entity?.toDomain()
        }
    }

    /**
     * Refreshes data from API and saves to DB.
     * Throws exception if network or auth fails, allowing the caller to handle it.
     */
    override suspend fun refreshUser() {
        if (!networkMonitor.isOnline.value) {
            Log.w("UserRepository", "Device is offline. Skipping user data refresh.")
            return
        }

        try {
            Log.d("UserRepository", "Refreshing user data...")
            // Fetch from Network
            val remoteUser = api.getUser()
            Log.d("UserRepository", "  User fetched: ${remoteUser.name}")

            val remoteStats = api.getCareerStats()
            Log.d("UserRepository", "  Career stats fetched. Exams: ${remoteStats.esamiSostenuti}")

            // Save to DB
            dao.insertUser(remoteUser.toEntity())
            dao.insertCareerStats(remoteStats.toEntity())
            Log.d("UserRepository", "  Data saved to local DB")

        } catch (e: Exception) {
            Log.e("UserRepository", "  Error refreshing user data", e)
            throw e // Propagate error to let ViewModel handle Auth failures
        }
    }
}