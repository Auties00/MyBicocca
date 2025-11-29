package it.attendance100.mybicocca.data.repository

import android.net.*
import android.util.*
import it.attendance100.mybicocca.data.datasources.auth.*
import it.attendance100.mybicocca.utils.*
import javax.inject.*
import it.attendance100.mybicocca.domain.contracts.AuthRepository as IAuthRepository

class AuthRepository @Inject constructor(
  private val authDataSource: AuthDataSource,
  private val preferencesManager: PreferencesManager,
) : IAuthRepository {

  override suspend fun performLoginCallback(code: String, state: String, cookie: String): Boolean {
    try {
      val response = authDataSource.getAuthHeaders(code, state, cookie)

      // Get the redirect location
      val location = response.headers()["Location"] ?: ""
      Log.d("AuthRepository", "Redirect Location: $location")

      if (location.isBlank()) return false

      // Parse URL parameters
      val uri = Uri.parse(location)

      // Logs show "access_token" but headers usually use "access-token", so we'll check both just in case
      val accessToken = uri.getQueryParameter("access_token")
        ?: uri.getQueryParameter("access-token")

      val client = uri.getQueryParameter("client")
      val uid = uri.getQueryParameter("uid")
      val fiscalCode = uri.getQueryParameter("fiscal_code")

      if (uid != null && client != null && accessToken != null) {
        preferencesManager.authUid = uid
        preferencesManager.authClient = client
        preferencesManager.authAccessToken = accessToken
        preferencesManager.authFiscalCode = fiscalCode
        preferencesManager.sessionStartTime = System.currentTimeMillis()
        return true
      }

      return false
    } catch (e: Exception) {
      e.printStackTrace()
      return false
    }
  }

  override fun isUserLoggedIn(): Boolean = preferencesManager.isLoggedIn()

  override fun logout() {
    preferencesManager.clearAuth()
  }
}