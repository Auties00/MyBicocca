package it.attendance100.mybicocca.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.data.repository.AccountRepositoryImpl
import it.attendance100.mybicocca.data.repository.AppLockRepositoryImpl
import it.attendance100.mybicocca.data.repository.AppointmentRepositoryImpl
import it.attendance100.mybicocca.data.repository.AttendanceRepositoryImpl
import it.attendance100.mybicocca.data.repository.CalendarRepositoryImpl
import it.attendance100.mybicocca.data.repository.ConnectivityRepositoryImpl
import it.attendance100.mybicocca.data.repository.ElearningAssignmentRepositoryImpl
import it.attendance100.mybicocca.data.repository.ElearningBadgeRepositoryImpl
import it.attendance100.mybicocca.data.repository.ElearningCatalogRepositoryImpl
import it.attendance100.mybicocca.data.repository.ElearningCourseRepositoryImpl
import it.attendance100.mybicocca.data.repository.ElearningFileRepositoryImpl
import it.attendance100.mybicocca.data.repository.ElearningForumRepositoryImpl
import it.attendance100.mybicocca.data.repository.ElearningGradeRepositoryImpl
import it.attendance100.mybicocca.data.repository.ElearningMessageRepositoryImpl
import it.attendance100.mybicocca.data.repository.ElearningQuizRepositoryImpl
import it.attendance100.mybicocca.data.repository.EnrollmentRepositoryImpl
import it.attendance100.mybicocca.data.repository.FakeExamRepository
import it.attendance100.mybicocca.data.repository.LibraryRepositoryImpl
import it.attendance100.mybicocca.data.repository.MapRepositoryImpl
import it.attendance100.mybicocca.data.repository.QuestionnaireRepositoryImpl
import it.attendance100.mybicocca.data.repository.SearchHistoryRepositoryImpl
import it.attendance100.mybicocca.data.repository.StudyPlanRepositoryImpl
import it.attendance100.mybicocca.data.repository.TaxRepositoryImpl
import it.attendance100.mybicocca.data.repository.TranscriptRepositoryImpl
import it.attendance100.mybicocca.data.repository.VideoPlaybackRepositoryImpl
import it.attendance100.mybicocca.domain.repository.AccountRepository
import it.attendance100.mybicocca.domain.repository.AppLockRepository
import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import it.attendance100.mybicocca.domain.repository.AttendanceRepository
import it.attendance100.mybicocca.domain.repository.CalendarRepository
import it.attendance100.mybicocca.domain.repository.ConnectivityRepository
import it.attendance100.mybicocca.domain.repository.ElearningAssignmentRepository
import it.attendance100.mybicocca.domain.repository.ElearningBadgeRepository
import it.attendance100.mybicocca.domain.repository.ElearningCatalogRepository
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import it.attendance100.mybicocca.domain.repository.ElearningFileRepository
import it.attendance100.mybicocca.domain.repository.ElearningForumRepository
import it.attendance100.mybicocca.domain.repository.ElearningGradeRepository
import it.attendance100.mybicocca.domain.repository.ElearningMessageRepository
import it.attendance100.mybicocca.domain.repository.ElearningQuizRepository
import it.attendance100.mybicocca.domain.repository.EnrollmentRepository
import it.attendance100.mybicocca.domain.repository.ExamRepository
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import it.attendance100.mybicocca.domain.repository.MapRepository
import it.attendance100.mybicocca.domain.repository.QuestionnaireRepository
import it.attendance100.mybicocca.domain.repository.SearchHistoryRepository
import it.attendance100.mybicocca.domain.repository.StudyPlanRepository
import it.attendance100.mybicocca.domain.repository.TaxRepository
import it.attendance100.mybicocca.domain.repository.TranscriptRepository
import it.attendance100.mybicocca.domain.repository.VideoPlaybackRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    abstract fun bindAppLockRepository(impl: AppLockRepositoryImpl): AppLockRepository

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(impl: CalendarRepositoryImpl): CalendarRepository

    @Binds
    @Singleton
    abstract fun bindConnectivityRepository(impl: ConnectivityRepositoryImpl): ConnectivityRepository

    @Binds
    @Singleton
    abstract fun bindStudyPlanRepository(impl: StudyPlanRepositoryImpl): StudyPlanRepository

    @Binds
    @Singleton
    abstract fun bindTranscriptRepository(impl: TranscriptRepositoryImpl): TranscriptRepository

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(impl: AttendanceRepositoryImpl): AttendanceRepository

    @Binds
    @Singleton
    abstract fun bindElearningCourseRepository(impl: ElearningCourseRepositoryImpl): ElearningCourseRepository

    @Binds
    @Singleton
    abstract fun bindElearningCatalogRepository(impl: ElearningCatalogRepositoryImpl): ElearningCatalogRepository

    @Binds
    @Singleton
    abstract fun bindElearningAssignmentRepository(impl: ElearningAssignmentRepositoryImpl): ElearningAssignmentRepository

    @Binds
    @Singleton
    abstract fun bindElearningQuizRepository(impl: ElearningQuizRepositoryImpl): ElearningQuizRepository

    @Binds
    @Singleton
    abstract fun bindElearningForumRepository(impl: ElearningForumRepositoryImpl): ElearningForumRepository

    @Binds
    @Singleton
    abstract fun bindElearningFileRepository(impl: ElearningFileRepositoryImpl): ElearningFileRepository

    @Binds
    @Singleton
    abstract fun bindElearningGradeRepository(impl: ElearningGradeRepositoryImpl): ElearningGradeRepository

    @Binds
    @Singleton
    abstract fun bindElearningMessageRepository(impl: ElearningMessageRepositoryImpl): ElearningMessageRepository

    @Binds
    @Singleton
    abstract fun bindElearningBadgeRepository(impl: ElearningBadgeRepositoryImpl): ElearningBadgeRepository

    @Binds
    @Singleton
    abstract fun bindVideoPlaybackRepository(impl: VideoPlaybackRepositoryImpl): VideoPlaybackRepository

    // TEMPORARY: fake esiti for UI testing — revert to ExamRepositoryImpl.
    @Binds
    @Singleton
    abstract fun bindExamRepository(impl: FakeExamRepository): ExamRepository

    @Binds
    @Singleton
    abstract fun bindTaxRepository(impl: TaxRepositoryImpl): TaxRepository

    @Binds
    @Singleton
    abstract fun bindQuestionnaireRepository(impl: QuestionnaireRepositoryImpl): QuestionnaireRepository

    @Binds
    @Singleton
    abstract fun bindMapRepository(impl: MapRepositoryImpl): MapRepository

    @Binds
    @Singleton
    abstract fun bindSearchHistoryRepository(impl: SearchHistoryRepositoryImpl): SearchHistoryRepository

    @Binds
    @Singleton
    abstract fun bindEnrollmentRepository(impl: EnrollmentRepositoryImpl): EnrollmentRepository

    @Binds
    @Singleton
    abstract fun bindAppointmentRepository(impl: AppointmentRepositoryImpl): AppointmentRepository

    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: LibraryRepositoryImpl): LibraryRepository
}
