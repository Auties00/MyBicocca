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
  fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
    val logger = HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BODY
    }

    return OkHttpClient.Builder()
        .addInterceptor(authInterceptor) // Adds auth headers
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