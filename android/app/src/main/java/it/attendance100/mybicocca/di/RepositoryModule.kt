package it.attendance100.mybicocca.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.data.repository.AccountRepositoryImpl
import it.attendance100.mybicocca.data.repository.CalendarRepositoryImpl
import it.attendance100.mybicocca.data.repository.ElearningAssignmentRepositoryImpl
import it.attendance100.mybicocca.data.repository.ElearningBadgeRepositoryImpl
import it.attendance100.mybicocca.data.repository.ElearningCatalogRepositoryImpl
import it.attendance100.mybicocca.data.repository.ElearningCourseRepositoryImpl
import it.attendance100.mybicocca.data.repository.ElearningForumRepositoryImpl
import it.attendance100.mybicocca.data.repository.ElearningGradeRepositoryImpl
import it.attendance100.mybicocca.data.repository.ElearningMessageRepositoryImpl
import it.attendance100.mybicocca.data.repository.ElearningQuizRepositoryImpl
import it.attendance100.mybicocca.data.repository.ExamRepositoryImpl
import it.attendance100.mybicocca.data.repository.MapRepositoryImpl
import it.attendance100.mybicocca.data.repository.StudyPlanRepositoryImpl
import it.attendance100.mybicocca.data.repository.TranscriptRepositoryImpl
import it.attendance100.mybicocca.data.repository.VideoPlaybackRepositoryImpl
import it.attendance100.mybicocca.domain.repository.AccountRepository
import it.attendance100.mybicocca.domain.repository.CalendarRepository
import it.attendance100.mybicocca.domain.repository.ElearningAssignmentRepository
import it.attendance100.mybicocca.domain.repository.ElearningBadgeRepository
import it.attendance100.mybicocca.domain.repository.ElearningCatalogRepository
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import it.attendance100.mybicocca.domain.repository.ElearningForumRepository
import it.attendance100.mybicocca.domain.repository.ElearningGradeRepository
import it.attendance100.mybicocca.domain.repository.ElearningMessageRepository
import it.attendance100.mybicocca.domain.repository.ElearningQuizRepository
import it.attendance100.mybicocca.domain.repository.ExamRepository
import it.attendance100.mybicocca.domain.repository.MapRepository
import it.attendance100.mybicocca.domain.repository.StudyPlanRepository
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
    abstract fun bindCalendarRepository(impl: CalendarRepositoryImpl): CalendarRepository

    @Binds
    @Singleton
    abstract fun bindStudyPlanRepository(impl: StudyPlanRepositoryImpl): StudyPlanRepository

    @Binds
    @Singleton
    abstract fun bindTranscriptRepository(impl: TranscriptRepositoryImpl): TranscriptRepository

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

    @Binds
    @Singleton
    abstract fun bindExamRepository(impl: ExamRepositoryImpl): ExamRepository

    @Binds
    @Singleton
    abstract fun bindMapRepository(impl: MapRepositoryImpl): MapRepository
}
