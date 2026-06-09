package it.attendance100.mybicocca.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.data.local.account.AccountDao
import it.attendance100.mybicocca.data.local.account.MyBicoccaDatabase
import it.attendance100.mybicocca.data.local.appointment.AppointmentReservationDao
import it.attendance100.mybicocca.data.local.calendar.CalendarDao
import it.attendance100.mybicocca.data.local.calendar.CalendarSyncStateDao
import it.attendance100.mybicocca.data.local.elearning.assignment.AssignmentDao
import it.attendance100.mybicocca.data.local.elearning.badge.BadgeDao
import it.attendance100.mybicocca.data.local.elearning.course.CourseDao
import it.attendance100.mybicocca.data.local.elearning.deadline.DeadlineDao
import it.attendance100.mybicocca.data.local.elearning.forum.ForumDao
import it.attendance100.mybicocca.data.local.elearning.grade.GradeDao
import it.attendance100.mybicocca.data.local.elearning.message.MessageDao
import it.attendance100.mybicocca.data.local.elearning.quiz.QuizDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateDao
import it.attendance100.mybicocca.data.local.elearning.video.VideoProgressDao
import it.attendance100.mybicocca.data.local.library.LibraryReservationDao
import it.attendance100.mybicocca.data.local.map.MapBuildingDao
import it.attendance100.mybicocca.data.local.map.MapRoomDao
import it.attendance100.mybicocca.data.local.map.MapRoomSyncStateDao
import it.attendance100.mybicocca.data.local.transcript.TranscriptDao
import it.attendance100.mybicocca.data.local.transcript.TranscriptSyncStateDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MyBicoccaDatabase =
        Room.databaseBuilder(context, MyBicoccaDatabase::class.java, DATABASE_NAME)
            // TODO remove before first prod release
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideAccountDao(db: MyBicoccaDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideCalendarDao(db: MyBicoccaDatabase): CalendarDao = db.calendarDao()

    @Provides
    fun provideCalendarSyncStateDao(db: MyBicoccaDatabase): CalendarSyncStateDao = db.calendarSyncStateDao()

    @Provides
    fun provideElearningCourseDao(db: MyBicoccaDatabase): CourseDao = db.elearningCourseDao()

    @Provides
    fun provideElearningDeadlineDao(db: MyBicoccaDatabase): DeadlineDao = db.elearningDeadlineDao()

    @Provides
    fun provideElearningAssignmentDao(db: MyBicoccaDatabase): AssignmentDao = db.elearningAssignmentDao()

    @Provides
    fun provideElearningQuizDao(db: MyBicoccaDatabase): QuizDao = db.elearningQuizDao()

    @Provides
    fun provideElearningForumDao(db: MyBicoccaDatabase): ForumDao = db.elearningForumDao()

    @Provides
    fun provideElearningGradeDao(db: MyBicoccaDatabase): GradeDao = db.elearningGradeDao()

    @Provides
    fun provideElearningMessageDao(db: MyBicoccaDatabase): MessageDao = db.elearningMessageDao()

    @Provides
    fun provideElearningBadgeDao(db: MyBicoccaDatabase): BadgeDao = db.elearningBadgeDao()

    @Provides
    fun provideElearningSyncStateDao(db: MyBicoccaDatabase): ElearningSyncStateDao = db.elearningSyncStateDao()

    @Provides
    fun provideElearningVideoProgressDao(db: MyBicoccaDatabase): VideoProgressDao =
        db.elearningVideoProgressDao()

    @Provides
    fun provideTranscriptDao(db: MyBicoccaDatabase): TranscriptDao = db.transcriptDao()

    @Provides
    fun provideTranscriptSyncStateDao(db: MyBicoccaDatabase): TranscriptSyncStateDao = db.transcriptSyncStateDao()

    @Provides
    fun provideMapBuildingDao(db: MyBicoccaDatabase): MapBuildingDao = db.mapBuildingDao()

    @Provides
    fun provideMapRoomDao(db: MyBicoccaDatabase): MapRoomDao = db.mapRoomDao()

    @Provides
    fun provideMapRoomSyncStateDao(db: MyBicoccaDatabase): MapRoomSyncStateDao = db.mapRoomSyncStateDao()

    @Provides
    fun provideAppointmentReservationDao(db: MyBicoccaDatabase): AppointmentReservationDao =
        db.appointmentReservationDao()

    @Provides
    fun provideLibraryReservationDao(db: MyBicoccaDatabase): LibraryReservationDao =
        db.libraryReservationDao()

    private const val DATABASE_NAME = "mybicocca.db"
}
