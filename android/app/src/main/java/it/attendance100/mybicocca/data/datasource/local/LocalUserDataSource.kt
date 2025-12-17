package it.attendance100.mybicocca.data.datasource.local

import it.attendance100.mybicocca.data.local.dao.*
import it.attendance100.mybicocca.data.local.entity.*
import kotlinx.coroutines.flow.*
import javax.inject.*

class LocalUserDataSource @Inject constructor(
  private val userDao: UserDao,
) {
  fun getUser(): Flow<UserEntity?> = userDao.getUser()

  suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)

  fun getCareerStats(): Flow<CareerStatsEntity?> = userDao.getCareerStats()

  suspend fun insertCareerStats(stats: CareerStatsEntity) = userDao.insertCareerStats(stats)

  suspend fun clear() = userDao.clear()
}
