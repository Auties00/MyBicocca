package it.attendance100.mybicocca.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncScope
import javax.inject.Singleton

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
                ElearningSyncScope.CONVERSATIONS to 60_000L,
                ElearningSyncScope.CONVERSATION_MESSAGES to 60_000L,
                ElearningSyncScope.QUIZ_ATTEMPTS to 30_000L,
                ElearningSyncScope.BADGES to 60 * 60_000L,
            ),
        )
    }
}
