package it.attendance100.mybicocca.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.data.calendar.CalendarDataSource
import it.attendance100.mybicocca.data.calendar.MockCalendarDataSource
import javax.inject.Singleton

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
}
