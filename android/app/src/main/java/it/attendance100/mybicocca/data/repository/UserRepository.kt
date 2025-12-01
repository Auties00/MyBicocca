package it.attendance100.mybicocca.data.repository

import android.util.*
import it.attendance100.mybicocca.data.daos.*
import it.attendance100.mybicocca.data.datasources.user.*
import it.attendance100.mybicocca.data.entities.*
import it.attendance100.mybicocca.domain.contracts.UserRepository
import it.attendance100.mybicocca.domain.model.*
import kotlinx.coroutines.flow.*
import javax.inject.*

class UserRepository @Inject constructor(
  private val api: UserDataSource, // Remote
  private val dao: UserDao,         // Local
) : UserRepository {

  /**
   * Retrieves the user from the Database.
   * Returns a Flow that immediately emits cached data if available.
   */
  override fun getUser(): Flow<User?> {
    return dao.getUser().map { entity ->
      entity?.toDomain()
    }
  }

  override fun getCareerStats(): Flow<CareerStats?> {
    return dao.getCareerStats().map { entity ->
      entity?.toDomain()
    }
  }

  /**
   * Refreshes data from API and saves to DB.
   * Throws exception if network or auth fails, allowing the caller to handle it.
   */
  override suspend fun refreshUser() {
    try {
      Log.d("UserRepository", "Refreshing user data...")
      // Fetch from Network
      val remoteUser = api.getUser()
      Log.d("UserRepository", "User fetched: ${remoteUser.name}")
      
      val remoteStats = api.getCareerStats()
      Log.d("UserRepository", "Career stats fetched. Exams: ${remoteStats.esamiSostenuti}")

      // Save to DB
      dao.insertUser(remoteUser.toEntity())
      dao.insertCareerStats(remoteStats.toEntity())
      Log.d("UserRepository", "Data saved to local DB")

    } catch (e: Exception) {
      Log.e("UserRepository", "Error refreshing user data", e)
      throw e // Propagate error to let ViewModel handle Auth failures
    }
  }
}