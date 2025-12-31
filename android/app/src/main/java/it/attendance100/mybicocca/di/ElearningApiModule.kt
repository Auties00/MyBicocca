package it.attendance100.mybicocca.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.data.api.elearning.ElearningApi
import it.attendance100.mybicocca.data.api.elearning.ElearningAuthEmailApi
import it.attendance100.mybicocca.data.api.elearning.ElearningBlockAccessreviewApi
import it.attendance100.mybicocca.data.api.elearning.ElearningBlockIomadCompanyAdminApi
import it.attendance100.mybicocca.data.api.elearning.ElearningBlockRecentlyaccesseditemsApi
import it.attendance100.mybicocca.data.api.elearning.ElearningBlockStarredcoursesApi
import it.attendance100.mybicocca.data.api.elearning.ElearningEnrolGuestApi
import it.attendance100.mybicocca.data.api.elearning.ElearningEnrolLicenseApi
import it.attendance100.mybicocca.data.api.elearning.ElearningEnrolManualApi
import it.attendance100.mybicocca.data.api.elearning.ElearningEnrolMetaApi
import it.attendance100.mybicocca.data.api.elearning.ElearningEnrolSelfApi
import it.attendance100.mybicocca.data.api.elearning.ElearningGradereportGraderApi
import it.attendance100.mybicocca.data.api.elearning.ElearningGradereportOverviewApi
import it.attendance100.mybicocca.data.api.elearning.ElearningGradereportSingleviewApi
import it.attendance100.mybicocca.data.api.elearning.ElearningGradereportUserApi
import it.attendance100.mybicocca.data.api.elearning.ElearningGradingformGuideApi
import it.attendance100.mybicocca.data.api.elearning.ElearningGradingformRubricApi
import it.attendance100.mybicocca.data.api.elearning.ElearningLocalIomadLearningpathApi
import it.attendance100.mybicocca.data.api.elearning.ElearningMediaVideojsApi
import it.attendance100.mybicocca.data.api.elearning.ElearningMessageAirnotifierApi
import it.attendance100.mybicocca.data.api.elearning.ElearningMessagePopupApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModAssignApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModBigbluebuttonbnApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModBookApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModChatApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModChoiceApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModDataApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModFeedbackApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModFolderApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModForumApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModGlossaryApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModH5pactivityApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModImscpApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModIomadcertificateApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModLabelApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModLessonApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModLtiApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModPageApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModQuizApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModResourceApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModScormApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModSurveyApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModUrlApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModWikiApi
import it.attendance100.mybicocca.data.api.elearning.ElearningModWorkshopApi
import it.attendance100.mybicocca.data.api.elearning.ElearningMoodleApi
import it.attendance100.mybicocca.data.api.elearning.ElearningPaygwPaypalApi
import it.attendance100.mybicocca.data.api.elearning.ElearningQbankColumnsortorderApi
import it.attendance100.mybicocca.data.api.elearning.ElearningQbankEditquestionApi
import it.attendance100.mybicocca.data.api.elearning.ElearningQbankTagquestionApi
import it.attendance100.mybicocca.data.api.elearning.ElearningQbankViewquestiontextApi
import it.attendance100.mybicocca.data.api.elearning.ElearningQuizaccessSebApi
import it.attendance100.mybicocca.data.api.elearning.ElearningReportCompetencyApi
import it.attendance100.mybicocca.data.api.elearning.ElearningReportInsightsApi
import it.attendance100.mybicocca.data.api.elearning.ElearningTinyAutosaveApi
import it.attendance100.mybicocca.data.api.elearning.ElearningTinyEquationApi
import it.attendance100.mybicocca.data.api.elearning.ElearningTinyPremiumApi
import it.attendance100.mybicocca.data.api.elearning.ElearningToolAnalyticsApi
import it.attendance100.mybicocca.data.api.elearning.ElearningToolBehatApi
import it.attendance100.mybicocca.data.api.elearning.ElearningToolDataprivacyApi
import it.attendance100.mybicocca.data.api.elearning.ElearningToolIomadpolicyApi
import it.attendance100.mybicocca.data.api.elearning.ElearningToolLpApi
import it.attendance100.mybicocca.data.api.elearning.ElearningToolMobileApi
import it.attendance100.mybicocca.data.api.elearning.ElearningToolMoodlenetApi
import it.attendance100.mybicocca.data.api.elearning.ElearningToolPolicyApi
import it.attendance100.mybicocca.data.api.elearning.ElearningToolTemplatelibraryApi
import it.attendance100.mybicocca.data.api.elearning.ElearningToolUsertoursApi
import it.attendance100.mybicocca.data.api.elearning.ElearningToolXmldbApi
import it.attendance100.mybicocca.manager.StorageManager
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
    fun provideOkHttpClient(storageManager: StorageManager): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                var request = chain.request()
                val token = storageManager.elearningToken

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


    @Provides
    @Singleton
    fun provideAuthEmailApi(@ElearningRetrofit retrofit: Retrofit): ElearningAuthEmailApi =
        retrofit.create(ElearningAuthEmailApi::class.java)


    @Provides
    @Singleton
    fun provideBlockAccessreviewApi(@ElearningRetrofit retrofit: Retrofit): ElearningBlockAccessreviewApi =
        retrofit.create(ElearningBlockAccessreviewApi::class.java)

    @Provides
    @Singleton
    fun provideBlockIomadCompanyAdminApi(@ElearningRetrofit retrofit: Retrofit): ElearningBlockIomadCompanyAdminApi =
        retrofit.create(ElearningBlockIomadCompanyAdminApi::class.java)

    @Provides
    @Singleton
    fun provideBlockRecentlyaccesseditemsApi(@ElearningRetrofit retrofit: Retrofit): ElearningBlockRecentlyaccesseditemsApi =
        retrofit.create(ElearningBlockRecentlyaccesseditemsApi::class.java)

    @Provides
    @Singleton
    fun provideBlockStarredcoursesApi(@ElearningRetrofit retrofit: Retrofit): ElearningBlockStarredcoursesApi =
        retrofit.create(ElearningBlockStarredcoursesApi::class.java)


    @Provides
    @Singleton
    fun provideEnrolGuestApi(@ElearningRetrofit retrofit: Retrofit): ElearningEnrolGuestApi =
        retrofit.create(ElearningEnrolGuestApi::class.java)

    @Provides
    @Singleton
    fun provideEnrolLicenseApi(@ElearningRetrofit retrofit: Retrofit): ElearningEnrolLicenseApi =
        retrofit.create(ElearningEnrolLicenseApi::class.java)

    @Provides
    @Singleton
    fun provideEnrolManualApi(@ElearningRetrofit retrofit: Retrofit): ElearningEnrolManualApi =
        retrofit.create(ElearningEnrolManualApi::class.java)

    @Provides
    @Singleton
    fun provideEnrolMetaApi(@ElearningRetrofit retrofit: Retrofit): ElearningEnrolMetaApi =
        retrofit.create(ElearningEnrolMetaApi::class.java)

    @Provides
    @Singleton
    fun provideEnrolSelfApi(@ElearningRetrofit retrofit: Retrofit): ElearningEnrolSelfApi =
        retrofit.create(ElearningEnrolSelfApi::class.java)


    @Provides
    @Singleton
    fun provideGradereportGraderApi(@ElearningRetrofit retrofit: Retrofit): ElearningGradereportGraderApi =
        retrofit.create(ElearningGradereportGraderApi::class.java)

    @Provides
    @Singleton
    fun provideGradereportOverviewApi(@ElearningRetrofit retrofit: Retrofit): ElearningGradereportOverviewApi =
        retrofit.create(ElearningGradereportOverviewApi::class.java)

    @Provides
    @Singleton
    fun provideGradereportSingleviewApi(@ElearningRetrofit retrofit: Retrofit): ElearningGradereportSingleviewApi =
        retrofit.create(ElearningGradereportSingleviewApi::class.java)

    @Provides
    @Singleton
    fun provideGradereportUserApi(@ElearningRetrofit retrofit: Retrofit): ElearningGradereportUserApi =
        retrofit.create(ElearningGradereportUserApi::class.java)


    @Provides
    @Singleton
    fun provideGradingformGuideApi(@ElearningRetrofit retrofit: Retrofit): ElearningGradingformGuideApi =
        retrofit.create(ElearningGradingformGuideApi::class.java)

    @Provides
    @Singleton
    fun provideGradingformRubricApi(@ElearningRetrofit retrofit: Retrofit): ElearningGradingformRubricApi =
        retrofit.create(ElearningGradingformRubricApi::class.java)


    @Provides
    @Singleton
    fun provideLocalIomadLearningpathApi(@ElearningRetrofit retrofit: Retrofit): ElearningLocalIomadLearningpathApi =
        retrofit.create(ElearningLocalIomadLearningpathApi::class.java)


    @Provides
    @Singleton
    fun provideMediaVideojsApi(@ElearningRetrofit retrofit: Retrofit): ElearningMediaVideojsApi =
        retrofit.create(ElearningMediaVideojsApi::class.java)


    @Provides
    @Singleton
    fun provideMessageAirnotifierApi(@ElearningRetrofit retrofit: Retrofit): ElearningMessageAirnotifierApi =
        retrofit.create(ElearningMessageAirnotifierApi::class.java)

    @Provides
    @Singleton
    fun provideMessagePopupApi(@ElearningRetrofit retrofit: Retrofit): ElearningMessagePopupApi =
        retrofit.create(ElearningMessagePopupApi::class.java)


    @Provides
    @Singleton
    fun provideModAssignApi(@ElearningRetrofit retrofit: Retrofit): ElearningModAssignApi =
        retrofit.create(ElearningModAssignApi::class.java)

    @Provides
    @Singleton
    fun provideModBigbluebuttonbnApi(@ElearningRetrofit retrofit: Retrofit): ElearningModBigbluebuttonbnApi =
        retrofit.create(ElearningModBigbluebuttonbnApi::class.java)

    @Provides
    @Singleton
    fun provideModBookApi(@ElearningRetrofit retrofit: Retrofit): ElearningModBookApi =
        retrofit.create(ElearningModBookApi::class.java)

    @Provides
    @Singleton
    fun provideModChatApi(@ElearningRetrofit retrofit: Retrofit): ElearningModChatApi =
        retrofit.create(ElearningModChatApi::class.java)

    @Provides
    @Singleton
    fun provideModChoiceApi(@ElearningRetrofit retrofit: Retrofit): ElearningModChoiceApi =
        retrofit.create(ElearningModChoiceApi::class.java)

    @Provides
    @Singleton
    fun provideModDataApi(@ElearningRetrofit retrofit: Retrofit): ElearningModDataApi =
        retrofit.create(ElearningModDataApi::class.java)

    @Provides
    @Singleton
    fun provideModFeedbackApi(@ElearningRetrofit retrofit: Retrofit): ElearningModFeedbackApi =
        retrofit.create(ElearningModFeedbackApi::class.java)

    @Provides
    @Singleton
    fun provideModFolderApi(@ElearningRetrofit retrofit: Retrofit): ElearningModFolderApi =
        retrofit.create(ElearningModFolderApi::class.java)

    @Provides
    @Singleton
    fun provideModForumApi(@ElearningRetrofit retrofit: Retrofit): ElearningModForumApi =
        retrofit.create(ElearningModForumApi::class.java)

    @Provides
    @Singleton
    fun provideModGlossaryApi(@ElearningRetrofit retrofit: Retrofit): ElearningModGlossaryApi =
        retrofit.create(ElearningModGlossaryApi::class.java)

    @Provides
    @Singleton
    fun provideModH5pactivityApi(@ElearningRetrofit retrofit: Retrofit): ElearningModH5pactivityApi =
        retrofit.create(ElearningModH5pactivityApi::class.java)

    @Provides
    @Singleton
    fun provideModImscpApi(@ElearningRetrofit retrofit: Retrofit): ElearningModImscpApi =
        retrofit.create(ElearningModImscpApi::class.java)

    @Provides
    @Singleton
    fun provideModIomadcertificateApi(@ElearningRetrofit retrofit: Retrofit): ElearningModIomadcertificateApi =
        retrofit.create(ElearningModIomadcertificateApi::class.java)

    @Provides
    @Singleton
    fun provideModLabelApi(@ElearningRetrofit retrofit: Retrofit): ElearningModLabelApi =
        retrofit.create(ElearningModLabelApi::class.java)

    @Provides
    @Singleton
    fun provideModLessonApi(@ElearningRetrofit retrofit: Retrofit): ElearningModLessonApi =
        retrofit.create(ElearningModLessonApi::class.java)

    @Provides
    @Singleton
    fun provideModLtiApi(@ElearningRetrofit retrofit: Retrofit): ElearningModLtiApi =
        retrofit.create(ElearningModLtiApi::class.java)

    @Provides
    @Singleton
    fun provideModPageApi(@ElearningRetrofit retrofit: Retrofit): ElearningModPageApi =
        retrofit.create(ElearningModPageApi::class.java)

    @Provides
    @Singleton
    fun provideModQuizApi(@ElearningRetrofit retrofit: Retrofit): ElearningModQuizApi =
        retrofit.create(ElearningModQuizApi::class.java)

    @Provides
    @Singleton
    fun provideModResourceApi(@ElearningRetrofit retrofit: Retrofit): ElearningModResourceApi =
        retrofit.create(ElearningModResourceApi::class.java)

    @Provides
    @Singleton
    fun provideModScormApi(@ElearningRetrofit retrofit: Retrofit): ElearningModScormApi =
        retrofit.create(ElearningModScormApi::class.java)

    @Provides
    @Singleton
    fun provideModSurveyApi(@ElearningRetrofit retrofit: Retrofit): ElearningModSurveyApi =
        retrofit.create(ElearningModSurveyApi::class.java)

    @Provides
    @Singleton
    fun provideModUrlApi(@ElearningRetrofit retrofit: Retrofit): ElearningModUrlApi =
        retrofit.create(ElearningModUrlApi::class.java)

    @Provides
    @Singleton
    fun provideModWikiApi(@ElearningRetrofit retrofit: Retrofit): ElearningModWikiApi =
        retrofit.create(ElearningModWikiApi::class.java)

    @Provides
    @Singleton
    fun provideModWorkshopApi(@ElearningRetrofit retrofit: Retrofit): ElearningModWorkshopApi =
        retrofit.create(ElearningModWorkshopApi::class.java)


    @Provides
    @Singleton
    fun provideMoodleApi(@ElearningRetrofit retrofit: Retrofit): ElearningMoodleApi =
        retrofit.create(ElearningMoodleApi::class.java)


    @Provides
    @Singleton
    fun providePaygwPaypalApi(@ElearningRetrofit retrofit: Retrofit): ElearningPaygwPaypalApi =
        retrofit.create(ElearningPaygwPaypalApi::class.java)


    @Provides
    @Singleton
    fun provideQbankColumnsortorderApi(@ElearningRetrofit retrofit: Retrofit): ElearningQbankColumnsortorderApi =
        retrofit.create(ElearningQbankColumnsortorderApi::class.java)

    @Provides
    @Singleton
    fun provideQbankEditquestionApi(@ElearningRetrofit retrofit: Retrofit): ElearningQbankEditquestionApi =
        retrofit.create(ElearningQbankEditquestionApi::class.java)

    @Provides
    @Singleton
    fun provideQbankTagquestionApi(@ElearningRetrofit retrofit: Retrofit): ElearningQbankTagquestionApi =
        retrofit.create(ElearningQbankTagquestionApi::class.java)

    @Provides
    @Singleton
    fun provideQbankViewquestiontextApi(@ElearningRetrofit retrofit: Retrofit): ElearningQbankViewquestiontextApi =
        retrofit.create(ElearningQbankViewquestiontextApi::class.java)


    @Provides
    @Singleton
    fun provideQuizaccessSebApi(@ElearningRetrofit retrofit: Retrofit): ElearningQuizaccessSebApi =
        retrofit.create(ElearningQuizaccessSebApi::class.java)


    @Provides
    @Singleton
    fun provideReportCompetencyApi(@ElearningRetrofit retrofit: Retrofit): ElearningReportCompetencyApi =
        retrofit.create(ElearningReportCompetencyApi::class.java)

    @Provides
    @Singleton
    fun provideReportInsightsApi(@ElearningRetrofit retrofit: Retrofit): ElearningReportInsightsApi =
        retrofit.create(ElearningReportInsightsApi::class.java)


    @Provides
    @Singleton
    fun provideTinyAutosaveApi(@ElearningRetrofit retrofit: Retrofit): ElearningTinyAutosaveApi =
        retrofit.create(ElearningTinyAutosaveApi::class.java)

    @Provides
    @Singleton
    fun provideTinyEquationApi(@ElearningRetrofit retrofit: Retrofit): ElearningTinyEquationApi =
        retrofit.create(ElearningTinyEquationApi::class.java)

    @Provides
    @Singleton
    fun provideTinyPremiumApi(@ElearningRetrofit retrofit: Retrofit): ElearningTinyPremiumApi =
        retrofit.create(ElearningTinyPremiumApi::class.java)


    @Provides
    @Singleton
    fun provideToolAnalyticsApi(@ElearningRetrofit retrofit: Retrofit): ElearningToolAnalyticsApi =
        retrofit.create(ElearningToolAnalyticsApi::class.java)

    @Provides
    @Singleton
    fun provideToolBehatApi(@ElearningRetrofit retrofit: Retrofit): ElearningToolBehatApi =
        retrofit.create(ElearningToolBehatApi::class.java)

    @Provides
    @Singleton
    fun provideToolDataprivacyApi(@ElearningRetrofit retrofit: Retrofit): ElearningToolDataprivacyApi =
        retrofit.create(ElearningToolDataprivacyApi::class.java)

    @Provides
    @Singleton
    fun provideToolIomadpolicyApi(@ElearningRetrofit retrofit: Retrofit): ElearningToolIomadpolicyApi =
        retrofit.create(ElearningToolIomadpolicyApi::class.java)

    @Provides
    @Singleton
    fun provideToolLpApi(@ElearningRetrofit retrofit: Retrofit): ElearningToolLpApi =
        retrofit.create(ElearningToolLpApi::class.java)

    @Provides
    @Singleton
    fun provideToolMobileApi(@ElearningRetrofit retrofit: Retrofit): ElearningToolMobileApi =
        retrofit.create(ElearningToolMobileApi::class.java)

    @Provides
    @Singleton
    fun provideToolMoodlenetApi(@ElearningRetrofit retrofit: Retrofit): ElearningToolMoodlenetApi =
        retrofit.create(ElearningToolMoodlenetApi::class.java)

    @Provides
    @Singleton
    fun provideToolPolicyApi(@ElearningRetrofit retrofit: Retrofit): ElearningToolPolicyApi =
        retrofit.create(ElearningToolPolicyApi::class.java)

    @Provides
    @Singleton
    fun provideToolTemplatelibraryApi(@ElearningRetrofit retrofit: Retrofit): ElearningToolTemplatelibraryApi =
        retrofit.create(ElearningToolTemplatelibraryApi::class.java)

    @Provides
    @Singleton
    fun provideToolUsertoursApi(@ElearningRetrofit retrofit: Retrofit): ElearningToolUsertoursApi =
        retrofit.create(ElearningToolUsertoursApi::class.java)

    @Provides
    @Singleton
    fun provideToolXmldbApi(@ElearningRetrofit retrofit: Retrofit): ElearningToolXmldbApi =
        retrofit.create(ElearningToolXmldbApi::class.java)


    @Provides
    @Singleton
    fun provideElearningApi(
        authEmailApi: ElearningAuthEmailApi,
        blockAccessreviewApi: ElearningBlockAccessreviewApi,
        blockIomadCompanyAdminApi: ElearningBlockIomadCompanyAdminApi,
        blockRecentlyaccesseditemsApi: ElearningBlockRecentlyaccesseditemsApi,
        blockStarredcoursesApi: ElearningBlockStarredcoursesApi,
        enrolGuestApi: ElearningEnrolGuestApi,
        enrolLicenseApi: ElearningEnrolLicenseApi,
        enrolManualApi: ElearningEnrolManualApi,
        enrolMetaApi: ElearningEnrolMetaApi,
        enrolSelfApi: ElearningEnrolSelfApi,
        gradereportGraderApi: ElearningGradereportGraderApi,
        gradereportOverviewApi: ElearningGradereportOverviewApi,
        gradereportSingleviewApi: ElearningGradereportSingleviewApi,
        gradereportUserApi: ElearningGradereportUserApi,
        gradingformGuideApi: ElearningGradingformGuideApi,
        gradingformRubricApi: ElearningGradingformRubricApi,
        localIomadLearningpathApi: ElearningLocalIomadLearningpathApi,
        mediaVideojsApi: ElearningMediaVideojsApi,
        messageAirnotifierApi: ElearningMessageAirnotifierApi,
        messagePopupApi: ElearningMessagePopupApi,
        modAssignApi: ElearningModAssignApi,
        modBigbluebuttonbnApi: ElearningModBigbluebuttonbnApi,
        modBookApi: ElearningModBookApi,
        modChatApi: ElearningModChatApi,
        modChoiceApi: ElearningModChoiceApi,
        modDataApi: ElearningModDataApi,
        modFeedbackApi: ElearningModFeedbackApi,
        modFolderApi: ElearningModFolderApi,
        modForumApi: ElearningModForumApi,
        modGlossaryApi: ElearningModGlossaryApi,
        modH5pactivityApi: ElearningModH5pactivityApi,
        modImscpApi: ElearningModImscpApi,
        modIomadcertificateApi: ElearningModIomadcertificateApi,
        modLabelApi: ElearningModLabelApi,
        modLessonApi: ElearningModLessonApi,
        modLtiApi: ElearningModLtiApi,
        modPageApi: ElearningModPageApi,
        modQuizApi: ElearningModQuizApi,
        modResourceApi: ElearningModResourceApi,
        modScormApi: ElearningModScormApi,
        modSurveyApi: ElearningModSurveyApi,
        modUrlApi: ElearningModUrlApi,
        modWikiApi: ElearningModWikiApi,
        modWorkshopApi: ElearningModWorkshopApi,
        moodleApi: ElearningMoodleApi,
        paygwPaypalApi: ElearningPaygwPaypalApi,
        qbankColumnsortorderApi: ElearningQbankColumnsortorderApi,
        qbankEditquestionApi: ElearningQbankEditquestionApi,
        qbankTagquestionApi: ElearningQbankTagquestionApi,
        qbankViewquestiontextApi: ElearningQbankViewquestiontextApi,
        quizaccessSebApi: ElearningQuizaccessSebApi,
        reportCompetencyApi: ElearningReportCompetencyApi,
        reportInsightsApi: ElearningReportInsightsApi,
        tinyAutosaveApi: ElearningTinyAutosaveApi,
        tinyEquationApi: ElearningTinyEquationApi,
        tinyPremiumApi: ElearningTinyPremiumApi,
        toolAnalyticsApi: ElearningToolAnalyticsApi,
        toolBehatApi: ElearningToolBehatApi,
        toolDataprivacyApi: ElearningToolDataprivacyApi,
        toolIomadpolicyApi: ElearningToolIomadpolicyApi,
        toolLpApi: ElearningToolLpApi,
        toolMobileApi: ElearningToolMobileApi,
        toolMoodlenetApi: ElearningToolMoodlenetApi,
        toolPolicyApi: ElearningToolPolicyApi,
        toolTemplatelibraryApi: ElearningToolTemplatelibraryApi,
        toolUsertoursApi: ElearningToolUsertoursApi,
        toolXmldbApi: ElearningToolXmldbApi
    ): ElearningApi {
        return ElearningApiImpl(
            authEmail = authEmailApi,
            blockAccessreview = blockAccessreviewApi,
            blockIomadCompanyAdmin = blockIomadCompanyAdminApi,
            blockRecentlyaccesseditems = blockRecentlyaccesseditemsApi,
            blockStarredcourses = blockStarredcoursesApi,
            enrolGuest = enrolGuestApi,
            enrolLicense = enrolLicenseApi,
            enrolManual = enrolManualApi,
            enrolMeta = enrolMetaApi,
            enrolSelf = enrolSelfApi,
            gradereportGrader = gradereportGraderApi,
            gradereportOverview = gradereportOverviewApi,
            gradereportSingleview = gradereportSingleviewApi,
            gradereportUser = gradereportUserApi,
            gradingformGuide = gradingformGuideApi,
            gradingformRubric = gradingformRubricApi,
            localIomadLearningpath = localIomadLearningpathApi,
            mediaVideojs = mediaVideojsApi,
            messageAirnotifier = messageAirnotifierApi,
            messagePopup = messagePopupApi,
            modAssign = modAssignApi,
            modBigbluebuttonbn = modBigbluebuttonbnApi,
            modBook = modBookApi,
            modChat = modChatApi,
            modChoice = modChoiceApi,
            modData = modDataApi,
            modFeedback = modFeedbackApi,
            modFolder = modFolderApi,
            modForum = modForumApi,
            modGlossary = modGlossaryApi,
            modH5pactivity = modH5pactivityApi,
            modImscp = modImscpApi,
            modIomadcertificate = modIomadcertificateApi,
            modLabel = modLabelApi,
            modLesson = modLessonApi,
            modLti = modLtiApi,
            modPage = modPageApi,
            modQuiz = modQuizApi,
            modResource = modResourceApi,
            modScorm = modScormApi,
            modSurvey = modSurveyApi,
            modUrl = modUrlApi,
            modWiki = modWikiApi,
            modWorkshop = modWorkshopApi,
            moodle = moodleApi,
            paygwPaypal = paygwPaypalApi,
            qbankColumnsortorder = qbankColumnsortorderApi,
            qbankEditquestion = qbankEditquestionApi,
            qbankTagquestion = qbankTagquestionApi,
            qbankViewquestiontext = qbankViewquestiontextApi,
            quizaccessSeb = quizaccessSebApi,
            reportCompetency = reportCompetencyApi,
            reportInsights = reportInsightsApi,
            tinyAutosave = tinyAutosaveApi,
            tinyEquation = tinyEquationApi,
            tinyPremium = tinyPremiumApi,
            toolAnalytics = toolAnalyticsApi,
            toolBehat = toolBehatApi,
            toolDataprivacy = toolDataprivacyApi,
            toolIomadpolicy = toolIomadpolicyApi,
            toolLp = toolLpApi,
            toolMobile = toolMobileApi,
            toolMoodlenet = toolMoodlenetApi,
            toolPolicy = toolPolicyApi,
            toolTemplatelibrary = toolTemplatelibraryApi,
            toolUsertours = toolUsertoursApi,
            toolXmldb = toolXmldbApi
        )
    }

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    private annotation class ElearningRetrofit

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    private annotation class ElearningOkHttpClient

    private class ElearningApiImpl(
        override val authEmail: ElearningAuthEmailApi,
        override val blockAccessreview: ElearningBlockAccessreviewApi,
        override val blockIomadCompanyAdmin: ElearningBlockIomadCompanyAdminApi,
        override val blockRecentlyaccesseditems: ElearningBlockRecentlyaccesseditemsApi,
        override val blockStarredcourses: ElearningBlockStarredcoursesApi,
        override val enrolGuest: ElearningEnrolGuestApi,
        override val enrolLicense: ElearningEnrolLicenseApi,
        override val enrolManual: ElearningEnrolManualApi,
        override val enrolMeta: ElearningEnrolMetaApi,
        override val enrolSelf: ElearningEnrolSelfApi,
        override val gradereportGrader: ElearningGradereportGraderApi,
        override val gradereportOverview: ElearningGradereportOverviewApi,
        override val gradereportSingleview: ElearningGradereportSingleviewApi,
        override val gradereportUser: ElearningGradereportUserApi,
        override val gradingformGuide: ElearningGradingformGuideApi,
        override val gradingformRubric: ElearningGradingformRubricApi,
        override val localIomadLearningpath: ElearningLocalIomadLearningpathApi,
        override val mediaVideojs: ElearningMediaVideojsApi,
        override val messageAirnotifier: ElearningMessageAirnotifierApi,
        override val messagePopup: ElearningMessagePopupApi,
        override val modAssign: ElearningModAssignApi,
        override val modBigbluebuttonbn: ElearningModBigbluebuttonbnApi,
        override val modBook: ElearningModBookApi,
        override val modChat: ElearningModChatApi,
        override val modChoice: ElearningModChoiceApi,
        override val modData: ElearningModDataApi,
        override val modFeedback: ElearningModFeedbackApi,
        override val modFolder: ElearningModFolderApi,
        override val modForum: ElearningModForumApi,
        override val modGlossary: ElearningModGlossaryApi,
        override val modH5pactivity: ElearningModH5pactivityApi,
        override val modImscp: ElearningModImscpApi,
        override val modIomadcertificate: ElearningModIomadcertificateApi,
        override val modLabel: ElearningModLabelApi,
        override val modLesson: ElearningModLessonApi,
        override val modLti: ElearningModLtiApi,
        override val modPage: ElearningModPageApi,
        override val modQuiz: ElearningModQuizApi,
        override val modResource: ElearningModResourceApi,
        override val modScorm: ElearningModScormApi,
        override val modSurvey: ElearningModSurveyApi,
        override val modUrl: ElearningModUrlApi,
        override val modWiki: ElearningModWikiApi,
        override val modWorkshop: ElearningModWorkshopApi,
        override val moodle: ElearningMoodleApi,
        override val paygwPaypal: ElearningPaygwPaypalApi,
        override val qbankColumnsortorder: ElearningQbankColumnsortorderApi,
        override val qbankEditquestion: ElearningQbankEditquestionApi,
        override val qbankTagquestion: ElearningQbankTagquestionApi,
        override val qbankViewquestiontext: ElearningQbankViewquestiontextApi,
        override val quizaccessSeb: ElearningQuizaccessSebApi,
        override val reportCompetency: ElearningReportCompetencyApi,
        override val reportInsights: ElearningReportInsightsApi,
        override val tinyAutosave: ElearningTinyAutosaveApi,
        override val tinyEquation: ElearningTinyEquationApi,
        override val tinyPremium: ElearningTinyPremiumApi,
        override val toolAnalytics: ElearningToolAnalyticsApi,
        override val toolBehat: ElearningToolBehatApi,
        override val toolDataprivacy: ElearningToolDataprivacyApi,
        override val toolIomadpolicy: ElearningToolIomadpolicyApi,
        override val toolLp: ElearningToolLpApi,
        override val toolMobile: ElearningToolMobileApi,
        override val toolMoodlenet: ElearningToolMoodlenetApi,
        override val toolPolicy: ElearningToolPolicyApi,
        override val toolTemplatelibrary: ElearningToolTemplatelibraryApi,
        override val toolUsertours: ElearningToolUsertoursApi,
        override val toolXmldb: ElearningToolXmldbApi
    ) : ElearningApi
}
