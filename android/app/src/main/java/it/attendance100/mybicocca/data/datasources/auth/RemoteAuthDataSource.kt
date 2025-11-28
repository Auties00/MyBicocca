package it.attendance100.mybicocca.data.datasources.auth

import it.attendance100.mybicocca.data.api.*
import retrofit2.*
import javax.inject.*

class RemoteAuthDataSource @Inject constructor(
  private val api: MyBicoccaApiService,
) : AuthDataSource {
  override suspend fun getAuthHeaders(code: String, state: String, cookie: String): Response<Unit> {
    return api.authCallback(code, state, cookie)
  }
}