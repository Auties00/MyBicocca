package it.attendance100.mybicocca.di

import dagger.*
import dagger.hilt.*
import dagger.hilt.components.*
import it.attendance100.mybicocca.data.api.*
import it.attendance100.mybicocca.utils.*
import okhttp3.*
import okhttp3.logging.*
import retrofit2.*
import retrofit2.converter.gson.*
import javax.inject.*

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

  private const val BASE_URL = "https://backoffice-app.unimib.it/"

  @Provides
  @Singleton
  @Named("AuthInterceptor")
  fun provideAuthInterceptor(preferencesManager: PreferencesManager): Interceptor {
    return Interceptor { chain ->
      val original = chain.request()
      val builder = original.newBuilder()

      // If we have credentials, attach them to every request
      if (preferencesManager.isLoggedIn()) {
        preferencesManager.authUid?.let { builder.addHeader("uid", it) }
        preferencesManager.authClient?.let { builder.addHeader("client", it) }
        preferencesManager.authAccessToken?.let { builder.addHeader("access-token", it) }
      }

      chain.proceed(builder.build())
    }
  }

  @Provides
  @Singleton
  @Named("TokenRefreshInterceptor")
  fun provideTokenRefreshInterceptor(preferencesManager: PreferencesManager): Interceptor {
    return Interceptor { chain ->
      val response = chain.proceed(chain.request())

      // Check if we should update tokens based on user settings
      val keepLoggedIn = preferencesManager.keepLoggedIn
      val sessionDuration = preferencesManager.sessionDuration
      val sessionStartTime = preferencesManager.sessionStartTime
      val currentTime = System.currentTimeMillis()

      val shouldUpdate = if (keepLoggedIn) {
        // If "Keep me logged in" is checked, we respect the session duration if set,
        // but usually this means "forever" or "until explicit logout".
        // However, if the user set a specific duration in settings, we should respect it.
        if (sessionDuration == PreferencesManager.DURATION_FOREVER) {
          true
        } else {
          // Check if we are still within the allowed duration
          currentTime - sessionStartTime < sessionDuration
        }
      } else {
        // If "Keep me logged in" is NOT checked, we assume the session is valid
        // as long as the app is running (or until a short default timeout).
        // But here we are in the interceptor, meaning the user is using the app.
        // So we should keep the session alive to prevent disruption.
        true
      }

      if (shouldUpdate) {
        val newAccessToken = response.header("access-token")
        val newClient = response.header("client")
        val newUid = response.header("uid")

        if (!newAccessToken.isNullOrBlank()) {
          preferencesManager.authAccessToken = newAccessToken
        }
        if (!newClient.isNullOrBlank()) {
          preferencesManager.authClient = newClient
        }
        if (!newUid.isNullOrBlank()) {
          preferencesManager.authUid = newUid
        }
      }

      response
    }
  }

  @Provides
  @Singleton
  fun provideOkHttpClient(
    @Named("AuthInterceptor") authInterceptor: Interceptor,
    @Named("TokenRefreshInterceptor") tokenRefreshInterceptor: Interceptor,
    errorHandlingInterceptor: ErrorHandlingInterceptor,
  ): OkHttpClient {
    val logger = HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BODY
    }

    return OkHttpClient.Builder()
        .addInterceptor(authInterceptor) // Adds auth headers
        .addInterceptor(tokenRefreshInterceptor) // Saves new auth headers
        .addInterceptor(errorHandlingInterceptor) // Checks for soft errors in body
        .addInterceptor(logger)
        .followRedirects(false) // Disable redirect following so we can catch headers from the 302 response of the callback if necessary
        .followSslRedirects(false)
        .build()
  }

  @Provides
  @Singleton
  fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
  }

  @Provides
  @Singleton
  fun provideApiService(retrofit: Retrofit): MyBicoccaApiService {
    return retrofit.create(MyBicoccaApiService::class.java)
  }
}