package it.attendance100.mybicocca.di

import dagger.*
import dagger.hilt.*
import dagger.hilt.components.*
import it.attendance100.mybicocca.data.datasources.calendar.*
import it.attendance100.mybicocca.data.datasources.notification.*
import it.attendance100.mybicocca.data.datasources.user.*
import javax.inject.*

/**
 * Chooses which data source to inject
 * Uses @Binds to link interfaces to their implementations
 *
 * To switch from Mock to Remote:
 * - Change MockCalendarDataSource -> RemoteCalendarDataSource
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    /**
     * Provides the implementation of CalendarDataSource
     * Currently uses MockCalendarDataSource for development
     *
     * When the API is ready, change to:
     * @Binds abstract fun bindCalendarDataSource(impl: RemoteCalendarDataSource): CalendarDataSource
     */
    @Binds
    @Singleton
    abstract fun bindCalendarDataSource(
        mockDataSource: MockCalendarDataSource
    ): CalendarDataSource

  @Binds
  @Singleton
  abstract fun bindUserDataSource(
    mockDataSource: MockUserDataSource,
  ): UserDataSource

  @Binds
  @Singleton
  abstract fun bindNotificationDataSource(
    mockDataSource: MockNotificationDataSource,
  ): NotificationDataSource
}
