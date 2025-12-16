package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.CareerStats
import it.attendance100.mybicocca.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * User operations contract
 * Defines methods for retrieving user profile and career stats
 */
interface UserRepository {
    fun getUser(): Flow<User?>
    fun getCareerStats(): Flow<CareerStats?>
    suspend fun refreshUser()
}
