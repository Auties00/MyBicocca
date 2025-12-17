package it.attendance100.mybicocca.domain.datasource

import retrofit2.*

interface AuthDataSource {
  /**
   * Retrieves authentication headers using the provided code, state, and cookie
   */
    suspend fun getAuthHeaders(code: String, state: String, cookie: String): Response<Unit>
}