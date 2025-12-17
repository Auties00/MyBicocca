package it.attendance100.mybicocca.data.repository

import kotlinx.coroutines.flow.Flow
import java.net.URI
import it.attendance100.mybicocca.domain.repository.UserRepository as IUserRepository

class UserRepository : IUserRepository {
    override fun isLoggedIn(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun startLogin(): Flow<URI> {
        TODO("Not yet implemented")
    }

    override suspend fun finishLogin(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun refreshSession() {
        TODO("Not yet implemented")
    }
}