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

interface ElearningQuizRepository {
    fun observeForCourse(accountId: AccountId, courseId: CourseId): Flow<Loadable<List<Quiz>>>
    fun observe(accountId: AccountId, quizId: QuizId): Flow<Loadable<Quiz>>
    fun observeAttempts(accountId: AccountId, quizId: QuizId): Flow<Loadable<List<QuizAttempt>>>
    fun observeBestGrade(accountId: AccountId, quizId: QuizId): Flow<Loadable<BestGrade?>>
    fun observeDraftAnswers(accountId: AccountId, attemptId: AttemptId): Flow<List<AttemptAnswer>>

    suspend fun refreshForCourse(accountId: AccountId, courseId: CourseId, force: Boolean = false)
    suspend fun refreshAttempts(accountId: AccountId, quizId: QuizId)
    suspend fun startAttempt(accountId: AccountId, quizId: QuizId, force: Boolean = false): AttemptId
    suspend fun loadAttemptPage(accountId: AccountId, attemptId: AttemptId, page: Int): AttemptPage
    suspend fun saveDraft(accountId: AccountId, attemptId: AttemptId, answers: List<AttemptAnswer>)
    suspend fun submitAttempt(
        accountId: AccountId,
        attemptId: AttemptId,
        answers: List<AttemptAnswer>,
        timeUp: Boolean = false,
    )
    suspend fun loadReview(accountId: AccountId, attemptId: AttemptId): AttemptReview

    suspend fun clearForAccount(accountId: AccountId)
}
