package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.UserDao
import it.attendance100.mybicocca.data.datasource.user.UnifiedAuthDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authDataSource: UnifiedAuthDataSource,
    private val userDao: UserDao,
) {
    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        authDataSource.login(email, password)
    }

    suspend fun logout(): Result<Unit> = runCatching {
        authDataSource.logout()
        userDao.deleteAll()
    }

    fun isLoggedIn(): Boolean = authDataSource.isLoggedIn()
}
