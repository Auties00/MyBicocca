package it.attendance100.mybicocca.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.data.remote.api.bicoccapp.BicoccappApi
import it.attendance100.mybicocca.data.remote.api.bicoccapp.BicoccappAuthApi
import it.attendance100.mybicocca.data.remote.api.bicoccapp.BicoccappCalendarApi
import it.attendance100.mybicocca.data.remote.api.bicoccapp.BicoccappCampusApi
import it.attendance100.mybicocca.data.remote.api.bicoccapp.BicoccappMessagesApi
import it.attendance100.mybicocca.data.remote.api.bicoccapp.BicoccappUserApi
import it.attendance100.mybicocca.data.remote.api.bicoccapp.BicoccappWizardApi
import it.attendance100.mybicocca.util.PreferencesManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * # BicoccApp API Module
 *
 * Dagger Hilt module that provides the BicoccApp REST API dependencies.
 *
 * ## Provided Dependencies
 *
 * - [OkHttpClient]: HTTP client with logging and timeout configuration
 * - [Retrofit]: Retrofit instance configured for BicoccApp API
 * - [BicoccappApi]: Unified API access point with all sub-APIs
 *
 * ## Configuration
 *
 * The module configures:
 * - Base URL for the BicoccApp backend
 * - Request/response logging (debug builds)
 * - Connection and read timeouts
 * - Gson serialization for JSON
 *
 * ## Usage
 *
 * Simply inject [BicoccappApi] in your classes:
 *
 * ```kotlin
 * class MyRepository @Inject constructor(
 *     private val api: BicoccappApi
 * ) {
 *     suspend fun fetchProfile() = api.user.getProfile()
 * }
 * ```
 */
@Module
@InstallIn(SingletonComponent::class)
object BicoccappApiModule {

    /**
     * Base URL for the BicoccApp API.
     */
    private const val BASE_URL = "https://backoffice-app.unimib.it/api/v1"

    /**
     * Connection timeout in seconds.
     */
    private const val CONNECT_TIMEOUT = 30L

    /**
     * Read timeout in seconds.
     */
    private const val READ_TIMEOUT = 30L

    /**
     * Write timeout in seconds.
     */
    private const val WRITE_TIMEOUT = 30L

    /**
     * Access token header.
     */
    private const val HEADER_ACCESS_TOKEN = "access-token"

    /**
     * Client header.
     */
    private const val HEADER_CLIENT = "client"

    /**
     * Uid header.
     */
    private const val HEADER_UID = "uid"


    /**
     * Provides a configured [OkHttpClient] for BicoccApp API requests.
     *
     * Configuration includes:
     * - Authentication interceptor for adding auth headers
     * - HTTP logging interceptor for debugging
     * - Connection, read, and write timeouts
     */
    @Provides
    @Singleton
    @Esse3OkHttpClient
    fun provideOkHttpClient(preferencesManager: PreferencesManager): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                if (!preferencesManager.isLoggedIn()) {
                    chain.proceed(originalRequest)
                } else if (originalRequest.header(HEADER_ACCESS_TOKEN) != null) {
                    chain.proceed(originalRequest)
                } else {
                    val authenticatedRequest = originalRequest.newBuilder().apply {
                        preferencesManager.authAccessToken?.let { header(HEADER_ACCESS_TOKEN, it) }
                        preferencesManager.authClient?.let { header(HEADER_CLIENT, it) }
                        preferencesManager.authUid?.let { header(HEADER_UID, it) }
                    }.build()
                    chain.proceed(authenticatedRequest)
                }
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides the Retrofit instance configured for BicoccApp API.
     *
     * Uses Gson for JSON serialization and the provided [OkHttpClient]
     * for HTTP operations.
     */
    @Provides
    @Singleton
    @BicoccappRetrofit
    fun provideRetrofit(@Esse3OkHttpClient okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provides the [BicoccappAuthApi] implementation.
     */
    @Provides
    @Singleton
    fun provideAuthApi(@BicoccappRetrofit retrofit: Retrofit): BicoccappAuthApi {
        return retrofit.create(BicoccappAuthApi::class.java)
    }

    /**
     * Provides the [BicoccappUserApi] implementation.
     */
    @Provides
    @Singleton
    fun provideUserApi(@BicoccappRetrofit retrofit: Retrofit): BicoccappUserApi {
        return retrofit.create(BicoccappUserApi::class.java)
    }

    /**
     * Provides the [BicoccappCalendarApi] implementation.
     */
    @Provides
    @Singleton
    fun provideCalendarApi(@BicoccappRetrofit retrofit: Retrofit): BicoccappCalendarApi {
        return retrofit.create(BicoccappCalendarApi::class.java)
    }

    /**
     * Provides the [BicoccappWizardApi] implementation.
     */
    @Provides
    @Singleton
    fun provideWizardApi(@BicoccappRetrofit retrofit: Retrofit): BicoccappWizardApi {
        return retrofit.create(BicoccappWizardApi::class.java)
    }

    /**
     * Provides the [BicoccappMessagesApi] implementation.
     */
    @Provides
    @Singleton
    fun provideMessagesApi(@BicoccappRetrofit retrofit: Retrofit): BicoccappMessagesApi {
        return retrofit.create(BicoccappMessagesApi::class.java)
    }

    /**
     * Provides the [BicoccappCampusApi] implementation.
     */
    @Provides
    @Singleton
    fun provideCampusApi(@BicoccappRetrofit retrofit: Retrofit): BicoccappCampusApi {
        return retrofit.create(BicoccappCampusApi::class.java)
    }

    /**
     * Provides the unified [BicoccappApi] that aggregates all sub-APIs.
     *
     * This is the recommended dependency to inject when you need
     * access to multiple BicoccApp API endpoints.
     */
    @Provides
    @Singleton
    fun provideBicoccappApi(
        authApi: BicoccappAuthApi,
        userApi: BicoccappUserApi,
        calendarApi: BicoccappCalendarApi,
        wizardApi: BicoccappWizardApi,
        messagesApi: BicoccappMessagesApi,
        campusApi: BicoccappCampusApi
    ): BicoccappApi {
        return BicoccappApiImpl(
            auth = authApi,
            user = userApi,
            calendar = calendarApi,
            wizard = wizardApi,
            messages = messagesApi,
            campus = campusApi
        )
    }


    /**
     * Qualifier annotation for the BicoccApp Retrofit instance.
     */
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    private annotation class BicoccappRetrofit

    /**
     * Qualifier annotation for the BicoccApp OkHttpClient instance.
     */
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    private annotation class Esse3OkHttpClient

    /**
     * Default implementation of [BicoccappApi].
     *
     * Holds references to all Retrofit-generated API implementations.
     * Created by [it.attendance100.mybicocca.di.BicoccappApiModule].
     *
     * @property auth Authentication API implementation
     * @property user User API implementation
     * @property calendar Calendar API implementation
     * @property wizard Wizard API implementation
     * @property messages Messages API implementation
     * @property campus Campus API implementation
     */
    private class BicoccappApiImpl(
        override val auth: BicoccappAuthApi,
        override val user: BicoccappUserApi,
        override val calendar: BicoccappCalendarApi,
        override val wizard: BicoccappWizardApi,
        override val messages: BicoccappMessagesApi,
        override val campus: BicoccappCampusApi
    ) : BicoccappApi
}
