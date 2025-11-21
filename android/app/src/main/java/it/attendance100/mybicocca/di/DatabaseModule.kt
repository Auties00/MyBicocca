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
 * Provides Room database instances
 * Supplies the database instance and DAOs
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the singleton instance of the Room database
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
     * Provides the DAO for calendar events
     */
    @Provides
    @Singleton
    fun provideCourseEventDao(database: AppDatabase): CourseEventDao {
        return database.courseEventDao()
    }

    /**
     * Provides the DAO for recurring schedules
     */
    @Provides
    @Singleton
    fun provideCourseScheduleDao(database: AppDatabase): CourseScheduleDao {
        return database.courseScheduleDao()
    }
}
