package it.attendance100.mybicocca.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import it.attendance100.mybicocca.data.api.easystaff.EasyStaffApi
import it.attendance100.mybicocca.data.api.elearning.ElearningApi
import it.attendance100.mybicocca.data.api.esse3.Esse3Api
import it.attendance100.mybicocca.data.datastore.AuthTokenStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataApiModule {
    private const val REQUEST_TIMEOUT_MS = 15_000L

    @Provides
    @Singleton
    fun provideEsse3Api(authTokenStore: AuthTokenStore): Esse3Api = Esse3Api {
        applyDefaultTimeouts()
        defaultRequest {
            authTokenStore.esse3BasicAuth?.let {
                header("Authorization", "Basic $it")
            }
        }
    }

    @Provides
    @Singleton
    fun provideEasyStaffApi(): EasyStaffApi = EasyStaffApi()

    @Provides
    @Singleton
    fun provideElearningApi(): ElearningApi = ElearningApi {
        applyDefaultTimeouts()
    }

    private fun HttpClientConfig<*>.applyDefaultTimeouts() {
        install(HttpTimeout) {
            requestTimeoutMillis = REQUEST_TIMEOUT_MS
            connectTimeoutMillis = REQUEST_TIMEOUT_MS
            socketTimeoutMillis = REQUEST_TIMEOUT_MS
        }
    }
}
