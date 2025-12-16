package it.attendance100.mybicocca.data.repository

import android.util.Log
import androidx.core.net.toUri
import it.attendance100.mybicocca.domain.datasource.AuthDataSource
import it.attendance100.mybicocca.util.PreferencesManager
import javax.inject.Inject
import it.attendance100.mybicocca.domain.repository.AuthRepository as IAuthRepository

class AuthRepository @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val preferencesManager: PreferencesManager,
) : IAuthRepository {

    override suspend fun performLoginCallback(
        code: String,
        state: String,
        cookie: String
    ): Boolean {
        Log.v("AuthRepository", "Performing login callback with code: $code")
        try {
            val response = authDataSource.getAuthHeaders(code, state, cookie)

            // Get the redirect location
            val location = response.headers()["Location"] ?: ""
            Log.v("AuthRepository", "Redirect Location: $location")

            if (location.isBlank()) {
                Log.e("AuthRepository", "Login failed: Empty redirect location")
                return false
            }

            // Parse URL parameters
            val uri = location.toUri()

            // Checking both "access_token" and "access-token" just in case
            val accessToken =
                uri.getQueryParameter("access_token") ?: uri.getQueryParameter("access-token")
            val client = uri.getQueryParameter("client")
            val uid = uri.getQueryParameter("uid")
            val fiscalCode = uri.getQueryParameter("fiscal_code")

            if (uid != null && client != null && accessToken != null) {
                Log.d("AuthRepository", "Login successful. Saving credentials for user: $uid")
                preferencesManager.authUid = uid
                preferencesManager.authClient = client
                preferencesManager.authAccessToken = accessToken
                preferencesManager.authFiscalCode = fiscalCode
                return true
            }

            Log.e("AuthRepository", "Login failed: Missing tokens in redirect URL")
            return false
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login exception", e)
            e.printStackTrace()
            return false
        }
    }

    override fun isUserLoggedIn(): Boolean {
        val loggedIn = preferencesManager.isLoggedIn()
        Log.d("AuthRepository", "Checking login status: $loggedIn")
        return loggedIn
    }

    override fun logout() {
        Log.d("AuthRepository", "Logging out user")
        preferencesManager.clearAuth()
    }
}