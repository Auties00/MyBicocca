package it.attendance100.mybicocca.data.api

import it.attendance100.mybicocca.data.dtos.*
import retrofit2.*
import retrofit2.http.*

interface MyBicoccaApiService {

  /**
   * Auth Callback.
   * The WebView intercepts the redirect to this URL, extracts code/state, and then we call this programmatically to get the headers
   */
  @GET("api/v1/auth/openid_connect/callback")
  suspend fun authCallback(
    @Query("code") code: String,
    @Query("state") state: String,
    @Header("Cookie") cookie: String,
  ): Response<Unit>


  // The AuthInterceptor will add the headers automatically to all the following requests

  /**
   * User Profile.
   */
  @GET("api/v1/user_profile")
  suspend fun getUserProfile(
    @Query("fiscalCode") fiscalCode: String?,
  ): UserProfileResponse

  /**
   * User Career.
   */
  @GET("api/v1/user_career")
  suspend fun getUserCareer(
    @Query("studentId") studentId: Int?,
    @Query("matricId") matricId: Int?,
    @Query("personId") personId: Int?,
    @Query("typeTitleCode") typeTitleCode: String?,
  ): UserCareerResponse

  /**
   * User Exams.
   */
  @GET("api/v1/user_exams")
  suspend fun getUserExams(
    @Query("matricId") matricId: Int?,
  ): UserExamsResponse
}