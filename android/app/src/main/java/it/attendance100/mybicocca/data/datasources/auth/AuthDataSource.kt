package it.attendance100.mybicocca.data.datasources.auth

import retrofit2.*

interface AuthDataSource {
  suspend fun getAuthHeaders(code: String, state: String, cookie: String): Response<Unit>
}