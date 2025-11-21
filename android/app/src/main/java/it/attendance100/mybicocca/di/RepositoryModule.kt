package it.attendance100.mybicocca.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.data.calendar.CalendarDataSource
import it.attendance100.mybicocca.model.CourseEventDao
import it.attendance100.mybicocca.model.CourseScheduleDao
import it.attendance100.mybicocca.repository.CalendarRepository
import javax.inject.Singleton

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
        scheduleDao: CourseScheduleDao
    ): CalendarRepository {
        return CalendarRepository(dataSource, eventDao, scheduleDao)
    }
}
