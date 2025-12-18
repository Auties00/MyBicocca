package it.attendance100.mybicocca.di

import dagger.*
import dagger.hilt.*
import dagger.hilt.components.*
import it.attendance100.mybicocca.data.repository.*
import javax.inject.*
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
	    database: it.attendance100.mybicocca.di.AppDatabase,
    ): ICalendarRepository {
	    return CalendarRepository(api, database)
    }

    /**
     * Provides the ElearningRepository
     * Hilt will automatically inject DataSource and DAO
     */
    @Provides
    @Singleton
    fun providesElearningRepository(
	    api: it.attendance100.mybicocca.data.api.elearning.ElearningApi,
	    database: it.attendance100.mybicocca.di.AppDatabase,
	    storageManager: it.attendance100.mybicocca.manager.StorageManager,
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
	    database: it.attendance100.mybicocca.di.AppDatabase,
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
	    database: it.attendance100.mybicocca.di.AppDatabase,
	    storageManager: it.attendance100.mybicocca.manager.StorageManager,
    ): IUserRepository {
	    return UserRepository(bicoccappApi, esse3Api, database, storageManager)
    }
}
