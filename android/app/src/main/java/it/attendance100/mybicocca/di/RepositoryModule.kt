package it.attendance100.mybicocca.di

import dagger.*
import dagger.hilt.*
import dagger.hilt.components.*
import it.attendance100.mybicocca.data.daos.*
import it.attendance100.mybicocca.data.datasources.auth.*
import it.attendance100.mybicocca.data.datasources.calendar.*
import it.attendance100.mybicocca.data.datasources.notification.*
import it.attendance100.mybicocca.data.datasources.user.*
import it.attendance100.mybicocca.data.repository.*
import it.attendance100.mybicocca.utils.*
import javax.inject.*
import it.attendance100.mybicocca.domain.contracts.AuthRepository as IAuthRepository
import it.attendance100.mybicocca.domain.contracts.CalendarRepository as ICalendarRepository
import it.attendance100.mybicocca.domain.contracts.NotificationRepository as INotificationRepository
import it.attendance100.mybicocca.domain.contracts.UserRepository as IUserRepository

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
      dataSource: CalendarDataSource,
      eventDao: CourseEventDao,
      scheduleDao: CourseScheduleDao,
    ): ICalendarRepository {
        return CalendarRepository(dataSource, eventDao, scheduleDao)
    }

  @Provides
  @Singleton
  fun provideUserRepository(
    dataSource: UserDataSource,
  ): IUserRepository {
    return UserRepository(dataSource)
  }

  @Provides
  @Singleton
  fun provideNotificationRepository(
    dataSource: NotificationDataSource,
  ): INotificationRepository {
    return NotificationRepository(dataSource)
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
