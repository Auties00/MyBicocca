package it.attendance100.mybicocca.di

import dagger.*
import dagger.hilt.*
import dagger.hilt.components.*
import it.attendance100.mybicocca.data.daos.*
import it.attendance100.mybicocca.data.datasources.calendar.*
import it.attendance100.mybicocca.data.repository.*
import javax.inject.*
import it.attendance100.mybicocca.domain.contracts.CalendarRepository as ICalendarRepository
import it.attendance100.mybicocca.domain.contracts.NotificationRepository as INotificationRepository
import it.attendance100.mybicocca.domain.contracts.UserRepository as IUserRepository

/**
 * Hilt Module per i repository.
 * Fornisce le istanze dei repository iniettando le dipendenze necessarie.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Fornisce il CalendarRepository.
     * Hilt inietterà automaticamente DataSource e DAO.
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
    dataSource: it.attendance100.mybicocca.data.datasources.user.UserDataSource,
  ): IUserRepository {
    return UserRepository(dataSource)
  }

  @Provides
  @Singleton
  fun provideNotificationRepository(
    dataSource: it.attendance100.mybicocca.data.datasources.notification.NotificationDataSource,
  ): INotificationRepository {
    return NotificationRepository(dataSource)
  }
}
