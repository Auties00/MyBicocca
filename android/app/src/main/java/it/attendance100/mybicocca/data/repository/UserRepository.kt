package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.datasources.user.*
import it.attendance100.mybicocca.domain.model.*
import kotlinx.coroutines.flow.*
import javax.inject.*
import it.attendance100.mybicocca.domain.contracts.UserRepository as IUserRepository

/**
 * Implements user contract, uses datasource
 * Manages user data and career statistics
 */
class UserRepository @Inject constructor(
  private val dataSource: UserDataSource,
) : IUserRepository {
  override fun getUser(): Flow<User> = flow {
    emit(dataSource.getUser())
  }

  override fun getCareerStats(): Flow<CareerStats> = flow {
    emit(dataSource.getCareerStats())
  }
}
