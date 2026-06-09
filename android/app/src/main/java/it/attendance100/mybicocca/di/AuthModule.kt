package it.attendance100.mybicocca.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.plugins.HttpTimeout
import it.attendance100.mybicocca.data.observability.HttpMetrics
import it.attendance100.mybicocca.data.remote.affluences.api.AffluencesApi
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

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

    // The browser-like User-Agent the app API requires is set inside AffluencesApi itself.
    @Provides
    @Singleton
    fun provideAffluencesApi(): AffluencesApi = AffluencesApi {
        install(HttpMetrics)
    }

    private const val HTTP_TIMEOUT_MS = 30_000L
}
