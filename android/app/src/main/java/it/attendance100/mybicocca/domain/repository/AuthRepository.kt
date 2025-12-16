package it.attendance100.mybicocca.domain.repository

interface AuthRepository {
    /**
     * Exchanges the code/state from the IdP redirect for the actual session headers.
     * @return true if successful
     */
    suspend fun performLoginCallback(code: String, state: String, cookie: String): Boolean
    fun isUserLoggedIn(): Boolean
    fun logout()
}