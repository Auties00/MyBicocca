package it.attendance100.mybicocca.domain.contracts

import it.attendance100.mybicocca.domain.model.*
import kotlinx.coroutines.flow.*

/**
 * User operations contract
 * Defines methods for retrieving user profile and career stats
 */
interface UserRepository {
  fun getUser(): Flow<User>
  fun getCareerStats(): Flow<CareerStats>
}
