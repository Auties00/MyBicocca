package it.attendance100.mybicocca.data.datasources.user

import it.attendance100.mybicocca.domain.model.*

/**
 * Abstraction for user data retrieval
 * Defines methods to fetch user profile and career statistics
 */
interface UserDataSource {
  suspend fun getUser(): User
  suspend fun getCareerStats(): CareerStats
}
