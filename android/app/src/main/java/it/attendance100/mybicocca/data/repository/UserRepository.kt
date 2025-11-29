package it.attendance100.mybicocca.data.repository

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
   */
  override suspend fun refreshUser() {
    try {
      // Fetch from Network
      val remoteUser = api.getUser()
      val remoteStats = api.getCareerStats()

      // Save to DB
      dao.insertUser(remoteUser.toEntity())
      dao.insertCareerStats(remoteStats.toEntity())

    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}