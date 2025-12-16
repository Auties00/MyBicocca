@file:Suppress("RedundantSuppression", "RedundantSuppression")

package it.attendance100.mybicocca.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.data.local.dao.CourseEventDao
import it.attendance100.mybicocca.data.local.dao.CourseScheduleDao
import it.attendance100.mybicocca.data.local.dao.UserDao
import it.attendance100.mybicocca.data.repository.AuthRepository
import it.attendance100.mybicocca.data.repository.CalendarRepository
import it.attendance100.mybicocca.data.repository.UserRepository
import it.attendance100.mybicocca.domain.datasource.AuthDataSource
import it.attendance100.mybicocca.domain.datasource.CalendarDataSource
import it.attendance100.mybicocca.domain.datasource.UserDataSource
import it.attendance100.mybicocca.util.NetworkMonitor
import it.attendance100.mybicocca.util.PreferencesManager
import javax.inject.Singleton
import it.attendance100.mybicocca.domain.repository.AuthRepository as IAuthRepository
import it.attendance100.mybicocca.domain.repository.CalendarRepository as ICalendarRepository
import it.attendance100.mybicocca.domain.repository.UserRepository as IUserRepository

/**
 * Provides repository instances
 * Supplies repository instances by injecting necessary dependencies
 */
@Module
@Suppress(
    "unused",
    "RedundantSuppression",
    "RedundantSuppression",
    "RedundantSuppression",
    "RedundantSuppression",
    "RedundantSuppression"
)
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Provides the CalendarRepository
     * Hilt will automatically inject DataSource and DAO
     */
    @Provides
    @Singleton
    fun provideCalendarRepository(
        dataSource: CalendarDataSource,
        eventDao: CourseEventDao,
        scheduleDao: CourseScheduleDao,
    ): ICalendarRepository {
        return CalendarRepository(dataSource, eventDao, scheduleDao)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userDataSource: UserDataSource,
        userDao: UserDao,
        networkMonitor: NetworkMonitor,
    ): IUserRepository {
        return UserRepository(userDataSource, userDao, networkMonitor)
    }

    // Auth
    @Provides
    @Singleton
    fun provideAuthRepository(
        dataSource: AuthDataSource,
        preferencesManager: PreferencesManager,
    ): IAuthRepository {
        return AuthRepository(dataSource, preferencesManager)
    }
}
