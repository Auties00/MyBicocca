package it.attendance100.mybicocca.di

import dagger.*
import dagger.hilt.*
import dagger.hilt.components.*
import it.attendance100.mybicocca.data.datasources.calendar.*
import it.attendance100.mybicocca.data.datasources.notification.*
import it.attendance100.mybicocca.data.datasources.user.*
import javax.inject.*

/**
 * Hilt Module per le sorgenti dati.
 * Usa @Binds per legare interfacce alle implementazioni.
 * 
 * Per switchare da Mock a Remote:
 * - Cambia MockCalendarDataSource → RemoteCalendarDataSource
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    /**
     * Fornisce l'implementazione del CalendarDataSource.
     * Attualmente usa MockCalendarDataSource per lo sviluppo.
     * 
     * Quando l'API sarà pronta, cambia in:
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
