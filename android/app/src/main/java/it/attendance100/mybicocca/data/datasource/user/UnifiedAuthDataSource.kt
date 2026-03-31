package it.attendance100.mybicocca.data.datasource.user

import android.util.Log
import it.attendance100.mybicocca.data.api.elearning.ElearningApi
import it.attendance100.mybicocca.data.api.esse3.Esse3Api
import it.attendance100.mybicocca.data.datastore.AuthTokenStore
import it.attendance100.mybicocca.data.dto.elearning.ElearningLoginResponse
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnifiedAuthDataSource @Inject constructor(
    private val esse3Api: Esse3Api,
    private val elearningApi: ElearningApi,
    private val authTokenStore: AuthTokenStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun login(email: String, password: String) = withContext(ioDispatcher) {
        // Esse3 login
        val credentials = Base64.getEncoder().encodeToString("$email:$password".toByteArray())
        authTokenStore.esse3BasicAuth = credentials

        val esse3Session = try {
            esse3Api.auth.login()
        } catch (e: Exception) {
            authTokenStore.esse3BasicAuth = null
            throw e
        }
        authTokenStore.esse3PersonId = esse3Session.user.personId ?: -1L
        authTokenStore.esse3UserId = esse3Session.user.id

        // Elearning login
        when (val elearningResult = elearningApi.auth.login(email, password)) {
            is ElearningLoginResponse.Success -> {
                authTokenStore.elearningWsToken = elearningResult.wsToken
                val siteInfo = elearningApi.site.getSiteInfo(elearningResult.wsToken)
                authTokenStore.elearningUserId = siteInfo.userId
            }
            is ElearningLoginResponse.Error -> {
                // Elearning login failed, but Esse3 succeeded -> continue with partial auth
                Log.w("Auth Login", "Elearning login Failed! Continuing with partial auth...")
            }
        }
    }

    suspend fun logout() = withContext(ioDispatcher) {
        runCatching { esse3Api.auth.logout() }
        authTokenStore.clearAll()
    }

    fun isLoggedIn(): Boolean = authTokenStore.isLoggedIn
}
