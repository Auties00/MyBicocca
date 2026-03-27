package it.attendance100.mybicocca.data.datasource.user

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
    suspend fun login(username: String, password: String) = withContext(ioDispatcher) {
        // 1. Esse3 login — store Basic auth credentials so the HttpClient includes them
        val credentials = Base64.getEncoder().encodeToString("$username:$password".toByteArray())
        authTokenStore.esse3BasicAuth = credentials

        val esse3Session = try {
            esse3Api.auth.login()
        } catch (e: Exception) {
            authTokenStore.esse3BasicAuth = null
            throw e
        }
        authTokenStore.esse3PersonId = esse3Session.user.personId ?: -1L
        authTokenStore.esse3UserId = esse3Session.user.id

        // 2. Elearning login
        when (val elearningResult = elearningApi.auth.login(username, password)) {
            is ElearningLoginResponse.Success -> {
                authTokenStore.elearningWsToken = elearningResult.wsToken
                val siteInfo = elearningApi.site.getSiteInfo(elearningResult.wsToken)
                authTokenStore.elearningUserId = siteInfo.userId
            }
            is ElearningLoginResponse.Error -> {
                // Elearning login failed, but Esse3 succeeded — continue with partial auth
            }
        }
    }

    suspend fun logout() = withContext(ioDispatcher) {
        runCatching { esse3Api.auth.logout() }
        authTokenStore.clearAll()
    }

    fun isLoggedIn(): Boolean = authTokenStore.isLoggedIn
}
