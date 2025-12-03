package it.attendance100.mybicocca.data.api

import it.attendance100.mybicocca.utils.*
import okhttp3.*
import javax.inject.*

class TokenRefreshInterceptor @Inject constructor(
  private val preferencesManager: PreferencesManager,
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val response = chain.proceed(chain.request())

    // Check if we should update tokens based on user settings
    val keepLoggedIn = preferencesManager.keepLoggedIn
    val sessionDuration = preferencesManager.sessionDuration
    val sessionStartTime = preferencesManager.sessionStartTime
    val currentTime = System.currentTimeMillis()

    val shouldUpdate = if (keepLoggedIn) {
      // If "Keep me logged in" is checked, we respect the session duration if set
      if (sessionDuration == PreferencesManager.DURATION_FOREVER) {
        true
      } else {
        // Check if we are still within the allowed duration
        currentTime - sessionStartTime < sessionDuration
      }
    } else {
      // Even if "Keep me logged in" is not checked, session is valid as long as the app is running
      true
    }

    if (shouldUpdate) {
      val newAccessToken = response.header("access-token")
      val newClient = response.header("client")
      val newUid = response.header("uid")
      val expiry = response.header("expiry")

      if (!newAccessToken.isNullOrBlank()) preferencesManager.authAccessToken = newAccessToken
      if (!newClient.isNullOrBlank()) preferencesManager.authClient = newClient
      if (!newUid.isNullOrBlank()) preferencesManager.authUid = newUid

      // Update session start time for sliding window session
      preferencesManager.sessionStartTime = System.currentTimeMillis()

      if (!expiry.isNullOrBlank()) {
        try {
          val expiryTimeSeconds = expiry.toLong()
          val expiryTimeMillis = expiryTimeSeconds * 1000L
          preferencesManager.authExpiry = expiryTimeMillis

          android.util.Log.v("TokenRefresh", "Server token expiry updated: $expiryTimeMillis")
        } catch (_: Exception) {
          android.util.Log.w("TokenRefresh", "Failed to parse expiry header: $expiry")
        }
      }
    }

    return response
  }
}
