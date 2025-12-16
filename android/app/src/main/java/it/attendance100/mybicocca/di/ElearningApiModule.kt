package it.attendance100.mybicocca.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.data.remote.api.elearning.ElearningApi
import it.attendance100.mybicocca.data.remote.api.elearning.ElearningAssignmentApi
import it.attendance100.mybicocca.data.remote.api.elearning.ElearningAuthApi
import it.attendance100.mybicocca.data.remote.api.elearning.ElearningCalendarApi
import it.attendance100.mybicocca.data.remote.api.elearning.ElearningCommonApi
import it.attendance100.mybicocca.data.remote.api.elearning.ElearningCourseApi
import it.attendance100.mybicocca.data.remote.api.elearning.ElearningForumApi
import it.attendance100.mybicocca.data.remote.api.elearning.ElearningMessageApi
import it.attendance100.mybicocca.data.remote.api.elearning.ElearningQuizApi
import it.attendance100.mybicocca.data.remote.api.elearning.ElearningUserApi
import it.attendance100.mybicocca.util.PreferencesManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * # Elearning API Module
 *
 * Dagger Hilt module that provides the Elearning (Moodle) REST API dependencies.
 *
 * ## Provided Dependencies
 *
 * - [OkHttpClient]: HTTP client with token injection and logging
 * - [Retrofit]: Retrofit instance configured for Elearning API
 * - [ElearningApi]: Unified API access point with all sub-APIs
 *
 * ## Configuration
 *
 * The module configures:
 * - Base URL for the Elearning backend
 * - Token injection via Query Parameter (wstoken)
 * - Request/response logging (debug builds)
 * - Connection and read timeouts
 * - Gson serialization for JSON
 */
@Module
@InstallIn(SingletonComponent::class)
object ElearningApiModule {

    /**
     * Base URL for the Elearning API.
     */
    private const val BASE_URL = "https://elearning.unimib.it/"

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
     * Web Service Token parameter name.
     */
    private const val PARAM_WSTOKEN = "wstoken"

    /**
     * Provides a configured [OkHttpClient] for Elearning API requests.
     *
     * Configuration includes:
     * - Interceptor to inject 'wstoken' query parameter if available
     * - HTTP logging interceptor for debugging
     * - Connection, read, and write timeouts
     */
    @Provides
    @Singleton
    @ElearningOkHttpClient
    fun provideOkHttpClient(preferencesManager: PreferencesManager): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                var request = chain.request()
                val token = preferencesManager.elearningToken

                if (!token.isNullOrBlank()) {
                    val url = request.url.newBuilder()
                        .addQueryParameter(PARAM_WSTOKEN, token)
                        .build()
                    request = request.newBuilder().url(url).build()
                }
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides the Retrofit instance configured for Elearning API.
     */
    @Provides
    @Singleton
    @ElearningRetrofit
    fun provideRetrofit(@ElearningOkHttpClient okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provides the [ElearningAuthApi] implementation.
     */
    @Provides
    @Singleton
    fun provideAuthApi(@ElearningRetrofit retrofit: Retrofit): ElearningAuthApi {
        return retrofit.create(ElearningAuthApi::class.java)
    }

    /**
     * Provides the [ElearningUserApi] implementation.
     */
    @Provides
    @Singleton
    fun provideUserApi(@ElearningRetrofit retrofit: Retrofit): ElearningUserApi {
        return retrofit.create(ElearningUserApi::class.java)
    }

    /**
     * Provides the [ElearningCourseApi] implementation.
     */
    @Provides
    @Singleton
    fun provideCourseApi(@ElearningRetrofit retrofit: Retrofit): ElearningCourseApi {
        return retrofit.create(ElearningCourseApi::class.java)
    }

    /**
     * Provides the [ElearningCalendarApi] implementation.
     */
    @Provides
    @Singleton
    fun provideCalendarApi(@ElearningRetrofit retrofit: Retrofit): ElearningCalendarApi {
        return retrofit.create(ElearningCalendarApi::class.java)
    }

    /**
     * Provides the [ElearningMessageApi] implementation.
     */
    @Provides
    @Singleton
    fun provideMessageApi(@ElearningRetrofit retrofit: Retrofit): ElearningMessageApi {
        return retrofit.create(ElearningMessageApi::class.java)
    }

    /**
     * Provides the [ElearningAssignmentApi] implementation.
     */
    @Provides
    @Singleton
    fun provideAssignmentApi(@ElearningRetrofit retrofit: Retrofit): ElearningAssignmentApi {
        return retrofit.create(ElearningAssignmentApi::class.java)
    }

    /**
     * Provides the [ElearningForumApi] implementation.
     */
    @Provides
    @Singleton
    fun provideForumApi(@ElearningRetrofit retrofit: Retrofit): ElearningForumApi {
        return retrofit.create(ElearningForumApi::class.java)
    }

    /**
     * Provides the [ElearningQuizApi] implementation.
     */
    @Provides
    @Singleton
    fun provideQuizApi(@ElearningRetrofit retrofit: Retrofit): ElearningQuizApi {
        return retrofit.create(ElearningQuizApi::class.java)
    }

    /**
     * Provides the [ElearningCommonApi] implementation.
     */
    @Provides
    @Singleton
    fun provideCommonApi(@ElearningRetrofit retrofit: Retrofit): ElearningCommonApi {
        return retrofit.create(ElearningCommonApi::class.java)
    }

    /**
     * Provides the unified [ElearningApi] that aggregates all sub-APIs.
     */
    @Provides
    @Singleton
    fun provideElearningApi(
        authApi: ElearningAuthApi,
        userApi: ElearningUserApi,
        courseApi: ElearningCourseApi,
        calendarApi: ElearningCalendarApi,
        messageApi: ElearningMessageApi,
        assignmentApi: ElearningAssignmentApi,
        forumApi: ElearningForumApi,
        quizApi: ElearningQuizApi,
        commonApi: ElearningCommonApi
    ): ElearningApi {
        return ElearningApiImpl(
            auth = authApi,
            user = userApi,
            course = courseApi,
            calendar = calendarApi,
            message = messageApi,
            assignment = assignmentApi,
            forum = forumApi,
            quiz = quizApi,
            common = commonApi
        )
    }

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    private annotation class ElearningRetrofit

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    private annotation class ElearningOkHttpClient

    private class ElearningApiImpl(
        override val auth: ElearningAuthApi,
        override val user: ElearningUserApi,
        override val course: ElearningCourseApi,
        override val calendar: ElearningCalendarApi,
        override val message: ElearningMessageApi,
        override val assignment: ElearningAssignmentApi,
        override val forum: ElearningForumApi,
        override val quiz: ElearningQuizApi,
        override val common: ElearningCommonApi
    ) : ElearningApi
}