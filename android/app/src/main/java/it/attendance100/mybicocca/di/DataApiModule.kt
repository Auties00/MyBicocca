package it.attendance100.mybicocca.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import it.attendance100.mybicocca.data.api.easystaff.EasyStaffApi
import it.attendance100.mybicocca.data.api.elearning.ElearningApi
import it.attendance100.mybicocca.data.api.esse3.Esse3Api
import it.attendance100.mybicocca.data.datastore.AuthTokenStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataApiModule {
    @Provides
    @Singleton
    fun provideEsse3Api(authTokenStore: AuthTokenStore): Esse3Api = Esse3Api {
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
    fun provideElearningApi(): ElearningApi = ElearningApi()
}
