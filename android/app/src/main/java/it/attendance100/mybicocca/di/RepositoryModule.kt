package it.attendance100.mybicocca.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.data.api.elearning.ElearningApi
import it.attendance100.mybicocca.data.repository.CalendarRepository
import it.attendance100.mybicocca.data.repository.ElearningRepository
import it.attendance100.mybicocca.data.repository.RegistryRepository
import it.attendance100.mybicocca.data.repository.UserRepository
import it.attendance100.mybicocca.manager.StorageManager
import javax.inject.Singleton
import it.attendance100.mybicocca.domain.repository.CalendarRepository as ICalendarRepository
import it.attendance100.mybicocca.domain.repository.ElearningRepository as IElearningRepository
import it.attendance100.mybicocca.domain.repository.RegistryRepository as IRegistryRepository
import it.attendance100.mybicocca.domain.repository.UserRepository as IUserRepository

/**
 * Provides repository instances
 * Supplies repository instances by injecting necessary dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    /**
     * Provides the CalendarRepository
     * Hilt will automatically inject DataSource and DAO
     */
    @Provides
    @Singleton
    fun provideCalendarRepository(
	    api: it.attendance100.mybicocca.data.api.bicoccapp.BicoccappApi,
	    database: AppDatabase,
        storageManager: StorageManager
    ): ICalendarRepository {
	    return CalendarRepository(api, database, storageManager)
    }

    /**
     * Provides the ElearningRepository
     * Hilt will automatically inject DataSource and DAO
     */
    @Provides
    @Singleton
    fun providesElearningRepository(
        api: ElearningApi,
        database: AppDatabase,
        storageManager: StorageManager,
    ): IElearningRepository {
	    return ElearningRepository(api, database, storageManager)
    }

    /**
     * Provides the RegistryRepository
     * Hilt will automatically inject DataSource and DAO
     */
    @Provides
    @Singleton
    fun providesRegistryRepository(
	    api: it.attendance100.mybicocca.data.api.esse3.Esse3Api,
	    database: AppDatabase,
    ): IRegistryRepository {
	    return RegistryRepository(api, database)
    }

    /**
     * Provides the UserRepository
     * Hilt will automatically inject DataSource and DAO
     */

    @Provides
    @Singleton
    fun providesUserRepository(
	    bicoccappApi: it.attendance100.mybicocca.data.api.bicoccapp.BicoccappApi,
	    esse3Api: it.attendance100.mybicocca.data.api.esse3.Esse3Api,
	    database: AppDatabase,
	    storageManager: StorageManager,
    ): IUserRepository {
	    return UserRepository(bicoccappApi, esse3Api, database, storageManager)
    }
}
