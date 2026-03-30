package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.UserDao
import it.attendance100.mybicocca.data.datasource.user.Esse3UserDataSource
import it.attendance100.mybicocca.data.model.user.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val dataSource: Esse3UserDataSource,
    private val dao: UserDao,
) {
    fun observeUser(): Flow<User?> = dao.observeUser()

    suspend fun refresh(): Result<Unit> = runCatching {
        val user = dataSource.getUser()
        dao.upsert(user)
    }
}
