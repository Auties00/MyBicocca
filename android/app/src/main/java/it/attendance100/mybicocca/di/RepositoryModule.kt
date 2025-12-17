package it.attendance100.mybicocca.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.data.repository.CalendarRepository
import it.attendance100.mybicocca.data.repository.ElearningRepository
import it.attendance100.mybicocca.data.repository.RegistryRepository
import it.attendance100.mybicocca.data.repository.UserRepository
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
    fun provideCalendarRepository(): ICalendarRepository {
        return CalendarRepository()
    }

    /**
     * Provides the ElearningRepository
     * Hilt will automatically inject DataSource and DAO
     */
    @Provides
    @Singleton
    fun providesElearningRepository(): IElearningRepository {
        return ElearningRepository()
    }

    /**
     * Provides the RegistryRepository
     * Hilt will automatically inject DataSource and DAO
     */
    @Provides
    @Singleton
    fun providesRegistryRepository(): IRegistryRepository {
        return RegistryRepository()
    }

    /**
     * Provides the UserRepository
     * Hilt will automatically inject DataSource and DAO
     */

    @Provides
    @Singleton
    fun providesUserRepository(): IUserRepository {
        return UserRepository()
    }
}
