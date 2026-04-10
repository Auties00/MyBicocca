package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.MyBicoccaDatabase
import it.attendance100.mybicocca.data.datasource.user.UnifiedAuthDataSource
import it.attendance100.mybicocca.data.sync.ResourceSyncManager
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authDataSource: UnifiedAuthDataSource,
    private val database: MyBicoccaDatabase,
    private val resourceSyncManager: ResourceSyncManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        authDataSource.login(email, password)
    }

    suspend fun logout(): Result<Unit> = runCatching {
        authDataSource.logout()
        withContext(ioDispatcher) {
            database.clearAllTables()
        }
        resourceSyncManager.clearAll()
    }

    fun isLoggedIn(): Boolean = authDataSource.isLoggedIn()
}
