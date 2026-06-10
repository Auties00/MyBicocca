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
import it.attendance100.mybicocca.data.local.certificate.CertificateCacheDao
import it.attendance100.mybicocca.data.local.document.DocumentCacheDao
import it.attendance100.mybicocca.data.local.enrollment.EnrollmentCacheDao
import it.attendance100.mybicocca.data.local.exam.ExamCacheDao
import it.attendance100.mybicocca.data.local.questionnaire.QuestionnaireCacheDao
import it.attendance100.mybicocca.data.local.tax.TaxCacheDao
import it.attendance100.mybicocca.data.local.calendar.CalendarDao
import it.attendance100.mybicocca.data.local.calendar.CalendarSyncStateDao
import it.attendance100.mybicocca.data.local.elearning.assignment.AssignmentDao
import it.attendance100.mybicocca.data.local.elearning.badge.BadgeDao
import it.attendance100.mybicocca.data.local.elearning.course.CourseDao
import it.attendance100.mybicocca.data.local.elearning.deadline.DeadlineDao
import it.attendance100.mybicocca.data.local.elearning.forum.ForumDao
import it.attendance100.mybicocca.data.local.elearning.grade.GradeDao
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

/** Provides the Room database and exposes each of its DAOs as an injectable. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Schema version bumps must ship an explicit [androidx.room.migration.Migration] registered on
     * this builder. The destructive fallback that dropped and recreated every table on a version
     * change is gone: a released build preserves the user's cached data across upgrades, so opening
     * a database whose version advanced without a matching migration is a hard failure surfaced
     * during development rather than silent data loss in the field.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MyBicoccaDatabase =
        Room.databaseBuilder(context, MyBicoccaDatabase::class.java, DATABASE_NAME)
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

    @Provides
    fun provideExamCacheDao(db: MyBicoccaDatabase): ExamCacheDao = db.examCacheDao()

    @Provides
    fun provideTaxCacheDao(db: MyBicoccaDatabase): TaxCacheDao = db.taxCacheDao()

    @Provides
    fun provideEnrollmentCacheDao(db: MyBicoccaDatabase): EnrollmentCacheDao = db.enrollmentCacheDao()

    @Provides
    fun provideQuestionnaireCacheDao(db: MyBicoccaDatabase): QuestionnaireCacheDao =
        db.questionnaireCacheDao()

    @Provides
    fun provideDocumentCacheDao(db: MyBicoccaDatabase): DocumentCacheDao = db.documentCacheDao()

    @Provides
    fun provideCertificateCacheDao(db: MyBicoccaDatabase): CertificateCacheDao =
        db.certificateCacheDao()

    private const val DATABASE_NAME = "mybicocca.db"
}
