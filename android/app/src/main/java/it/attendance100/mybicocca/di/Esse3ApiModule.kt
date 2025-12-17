package it.attendance100.mybicocca.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.data.api.esse3.*
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * # Esse3 API Module
 *
 * Dagger Hilt module that provides the Esse3 REST API dependencies.
 *
 * ## Provided Dependencies
 *
 * - [OkHttpClient]: HTTP client with cookie management and logging
 * - [Retrofit]: Retrofit instance configured for Esse3 API
 * - [Esse3Api]: Unified API access point with all sub-APIs
 *
 * ## Configuration
 *
 * The module configures:
 * - Base URL for the Esse3 backend
 * - Request/response logging (debug builds)
 * - Connection and read timeouts
 * - Cookie management (InMemory for session)
 * - ScalarsConverter (for HTML string responses)
 * - GsonConverter (for JSON responses)
 *
 * ## Usage
 *
 * Simply inject [Esse3Api] in your classes:
 *
 * ```kotlin
 * class MyRepository @Inject constructor(
 *     private val api: Esse3Api
 * ) {
 *     suspend fun fetchExams() = api.exams.getExamSessions()
 * }
 * ```
 */
@Module
@InstallIn(SingletonComponent::class)
object Esse3ApiModule {

    /**
     * Base URL for the Esse3 API.
     */
    private const val BASE_URL = "https://s3w.si.unimib.it/"

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
     * Provides a configured [CookieJar] for session management.
     *
     * Esse3 relies heavily on JSESSIONID cookies. This simple in-memory
     * implementation ensures the session is maintained across requests.
     */
    @Provides
    @Singleton
    fun provideCookieJar(): CookieJar {
        return object : CookieJar {
            private val cookieStore = HashMap<String, List<Cookie>>()

            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore[url.host] = cookies
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return cookieStore[url.host] ?: ArrayList()
            }
        }
    }

    /**
     * Provides a configured [OkHttpClient] for Esse3 API requests.
     *
     * Configuration includes:
     * - Cookie Jar for session management
     * - HTTP logging interceptor for debugging
     * - Connection, read, and write timeouts
     * - Redirect handling (essential for Esse3 flows)
     */
    @Provides
    @Singleton
    @Esse3OkHttpClient
    fun provideOkHttpClient(cookieJar: CookieJar): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    /**
     * Provides the Retrofit instance configured for Esse3 API.
     *
     * Uses ScalarsConverter for HTML responses and Gson for JSON serialization,
     * along with the provided [OkHttpClient].
     */
    @Provides
    @Singleton
    @Esse3Retrofit
    fun provideRetrofit(@Esse3OkHttpClient okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provides the [Esse3AuthApi] implementation.
     */
    @Provides
    @Singleton
    fun provideAuthApi(@Esse3Retrofit retrofit: Retrofit): Esse3AuthApi {
        return retrofit.create(Esse3AuthApi::class.java)
    }

    /**
     * Provides the [Esse3UserApi] implementation.
     */
    @Provides
    @Singleton
    fun provideUserApi(@Esse3Retrofit retrofit: Retrofit): Esse3UserApi {
        return retrofit.create(Esse3UserApi::class.java)
    }

    /**
     * Provides the [Esse3CareerApi] implementation.
     */
    @Provides
    @Singleton
    fun provideCareerApi(@Esse3Retrofit retrofit: Retrofit): Esse3CareerApi {
        return retrofit.create(Esse3CareerApi::class.java)
    }

    /**
     * Provides the [Esse3ExamsApi] implementation.
     */
    @Provides
    @Singleton
    fun provideExamsApi(@Esse3Retrofit retrofit: Retrofit): Esse3ExamsApi {
        return retrofit.create(Esse3ExamsApi::class.java)
    }

    /**
     * Provides the [Esse3InternshipApi] implementation.
     */
    @Provides
    @Singleton
    fun provideInternshipApi(@Esse3Retrofit retrofit: Retrofit): Esse3InternshipApi {
        return retrofit.create(Esse3InternshipApi::class.java)
    }

    /**
     * Provides the [Esse3AdmissionApi] implementation.
     */
    @Provides
    @Singleton
    fun provideAdmissionApi(@Esse3Retrofit retrofit: Retrofit): Esse3AdmissionApi {
        return retrofit.create(Esse3AdmissionApi::class.java)
    }

    /**
     * Provides the [Esse3QuestionnaireApi] implementation.
     */
    @Provides
    @Singleton
    fun provideQuestionnaireApi(@Esse3Retrofit retrofit: Retrofit): Esse3QuestionnaireApi {
        return retrofit.create(Esse3QuestionnaireApi::class.java)
    }

    /**
     * Provides the [Esse3CommonApi] implementation.
     */
    @Provides
    @Singleton
    fun provideCommonApi(@Esse3Retrofit retrofit: Retrofit): Esse3CommonApi {
        return retrofit.create(Esse3CommonApi::class.java)
    }

    /**
     * Provides the unified [Esse3Api] that aggregates all sub-APIs.
     *
     * This is the recommended dependency to inject when you need
     * access to multiple Esse3 API endpoints.
     */
    @Provides
    @Singleton
    fun provideEsse3Api(
        authApi: Esse3AuthApi,
        userApi: Esse3UserApi,
        careerApi: Esse3CareerApi,
        examsApi: Esse3ExamsApi,
        internshipApi: Esse3InternshipApi,
        admissionApi: Esse3AdmissionApi,
        questionnaireApi: Esse3QuestionnaireApi,
        commonApi: Esse3CommonApi
    ): Esse3Api {
        return Esse3ApiImpl(
            auth = authApi,
            user = userApi,
            career = careerApi,
            exams = examsApi,
            internship = internshipApi,
            admission = admissionApi,
            questionnaire = questionnaireApi,
            common = commonApi
        )
    }

    /**
     * Qualifier annotation for the Esse3 Retrofit instance.
     *
     * Use this to distinguish the Esse3 Retrofit from other
     * Retrofit instances (e.g., BicoccApp, Elearning).
     */
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    private annotation class Esse3Retrofit

    /**
     * Qualifier annotation for the Esse3 OkHttpClient instance.
     */
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    private annotation class Esse3OkHttpClient

    /**
     * Default implementation of [Esse3Api].
     *
     * Holds references to all Retrofit-generated API implementations.
     * Created by [it.attendance100.mybicocca.di.Esse3ApiModule].
     *
     * @property auth Authentication API implementation
     * @property user User API implementation
     * @property career Career API implementation
     * @property exams Exams API implementation
     * @property internship Internship API implementation
     * @property admission Admission API implementation
     * @property questionnaire Questionnaire API implementation
     * @property common Common API implementation
     */
    private class Esse3ApiImpl(
        override val auth: Esse3AuthApi,
        override val user: Esse3UserApi,
        override val career: Esse3CareerApi,
        override val exams: Esse3ExamsApi,
        override val internship: Esse3InternshipApi,
        override val admission: Esse3AdmissionApi,
        override val questionnaire: Esse3QuestionnaireApi,
        override val common: Esse3CommonApi
    ) : Esse3Api
}
