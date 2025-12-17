package it.attendance100.mybicocca.domain.repository

import kotlinx.coroutines.flow.Flow
import java.net.URI

interface UserRepository {
    fun isLoggedIn(): Boolean
    suspend fun startLogin(): Flow<URI>
    suspend fun finishLogin(): Boolean
}
