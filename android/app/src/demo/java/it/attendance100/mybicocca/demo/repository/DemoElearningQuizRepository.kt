package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.demo.DemoClock
import it.attendance100.mybicocca.demo.DemoCourse
import it.attendance100.mybicocca.demo.DemoCourses
import it.attendance100.mybicocca.demo.DemoElearning
import it.attendance100.mybicocca.demo.DemoStudent
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptAnswer
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptPage
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptQuestion
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptReview
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptState
import it.attendance100.mybicocca.domain.model.elearning.quiz.BestGrade
import it.attendance100.mybicocca.domain.model.elearning.quiz.Quiz
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizAttempt
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import it.attendance100.mybicocca.domain.repository.ElearningQuizRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One self-assessment quiz per course with a finished attempt already on record. New attempts
 * serve the same three multiple-choice questions, written in the Moodle markup the UI parses.
 */
@Singleton
class DemoElearningQuizRepository @Inject constructor() : ElearningQuizRepository {

    private val quizzes = DemoCourses.all.map(::quiz)
    private val attempts = MutableStateFlow(quizzes.associate { it.id to listOf(finishedAttempt(it.id)) })
    private val drafts = MutableStateFlow<Map<AttemptId, List<AttemptAnswer>>>(emptyMap())

    override fun observeForCourse(accountId: AccountId, courseId: CourseId): Flow<Loadable<List<Quiz>>> =
        flowOf(Loadable.Loaded(quizzes.filter { it.courseId == courseId }))

    override fun observe(accountId: AccountId, quizId: QuizId): Flow<Loadable<Quiz>> =
        flowOf(quizzes.firstOrNull { it.id == quizId }?.let { Loadable.Loaded(it) } ?: Loadable.NotYetLoaded)

    override fun observeAllForAccount(accountId: AccountId): Flow<List<Quiz>> = flowOf(quizzes)

    override fun observeAttempts(accountId: AccountId, quizId: QuizId): Flow<Loadable<List<QuizAttempt>>> =
        attempts.map { Loadable.Loaded(it[quizId].orEmpty()) }

    override fun observeBestGrade(accountId: AccountId, quizId: QuizId): Flow<Loadable<BestGrade?>> =
        attempts.map { all ->
            val best = all[quizId].orEmpty().mapNotNull { it.sumGrades }.maxOrNull()
            Loadable.Loaded(best?.let { BestGrade(quizId, grade = it / QUESTIONS * MAX_GRADE, maxGrade = MAX_GRADE) })
        }

    override fun observeDraftAnswers(accountId: AccountId, attemptId: AttemptId): Flow<List<AttemptAnswer>> =
        drafts.map { it[attemptId].orEmpty() }

    override suspend fun refreshForCourse(accountId: AccountId, courseId: CourseId, force: Boolean) = Unit

    override suspend fun refreshAttempts(accountId: AccountId, quizId: QuizId) = Unit

    override suspend fun startAttempt(accountId: AccountId, quizId: QuizId, force: Boolean): AttemptId {
        val existing = attempts.value[quizId].orEmpty()
        existing.firstOrNull { it.state == AttemptState.InProgress }?.let { return it.id }
        val attempt = QuizAttempt(
            id = AttemptId(quizId.value * 10 + existing.size + 1),
            quizId = quizId,
            userId = DemoStudent.account.learning.lmsUserId,
            attemptNumber = existing.size + 1,
            state = AttemptState.InProgress,
            sumGrades = null,
            timeStart = DemoClock.fixed.instant(),
            timeFinish = null,
            timeModified = DemoClock.fixed.instant(),
            layout = "1,2,3,0",
            previewMode = false,
        )
        attempts.update { it + (quizId to existing + attempt) }
        return attempt.id
    }

    override suspend fun loadAttemptPage(accountId: AccountId, attemptId: AttemptId, page: Int): AttemptPage =
        page(attemptId, review = false)

    override suspend fun saveDraft(accountId: AccountId, attemptId: AttemptId, answers: List<AttemptAnswer>) {
        drafts.update { it + (attemptId to answers) }
    }

    override suspend fun submitAttempt(
        accountId: AccountId,
        attemptId: AttemptId,
        answers: List<AttemptAnswer>,
        timeUp: Boolean,
    ) {
        attempts.update { all ->
            all.mapValues { (_, list) ->
                list.map {
                    if (it.id != attemptId) it
                    else it.copy(state = AttemptState.Finished, sumGrades = 2.0, timeFinish = DemoClock.fixed.instant())
                }
            }
        }
    }

    override suspend fun loadReview(accountId: AccountId, attemptId: AttemptId): AttemptReview = AttemptReview(
        attemptId = attemptId,
        pages = listOf(page(attemptId, review = true)),
        sumGrades = 2.0,
        maxGrade = QUESTIONS.toDouble(),
        gradeFormatted = "20,00",
        feedback = "Ripassa gli argomenti delle domande sbagliate prima della prova scritta.",
    )

    override suspend fun clearForAccount(accountId: AccountId) = Unit

    private fun quiz(course: DemoCourse) = Quiz(
        id = QuizId(DemoElearning.quizId(course)),
        courseId = CourseId(course.moodleCourseId),
        cmId = DemoElearning.cmId(course, 91),
        name = "Quiz di autovalutazione",
        intro = "<p>Tre domande a risposta multipla sugli argomenti delle ultime settimane. " +
            "Il voto non concorre alla valutazione finale.</p>",
        timeOpen = DemoClock.instant(DemoClock.now.minusDays(10)),
        timeClose = DemoElearning.quizClosesAt(course),
        timeLimitSeconds = 15 * 60,
        gracePeriodSeconds = null,
        maxAttempts = 3,
        passGrade = 18.0,
        sumGrades = QUESTIONS.toDouble(),
        maxGrade = MAX_GRADE,
        preferredBehaviour = "deferredfeedback",
        reviewBeforeBitmask = null,
        reviewAfterBitmask = null,
    )

    private fun finishedAttempt(quizId: QuizId) = QuizAttempt(
        id = AttemptId(quizId.value * 10 + 1),
        quizId = quizId,
        userId = DemoStudent.account.learning.lmsUserId,
        attemptNumber = 1,
        state = AttemptState.Finished,
        sumGrades = 2.0,
        timeStart = DemoClock.instant(DemoClock.now.minusDays(3).withHour(21).withMinute(4)),
        timeFinish = DemoClock.instant(DemoClock.now.minusDays(3).withHour(21).withMinute(15)),
        timeModified = DemoClock.instant(DemoClock.now.minusDays(3).withHour(21).withMinute(15)),
        layout = "1,2,3,0",
        previewMode = false,
    )

    private fun page(attemptId: AttemptId, review: Boolean) = AttemptPage(
        attemptId = attemptId,
        pageIndex = 0,
        nextPage = null,
        questions = questions.mapIndexed { index, question ->
            val slot = index + 1
            val correct = slot != 2
            AttemptQuestion(
                slot = slot,
                type = "multichoice",
                pageIndex = 0,
                // Reviewed attempts show the right option picked, except on the question graded wrong.
                html = question.html(attemptId, slot, review, picked = if (correct) question.answer else question.answer - 1),
                state = if (!review) "todo" else if (correct) "gradedright" else "gradedwrong",
                mark = if (!review) null else if (correct) 1.0 else 0.0,
                maxMark = 1.0,
                flagged = false,
                sequenceCheck = "1",
            )
        },
    )

    private data class Question(val prompt: String, val options: List<String>, val answer: Int) {
        fun html(attemptId: AttemptId, slot: Int, review: Boolean, picked: Int): String {
            val name = "q${attemptId.value}:${slot}_"
            val rows = options.mapIndexed { index, option ->
                val checked = if (review && index == picked) " checked=\"checked\"" else ""
                "<div class=\"r${index % 2}\"><input type=\"radio\" name=\"${name}answer\" value=\"$index\" " +
                    "id=\"${name}answer$index\"$checked><label for=\"${name}answer$index\">$option</label></div>"
            }.joinToString("")
            val outcome = if (review) "<div class=\"rightanswer\">La risposta corretta è: ${options[answer]}</div>" else ""
            return "<div class=\"que multichoice\"><div class=\"formulation\">" +
                "<input type=\"hidden\" name=\"${name}:sequencecheck\" value=\"1\">" +
                "<div class=\"qtext\"><p>$prompt</p></div>" +
                "<div class=\"ablock\"><div class=\"answer\">$rows</div></div></div>$outcome</div>"
        }
    }

    private val questions = listOf(
        Question(
            "Qual è la complessità nel caso peggiore della ricerca in un albero binario di ricerca bilanciato?",
            listOf("O(1)", "O(log n)", "O(n)", "O(n log n)"),
            answer = 1,
        ),
        Question(
            "Quale forma normale elimina le dipendenze transitive dagli attributi non chiave?",
            listOf("Prima forma normale", "Seconda forma normale", "Terza forma normale", "Nessuna delle precedenti"),
            answer = 2,
        ),
        Question(
            "Quale algoritmo di scheduling può causare starvation dei processi lunghi?",
            listOf("Round Robin", "First Come First Served", "Shortest Job First", "Nessuno dei precedenti"),
            answer = 2,
        ),
    )

    private companion object {
        const val QUESTIONS = 3
        const val MAX_GRADE = 30.0
    }
}
