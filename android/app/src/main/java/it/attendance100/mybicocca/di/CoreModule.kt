package it.attendance100.mybicocca.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncScope
import javax.inject.Singleton

/**
 * Provides cross-cutting configuration singletons: the cache staleness policy, with per-scope
 * TTLs tuned to how quickly each e-learning source goes stale (quiz attempts in the sub-minute
 * range, badges hourly, course content around ten minutes).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CoreModule {

    companion object {
        @Provides
        @Singleton
        fun provideStalePolicy(): StalePolicy = StalePolicy(
            defaultTtlMs = 10 * 60_000L,
            perSourceTtlMs = mapOf(
                ElearningSyncScope.ENROLLED_COURSES to 15 * 60_000L,
                ElearningSyncScope.COURSE_DETAILS to 10 * 60_000L,
                ElearningSyncScope.COURSE_ASSIGNMENTS to 10 * 60_000L,
                ElearningSyncScope.COURSE_QUIZZES to 10 * 60_000L,
                ElearningSyncScope.COURSE_FORUMS to 10 * 60_000L,
                ElearningSyncScope.COURSE_GRADES to 10 * 60_000L,
                ElearningSyncScope.ALL_COURSE_GRADES to 10 * 60_000L,
                ElearningSyncScope.FORUM_DISCUSSIONS to 5 * 60_000L,
                ElearningSyncScope.DISCUSSION_POSTS to 5 * 60_000L,
                ElearningSyncScope.QUIZ_ATTEMPTS to 30_000L,
                ElearningSyncScope.BADGES to 60 * 60_000L,
            ),
        )
    }
}
