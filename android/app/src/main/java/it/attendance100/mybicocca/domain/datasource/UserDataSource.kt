package it.attendance100.mybicocca.domain.datasource

import it.attendance100.mybicocca.domain.model.CareerStats
import it.attendance100.mybicocca.domain.model.User

/**
 * Abstraction for user data retrieval
 * Defines methods to fetch user profile and career statistics
 */
interface UserDataSource {
    suspend fun getUser(): User
    suspend fun getCareerStats(): CareerStats
}