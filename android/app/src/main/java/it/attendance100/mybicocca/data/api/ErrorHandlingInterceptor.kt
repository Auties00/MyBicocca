package it.attendance100.mybicocca.data.api

import android.util.*
import okhttp3.*
import org.json.*
import java.io.*
import javax.inject.*

class ErrorHandlingInterceptor @Inject constructor() : Interceptor {

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val response = chain.proceed(request)

    if (response.isSuccessful) {
      try {
        // Peek a reasonable amount of data (e.g., 1MB) to check for errors without consuming the stream
        val peekedBody = response.peekBody(1024 * 1024)
        val jsonString = peekedBody.string()

        if (jsonString.isNotEmpty()) {
          // Check if it looks like JSON before parsing to avoid unnecessary exceptions
          val trimmed = jsonString.trim()
          if (trimmed.startsWith("{")) {
            val jsonObject = JSONObject(jsonString)
            // Check if `status` exists and is an integer
            if (jsonObject.has("status") && !jsonObject.isNull("status")) {
              val statusOpt = jsonObject.opt("status")
              if (statusOpt is Int) {
                if (statusOpt >= 400) {
                  val message = jsonObject.optString("message", "Unknown Error")
                  val code = jsonObject.optString("code", "")

                  Log.w("ErrorInterceptor", "Detected error in 200 OK response: status=$statusOpt, code=$code")

                  // Check for JWT expired or generic 500 that means auth failure
                  if (statusOpt == 500 && (message.contains("JWT expired", ignoreCase = true) || code == "eS3-813")) {
                    Log.e("ErrorInterceptor", "Throwing ExpiredJWTApiException")
                    throw ExpiredJWTApiException(statusOpt, message)
                  }
                }
              }
            }
          }
        }
      } catch (e: Exception) {
        // If we just threw ExpiredJWTApiException, rethrow it
        if (e is ExpiredJWTApiException) throw e

        // Otherwise, ignore parsing errors and let Retrofit handle the body
        // Log.e("ErrorInterceptor", "Failed to parse error body", e)
      }
    }

    return response
  }
}
