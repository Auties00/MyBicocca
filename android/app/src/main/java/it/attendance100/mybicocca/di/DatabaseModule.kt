package it.attendance100.mybicocca.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.model.AppDatabase
import it.attendance100.mybicocca.model.CourseEventDao
import it.attendance100.mybicocca.model.CourseScheduleDao
import javax.inject.Singleton

/**
 * Hilt Module per il database Room.
 * Fornisce l'istanza del database e i DAO.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Fornisce l'istanza singleton del database Room.
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "mybicocca_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Fornisce il DAO per gli eventi del calendario.
     */
    @Provides
    @Singleton
    fun provideCourseEventDao(database: AppDatabase): CourseEventDao {
        return database.courseEventDao()
    }

    /**
     * Fornisce il DAO per gli orari ricorrenti.
     */
    @Provides
    @Singleton
    fun provideCourseScheduleDao(database: AppDatabase): CourseScheduleDao {
        return database.courseScheduleDao()
    }
}
