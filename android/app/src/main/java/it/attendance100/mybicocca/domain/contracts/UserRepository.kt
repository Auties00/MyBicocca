package it.attendance100.mybicocca.domain.contracts

import it.attendance100.mybicocca.domain.model.*
import kotlinx.coroutines.flow.*

interface UserRepository {
  fun getUser(): Flow<User>
  fun getCareerStats(): Flow<CareerStats>
}
