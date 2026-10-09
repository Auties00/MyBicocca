package it.attendance100.mybicocca.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.demo.repository.DemoAccountRepository
import it.attendance100.mybicocca.data.repository.AppLockRepositoryImpl
import it.attendance100.mybicocca.data.repository.AppearanceSettingsRepositoryImpl
import it.attendance100.mybicocca.demo.repository.DemoAppointmentRepository
import it.attendance100.mybicocca.demo.repository.DemoAttendanceRepository
import it.attendance100.mybicocca.demo.repository.DemoCalendarRepository
import it.attendance100.mybicocca.demo.repository.DemoCertificateRepository
import it.attendance100.mybicocca.data.repository.ConnectivityRepositoryImpl
import it.attendance100.mybicocca.data.repository.DeepLinkRepositoryImpl
import it.attendance100.mybicocca.demo.repository.DemoDocumentRepository
import it.attendance100.mybicocca.demo.repository.DemoElearningAssignmentRepository
import it.attendance100.mybicocca.demo.repository.DemoElearningBadgeRepository
import it.attendance100.mybicocca.data.repository.ElearningCatalogRepositoryImpl
import it.attendance100.mybicocca.demo.repository.DemoElearningCourseRepository
import it.attendance100.mybicocca.demo.repository.DemoElearningFileRepository
import it.attendance100.mybicocca.demo.repository.DemoElearningForumRepository
import it.attendance100.mybicocca.demo.repository.DemoElearningGradeRepository
import it.attendance100.mybicocca.demo.repository.DemoElearningQuizRepository
import it.attendance100.mybicocca.demo.repository.DemoEnrollmentRepository
import it.attendance100.mybicocca.demo.repository.DemoExamRepository
import it.attendance100.mybicocca.data.repository.FileOpenPreferenceRepositoryImpl
import it.attendance100.mybicocca.data.repository.HapticSettingsRepositoryImpl
import it.attendance100.mybicocca.demo.repository.DemoLibraryRepository
import it.attendance100.mybicocca.data.repository.MapRepositoryImpl
import it.attendance100.mybicocca.data.repository.PdfViewerSettingsRepositoryImpl
import it.attendance100.mybicocca.data.repository.PrivacySettingsRepositoryImpl
import it.attendance100.mybicocca.demo.repository.DemoQuestionnaireRepository
import it.attendance100.mybicocca.data.repository.SearchHistoryRepositoryImpl
import it.attendance100.mybicocca.data.repository.SecuritySettingsRepositoryImpl
import it.attendance100.mybicocca.demo.repository.DemoStudyPlanRepository
import it.attendance100.mybicocca.demo.repository.DemoTaxRepository
import it.attendance100.mybicocca.demo.repository.DemoTranscriptRepository
import it.attendance100.mybicocca.demo.repository.DemoUpdateRepository
import it.attendance100.mybicocca.demo.repository.DemoVideoPlaybackRepository
import it.attendance100.mybicocca.domain.repository.AccountRepository
import it.attendance100.mybicocca.domain.repository.AppLockRepository
import it.attendance100.mybicocca.domain.repository.AppearanceSettingsRepository
import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import it.attendance100.mybicocca.domain.repository.AttendanceRepository
import it.attendance100.mybicocca.domain.repository.CalendarRepository
import it.attendance100.mybicocca.domain.repository.CertificateRepository
import it.attendance100.mybicocca.domain.repository.ConnectivityRepository
import it.attendance100.mybicocca.domain.repository.DeepLinkRepository
import it.attendance100.mybicocca.domain.repository.DocumentRepository
import it.attendance100.mybicocca.domain.repository.ElearningAssignmentRepository
import it.attendance100.mybicocca.domain.repository.ElearningBadgeRepository
import it.attendance100.mybicocca.domain.repository.ElearningCatalogRepository
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import it.attendance100.mybicocca.domain.repository.ElearningFileRepository
import it.attendance100.mybicocca.domain.repository.ElearningForumRepository
import it.attendance100.mybicocca.domain.repository.ElearningGradeRepository
import it.attendance100.mybicocca.domain.repository.ElearningQuizRepository
import it.attendance100.mybicocca.domain.repository.EnrollmentRepository
import it.attendance100.mybicocca.domain.repository.ExamRepository
import it.attendance100.mybicocca.domain.repository.FileOpenPreferenceRepository
import it.attendance100.mybicocca.domain.repository.HapticSettingsRepository
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import it.attendance100.mybicocca.domain.repository.MapRepository
import it.attendance100.mybicocca.domain.repository.PdfViewerSettingsRepository
import it.attendance100.mybicocca.domain.repository.PrivacySettingsRepository
import it.attendance100.mybicocca.domain.repository.QuestionnaireRepository
import it.attendance100.mybicocca.domain.repository.SearchHistoryRepository
import it.attendance100.mybicocca.domain.repository.SecuritySettingsRepository
import it.attendance100.mybicocca.domain.repository.StudyPlanRepository
import it.attendance100.mybicocca.domain.repository.TaxRepository
import it.attendance100.mybicocca.domain.repository.TranscriptRepository
import it.attendance100.mybicocca.domain.repository.UpdateRepository
import it.attendance100.mybicocca.domain.repository.VideoPlaybackRepository
import javax.inject.Singleton

/**
 * Demo counterpart of the live module: binds the repositories that carry student data to fakes
 * under `demo.repository`, and leaves device-local ones (settings, app lock) on the real ones.
 */
@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAdminMessageRepository(impl: it.attendance100.mybicocca.data.admin.AdminMessageRepositoryImpl): it.attendance100.mybicocca.domain.repository.admin.AdminMessageRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: DemoAccountRepository): AccountRepository

    @Binds
    @Singleton
    abstract fun bindAppLockRepository(impl: AppLockRepositoryImpl): AppLockRepository

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(impl: DemoCalendarRepository): CalendarRepository

    @Binds
    @Singleton
    abstract fun bindConnectivityRepository(impl: ConnectivityRepositoryImpl): ConnectivityRepository

    @Binds
    @Singleton
    abstract fun bindStudyPlanRepository(impl: DemoStudyPlanRepository): StudyPlanRepository

    @Binds
    @Singleton
    abstract fun bindTranscriptRepository(impl: DemoTranscriptRepository): TranscriptRepository

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(impl: DemoAttendanceRepository): AttendanceRepository

    @Binds
    @Singleton
    abstract fun bindElearningCourseRepository(impl: DemoElearningCourseRepository): ElearningCourseRepository

    @Binds
    @Singleton
    abstract fun bindElearningCatalogRepository(impl: ElearningCatalogRepositoryImpl): ElearningCatalogRepository

    @Binds
    @Singleton
    abstract fun bindElearningAssignmentRepository(impl: DemoElearningAssignmentRepository): ElearningAssignmentRepository

    @Binds
    @Singleton
    abstract fun bindElearningQuizRepository(impl: DemoElearningQuizRepository): ElearningQuizRepository

    @Binds
    @Singleton
    abstract fun bindElearningForumRepository(impl: DemoElearningForumRepository): ElearningForumRepository

    @Binds
    @Singleton
    abstract fun bindElearningFileRepository(impl: DemoElearningFileRepository): ElearningFileRepository

    @Binds
    @Singleton
    abstract fun bindElearningGradeRepository(impl: DemoElearningGradeRepository): ElearningGradeRepository

    @Binds
    @Singleton
    abstract fun bindElearningBadgeRepository(impl: DemoElearningBadgeRepository): ElearningBadgeRepository

    @Binds
    @Singleton
    abstract fun bindVideoPlaybackRepository(impl: DemoVideoPlaybackRepository): VideoPlaybackRepository

    @Binds
    @Singleton
    abstract fun bindExamRepository(impl: DemoExamRepository): ExamRepository

    @Binds
    @Singleton
    abstract fun bindTaxRepository(impl: DemoTaxRepository): TaxRepository

    @Binds
    @Singleton
    abstract fun bindQuestionnaireRepository(impl: DemoQuestionnaireRepository): QuestionnaireRepository

    @Binds
    @Singleton
    abstract fun bindMapRepository(impl: MapRepositoryImpl): MapRepository

    @Binds
    @Singleton
    abstract fun bindSearchHistoryRepository(impl: SearchHistoryRepositoryImpl): SearchHistoryRepository

    @Binds
    @Singleton
    abstract fun bindEnrollmentRepository(impl: DemoEnrollmentRepository): EnrollmentRepository

    @Binds
    @Singleton
    abstract fun bindAppointmentRepository(impl: DemoAppointmentRepository): AppointmentRepository

    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: DemoLibraryRepository): LibraryRepository

    @Binds
    @Singleton
    abstract fun bindAppearanceSettingsRepository(impl: AppearanceSettingsRepositoryImpl): AppearanceSettingsRepository

    @Binds
    @Singleton
    abstract fun bindSecuritySettingsRepository(impl: SecuritySettingsRepositoryImpl): SecuritySettingsRepository

    @Binds
    @Singleton
    abstract fun bindPrivacySettingsRepository(impl: PrivacySettingsRepositoryImpl): PrivacySettingsRepository

    @Binds
    @Singleton
    abstract fun bindHapticSettingsRepository(impl: HapticSettingsRepositoryImpl): HapticSettingsRepository

    @Binds
    @Singleton
    abstract fun bindFileOpenPreferenceRepository(impl: FileOpenPreferenceRepositoryImpl): FileOpenPreferenceRepository

    @Binds
    @Singleton
    abstract fun bindPdfViewerSettingsRepository(impl: PdfViewerSettingsRepositoryImpl): PdfViewerSettingsRepository

    @Binds
    @Singleton
    abstract fun bindDeepLinkRepository(impl: DeepLinkRepositoryImpl): DeepLinkRepository

    @Binds
    @Singleton
    abstract fun bindCertificateRepository(impl: DemoCertificateRepository): CertificateRepository

    @Binds
    @Singleton
    abstract fun bindDocumentRepository(impl: DemoDocumentRepository): DocumentRepository

    @Binds
    @Singleton
    abstract fun bindUpdateRepository(impl: DemoUpdateRepository): UpdateRepository
}
