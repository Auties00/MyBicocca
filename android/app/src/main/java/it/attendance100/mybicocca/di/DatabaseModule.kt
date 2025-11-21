package it.attendance100.mybicocca.di

import android.content.*
import androidx.room.*
import dagger.*
import dagger.hilt.*
import dagger.hilt.android.qualifiers.*
import dagger.hilt.components.*
import it.attendance100.mybicocca.data.daos.*
import javax.inject.*

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
            .fallbackToDestructiveMigration(false)
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
