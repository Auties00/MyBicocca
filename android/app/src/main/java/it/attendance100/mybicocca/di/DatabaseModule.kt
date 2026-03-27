package it.attendance100.mybicocca.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.data.database.MyBicoccaDatabase
import it.attendance100.mybicocca.data.database.dao.AppointmentDao
import it.attendance100.mybicocca.data.database.dao.AssignmentDao
import it.attendance100.mybicocca.data.database.dao.AttendanceDao
import it.attendance100.mybicocca.data.database.dao.BadgeDao
import it.attendance100.mybicocca.data.database.dao.CalendarDao
import it.attendance100.mybicocca.data.database.dao.CampusDao
import it.attendance100.mybicocca.data.database.dao.CourseDao
import it.attendance100.mybicocca.data.database.dao.DegreeAwardDao
import it.attendance100.mybicocca.data.database.dao.EvaluationDao
import it.attendance100.mybicocca.data.database.dao.ExamDao
import it.attendance100.mybicocca.data.database.dao.ForumDao
import it.attendance100.mybicocca.data.database.dao.InternshipDao
import it.attendance100.mybicocca.data.database.dao.MessagingDao
import it.attendance100.mybicocca.data.database.dao.QuizDao
import it.attendance100.mybicocca.data.database.dao.CareerDao
import it.attendance100.mybicocca.data.database.dao.IseeDao
import it.attendance100.mybicocca.data.database.dao.ReferenceDao
import it.attendance100.mybicocca.data.database.dao.StudyPlanDao
import it.attendance100.mybicocca.data.database.dao.TaxDao
import it.attendance100.mybicocca.data.database.dao.TeacherDao
import it.attendance100.mybicocca.data.database.dao.TranscriptDao
import it.attendance100.mybicocca.data.database.dao.UserDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MyBicoccaDatabase =
        Room.databaseBuilder(context, MyBicoccaDatabase::class.java, "mybicocca.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideUserDao(db: MyBicoccaDatabase): UserDao = db.userDao()

    @Provides
    fun provideCourseDao(db: MyBicoccaDatabase): CourseDao = db.courseDao()

    @Provides
    fun provideAssignmentDao(db: MyBicoccaDatabase): AssignmentDao = db.assignmentDao()

    @Provides
    fun provideQuizDao(db: MyBicoccaDatabase): QuizDao = db.quizDao()

    @Provides
    fun provideForumDao(db: MyBicoccaDatabase): ForumDao = db.forumDao()

    @Provides
    fun provideBadgeDao(db: MyBicoccaDatabase): BadgeDao = db.badgeDao()

    @Provides
    fun provideMessagingDao(db: MyBicoccaDatabase): MessagingDao = db.messagingDao()

    @Provides
    fun provideCareerDao(db: MyBicoccaDatabase): CareerDao = db.careerDao()

    @Provides
    fun provideTranscriptDao(db: MyBicoccaDatabase): TranscriptDao = db.transcriptDao()

    @Provides
    fun provideStudyPlanDao(db: MyBicoccaDatabase): StudyPlanDao = db.studyPlanDao()

    @Provides
    fun provideTaxDao(db: MyBicoccaDatabase): TaxDao = db.taxDao()

    @Provides
    fun provideCalendarDao(db: MyBicoccaDatabase): CalendarDao = db.calendarDao()

    @Provides
    fun provideExamDao(db: MyBicoccaDatabase): ExamDao = db.examDao()

    @Provides
    fun provideCampusDao(db: MyBicoccaDatabase): CampusDao = db.campusDao()

    @Provides
    fun provideTeacherDao(db: MyBicoccaDatabase): TeacherDao = db.teacherDao()

    @Provides
    fun provideAttendanceDao(db: MyBicoccaDatabase): AttendanceDao = db.attendanceDao()

    @Provides
    fun provideDegreeAwardDao(db: MyBicoccaDatabase): DegreeAwardDao = db.degreeAwardDao()

    @Provides
    fun provideInternshipDao(db: MyBicoccaDatabase): InternshipDao = db.internshipDao()

    @Provides
    fun provideEvaluationDao(db: MyBicoccaDatabase): EvaluationDao = db.evaluationDao()

    @Provides
    fun provideAppointmentDao(db: MyBicoccaDatabase): AppointmentDao = db.appointmentDao()

    @Provides
    fun provideReferenceDao(db: MyBicoccaDatabase): ReferenceDao = db.referenceDao()

    @Provides
    fun provideIseeDao(db: MyBicoccaDatabase): IseeDao = db.iseeDao()
}
