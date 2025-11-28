package it.attendance100.mybicocca.data.api

import retrofit2.*
import retrofit2.http.*

interface MyBicoccaApiService {

  /**
   * Auth Callback.
   * The WebView intercepts the redirect to this URL, extracts code/state, and then we call this programmatically to get the headers.
   */
  @GET("api/v1/auth/openid_connect/callback")
  suspend fun authCallback(
    @Query("code") code: String,
    @Query("state") state: String,
    @Header("Cookie") cookie: String,
  ): Response<Unit>

  /**
   * User Profile.
   * The AuthInterceptor will add the headers automatically.
   */
  @GET("api/v1/user_profile")
  suspend fun getUserProfile(
    @Query("fiscalCode") fiscalCode: String?,
  ): okhttp3.ResponseBody
}