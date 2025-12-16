package it.attendance100.mybicocca.domain.datasource

import retrofit2.Response

interface AuthDataSource {
    suspend fun getAuthHeaders(code: String, state: String, cookie: String): Response<Unit>
}