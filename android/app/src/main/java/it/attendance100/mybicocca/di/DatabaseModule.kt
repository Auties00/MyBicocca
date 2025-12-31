package it.attendance100.mybicocca.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.data.dao.CourseEventDao
import it.attendance100.mybicocca.data.dao.UserDao
import javax.inject.Singleton

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
        @ApplicationContext context: Context,
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
     * Provides the DAO for calendar events
     */
    @Provides
    @Singleton
    fun provideCourseEventDao(database: AppDatabase): CourseEventDao {
        return database.courseEventDao()
    }

    /**
     * Provides the DAO for user profile
     */
    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}
