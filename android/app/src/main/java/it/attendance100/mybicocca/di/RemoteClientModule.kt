package it.attendance100.mybicocca.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.plugins.HttpTimeout
import it.attendance100.mybicocca.data.observability.HttpMetrics
import it.attendance100.mybicocca.data.remote.affluences.api.AffluencesApi
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffApi
import it.attendance100.mybicocca.data.update.GithubReleaseApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Provides the application-lifetime coroutine scope and the unauthenticated HTTP clients —
 * the APIs that need no signed-in session (EasyStaff and Affluences) — each with HTTP
 * performance metrics installed. Account-bound clients (Esse3, Moodle) are not provided here:
 * they are built per session by the factories in the auth layer.
 */
@Module
@InstallIn(SingletonComponent::class)
object RemoteClientModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideAppScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideEasyStaffApi(): EasyStaffApi = EasyStaffApi {
        install(HttpTimeout) {
            connectTimeoutMillis = HTTP_TIMEOUT_MS
            socketTimeoutMillis = HTTP_TIMEOUT_MS
            requestTimeoutMillis = HTTP_TIMEOUT_MS
        }
        install(HttpMetrics)
    }

    /** The browser-like User-Agent the Affluences API requires is set inside [AffluencesApi] itself. */
    @Provides
    @Singleton
    fun provideAffluencesApi(): AffluencesApi = AffluencesApi {
        install(HttpMetrics)
    }

    /**
     * The GitHub Releases client behind the app-update check. Unauthenticated;
     * the repo coordinates are fixed here, and timeout/headers/metrics live inside [GithubReleaseApi].
     */
    @Suppress("unused")
    @Provides
    @Singleton
    fun provideGithubReleaseApi(): GithubReleaseApi =
        GithubReleaseApi(owner = GITHUB_OWNER, repo = GITHUB_REPO)

    private const val HTTP_TIMEOUT_MS = 30_000L
    private const val GITHUB_OWNER = "Auties00"
    private const val GITHUB_REPO = "MyBicocca"
}
