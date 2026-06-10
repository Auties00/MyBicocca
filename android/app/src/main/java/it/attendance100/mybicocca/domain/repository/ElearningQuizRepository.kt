package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptAnswer
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptPage
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptReview
import it.attendance100.mybicocca.domain.model.elearning.quiz.BestGrade
import it.attendance100.mybicocca.domain.model.elearning.quiz.Quiz
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizAttempt
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import kotlinx.coroutines.flow.Flow

/**
 * Contract for quizzes of e-learning courses, covering the whole attempt flow: starting,
 * loading pages, autosaving drafts, submitting and reviewing. Observe methods are hot flows
 * from the local cache, which is the single source of truth; refresh and attempt methods hit
 * the e-learning platform and throw on failure. All cached data is account-scoped.
 */
interface ElearningQuizRepository {
    /** Streams a course's cached quizzes. */
    fun observeForCourse(accountId: AccountId, courseId: CourseId): Flow<Loadable<List<Quiz>>>

    /** Streams one cached quiz; not-yet-loaded while absent from the cache. */
    fun observe(accountId: AccountId, quizId: QuizId): Flow<Loadable<Quiz>>

    /** Account-wide cached stream feeding the unified search index. */
    fun observeAllForAccount(accountId: AccountId): Flow<List<Quiz>>

    /** Streams the student's cached attempts at a quiz, newest first. */
    fun observeAttempts(accountId: AccountId, quizId: QuizId): Flow<Loadable<List<QuizAttempt>>>

    /** Streams the cached best grade for a quiz; the loaded value is null while none is cached. */
    fun observeBestGrade(accountId: AccountId, quizId: QuizId): Flow<Loadable<BestGrade?>>

    /** Streams the locally persisted draft answers of an attempt, in slot order. */
    fun observeDraftAnswers(accountId: AccountId, attemptId: AttemptId): Flow<List<AttemptAnswer>>

    /**
     * Syncs a course's quizzes into the cache. Skipped while the cached data is fresher than
     * the staleness policy unless [force]d.
     */
    suspend fun refreshForCourse(accountId: AccountId, courseId: CourseId, force: Boolean = false)

    /** Syncs the student's attempts and best grade for a quiz into the cache. */
    suspend fun refreshAttempts(accountId: AccountId, quizId: QuizId)

    /**
     * Starts an attempt on the platform, caches it and returns its id; [force] requests a
     * brand-new attempt.
     */
    suspend fun startAttempt(accountId: AccountId, quizId: QuizId, force: Boolean = false): AttemptId

    /**
     * Loads one page of questions of an in-progress attempt, reconciling the cached attempt
     * state along the way.
     */
    suspend fun loadAttemptPage(accountId: AccountId, attemptId: AttemptId, page: Int): AttemptPage

    /**
     * Saves the given answers locally and to the platform's attempt autosave. The local write
     * happens first, so an interrupted attempt keeps its answers even when the remote save fails.
     */
    suspend fun saveDraft(accountId: AccountId, attemptId: AttemptId, answers: List<AttemptAnswer>)

    /**
     * Submits the attempt for grading with the final answers ([timeUp] marks an expired time
     * limit) and clears the locally persisted drafts. The cached attempt list is not refreshed
     * here; callers refresh attempts afterwards to surface the finished state and best grade.
     */
    suspend fun submitAttempt(
        accountId: AccountId,
        attemptId: AttemptId,
        answers: List<AttemptAnswer>,
        timeUp: Boolean = false,
    )

    /**
     * Loads the graded review of a finished attempt, reconciling the cached attempt state along
     * the way.
     */
    suspend fun loadReview(accountId: AccountId, attemptId: AttemptId): AttemptReview

    /** Drops every cached quiz, attempt, best grade and draft answer of the account. */
    suspend fun clearForAccount(accountId: AccountId)
}
