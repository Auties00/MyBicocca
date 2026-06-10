package it.attendance100.mybicocca.data.local.elearning.quiz

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.account.MyBicoccaDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Behaviour coverage for [QuizDao] against a real in-memory Room database (Robolectric).
 * Exercises the quiz/attempt/answer/best-grade tables: account and course scoping, the
 * null-open-time-last ordering of the course listing, attempt ordering newest-first, the
 * slot ordering of stored draft answers, the splice `replaceForCourse`/`replaceAttempts`
 * transactions (drop only the targeted scope then insert fresh rows), and the account-wide
 * clear. None of the quiz tables declares a foreign key, so rows insert without a parent
 * account.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QuizDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: QuizDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.elearningQuizDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `observeForCourse orders null open time last then by open time then name`() = runTest {
        dao.upsertQuizzes(
            listOf(
                quiz(quizId = 1, name = "Zeta", timeOpenMs = null),
                quiz(quizId = 2, name = "Alpha", timeOpenMs = null),
                quiz(quizId = 3, name = "Later", timeOpenMs = 5_000L),
                quiz(quizId = 4, name = "Earlier", timeOpenMs = 1_000L),
            ),
        )

        val ordered = dao.observeForCourse("acc-1", courseId = 100).first()

        assertThat(ordered.map { it.name })
            .containsExactly("Earlier", "Later", "Alpha", "Zeta").inOrder()
    }

    @Test
    fun `observeForCourse is scoped to account and course`() = runTest {
        dao.upsertQuizzes(
            listOf(
                quiz(quizId = 1, name = "Mine", courseId = 100, accountId = "acc-1"),
                quiz(quizId = 2, name = "OtherCourse", courseId = 200, accountId = "acc-1"),
                quiz(quizId = 3, name = "OtherAccount", courseId = 100, accountId = "acc-2"),
            ),
        )

        val rows = dao.observeForCourse("acc-1", courseId = 100).first()

        assertThat(rows.map { it.name }).containsExactly("Mine")
    }

    @Test
    fun `observe returns the single quiz and round-trips every column`() = runTest {
        dao.upsertQuizzes(listOf(quiz(quizId = 7).copy(
            sumGrades = 30.0,
            maxGrade = 31.0,
            passGrade = 18.0,
            preferredBehaviour = "deferredfeedback",
            reviewBeforeBitmask = 0x11000,
            reviewAfterBitmask = 0x10000,
        )))

        val stored = dao.observe("acc-1", quizId = 7).first()

        assertThat(stored).isNotNull()
        assertThat(stored!!.preferredBehaviour).isEqualTo("deferredfeedback")
        assertThat(stored.reviewBeforeBitmask).isEqualTo(0x11000)
        assertThat(stored.maxGrade).isEqualTo(31.0)
    }

    @Test
    fun `observeForAccount returns all courses' quizzes for the account`() = runTest {
        dao.upsertQuizzes(
            listOf(
                quiz(quizId = 1, courseId = 100),
                quiz(quizId = 2, courseId = 200),
                quiz(quizId = 3, courseId = 300, accountId = "acc-2"),
            ),
        )

        val rows = dao.observeForAccount("acc-1").first()

        assertThat(rows.map { it.quizId }).containsExactly(1, 2)
    }

    @Test
    fun `observeAttempts orders by attempt number descending and is quiz scoped`() = runTest {
        dao.upsertAttempts(
            listOf(
                attempt(attemptId = 10, quizId = 7, attemptNumber = 1),
                attempt(attemptId = 11, quizId = 7, attemptNumber = 3),
                attempt(attemptId = 12, quizId = 7, attemptNumber = 2),
                attempt(attemptId = 99, quizId = 8, attemptNumber = 5),
            ),
        )

        val attempts = dao.observeAttempts("acc-1", quizId = 7).first()

        assertThat(attempts.map { it.attemptNumber }).containsExactly(3, 2, 1).inOrder()
    }

    @Test
    fun `observeBestGrade returns the stored grade and null when absent`() = runTest {
        dao.upsertBestGrade(QuizBestGradeEntity("acc-1", quizId = 7, grade = 27.0, maxGrade = 31.0))

        val present = dao.observeBestGrade("acc-1", quizId = 7).first()
        val absent = dao.observeBestGrade("acc-1", quizId = 8).first()

        assertThat(present!!.grade).isEqualTo(27.0)
        assertThat(absent).isNull()
    }

    @Test
    fun `observeAnswers orders draft answers by slot`() = runTest {
        dao.upsertAnswers(
            listOf(
                answer(attemptId = 10, slot = 3),
                answer(attemptId = 10, slot = 1),
                answer(attemptId = 10, slot = 2),
                answer(attemptId = 99, slot = 1),
            ),
        )

        val answers = dao.observeAnswers("acc-1", attemptId = 10).first()

        assertThat(answers.map { it.slot }).containsExactly(1, 2, 3).inOrder()
    }

    @Test
    fun `replaceForCourse swaps only the target course quizzes`() = runTest {
        dao.upsertQuizzes(
            listOf(
                quiz(quizId = 1, courseId = 100),
                quiz(quizId = 2, courseId = 200),
            ),
        )

        dao.replaceForCourse("acc-1", courseId = 100, rows = listOf(quiz(quizId = 3, courseId = 100)))

        assertThat(dao.observeForCourse("acc-1", 100).first().map { it.quizId }).containsExactly(3)
        assertThat(dao.observeForCourse("acc-1", 200).first().map { it.quizId }).containsExactly(2)
    }

    @Test
    fun `replaceForCourse with empty list clears the course without re-inserting`() = runTest {
        dao.upsertQuizzes(listOf(quiz(quizId = 1, courseId = 100)))

        dao.replaceForCourse("acc-1", courseId = 100, rows = emptyList())

        assertThat(dao.observeForCourse("acc-1", 100).first()).isEmpty()
    }

    @Test
    fun `replaceAttempts swaps only the target quiz attempts`() = runTest {
        dao.upsertAttempts(
            listOf(
                attempt(attemptId = 10, quizId = 7, attemptNumber = 1),
                attempt(attemptId = 20, quizId = 8, attemptNumber = 1),
            ),
        )

        dao.replaceAttempts("acc-1", quizId = 7, rows = listOf(attempt(attemptId = 11, quizId = 7, attemptNumber = 2)))

        assertThat(dao.observeAttempts("acc-1", 7).first().map { it.attemptId }).containsExactly(11)
        assertThat(dao.observeAttempts("acc-1", 8).first().map { it.attemptId }).containsExactly(20)
    }

    @Test
    fun `deleteAnswers removes only the targeted attempt's answers`() = runTest {
        dao.upsertAnswers(
            listOf(
                answer(attemptId = 10, slot = 1),
                answer(attemptId = 11, slot = 1),
            ),
        )

        dao.deleteAnswers("acc-1", attemptId = 10)

        assertThat(dao.observeAnswers("acc-1", 10).first()).isEmpty()
        assertThat(dao.observeAnswers("acc-1", 11).first()).hasSize(1)
    }

    @Test
    fun `clearAllForAccount empties every quiz table for the account only`() = runTest {
        dao.upsertQuizzes(listOf(quiz(quizId = 1), quiz(quizId = 2, accountId = "acc-2")))
        dao.upsertAttempts(listOf(attempt(attemptId = 10, quizId = 1), attempt(attemptId = 20, quizId = 2, accountId = "acc-2")))
        dao.upsertBestGrade(QuizBestGradeEntity("acc-1", quizId = 1, grade = 20.0, maxGrade = 30.0))
        dao.upsertBestGrade(QuizBestGradeEntity("acc-2", quizId = 2, grade = 20.0, maxGrade = 30.0))
        dao.upsertAnswers(listOf(answer(attemptId = 10, slot = 1), answer(attemptId = 20, slot = 1, accountId = "acc-2")))

        dao.clearAllForAccount("acc-1")

        assertThat(dao.observeForAccount("acc-1").first()).isEmpty()
        assertThat(dao.observeAttempts("acc-1", 1).first()).isEmpty()
        assertThat(dao.observeBestGrade("acc-1", 1).first()).isNull()
        assertThat(dao.observeAnswers("acc-1", 10).first()).isEmpty()

        assertThat(dao.observeForAccount("acc-2").first()).hasSize(1)
        assertThat(dao.observeAttempts("acc-2", 2).first()).hasSize(1)
        assertThat(dao.observeBestGrade("acc-2", 2).first()).isNotNull()
        assertThat(dao.observeAnswers("acc-2", 20).first()).hasSize(1)
    }

    @Test
    fun `observe re-emits after an upsert mutates the row`() = runTest {
        dao.upsertQuizzes(listOf(quiz(quizId = 7, name = "Original")))

        dao.observe("acc-1", quizId = 7).test {
            assertThat(awaitItem()!!.name).isEqualTo("Original")

            dao.upsertQuizzes(listOf(quiz(quizId = 7, name = "Renamed")))
            assertThat(awaitItem()!!.name).isEqualTo("Renamed")
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun quiz(
        quizId: Int,
        accountId: String = "acc-1",
        courseId: Int = 100,
        name: String = "Quiz $quizId",
        timeOpenMs: Long? = null,
    ) = QuizEntity(
        accountId = accountId,
        quizId = quizId,
        courseId = courseId,
        cmId = quizId + 1_000,
        name = name,
        intro = null,
        timeOpenMs = timeOpenMs,
        timeCloseMs = null,
        timeLimitSeconds = null,
        gracePeriodSeconds = null,
        maxAttempts = 0,
        passGrade = null,
        sumGrades = null,
        maxGrade = null,
        preferredBehaviour = null,
        reviewBeforeBitmask = null,
        reviewAfterBitmask = null,
    )

    private fun attempt(
        attemptId: Int,
        quizId: Int,
        accountId: String = "acc-1",
        attemptNumber: Int = 1,
    ) = QuizAttemptEntity(
        accountId = accountId,
        attemptId = attemptId,
        quizId = quizId,
        userId = 42,
        attemptNumber = attemptNumber,
        stateRaw = "finished",
        sumGrades = 25.0,
        timeStartMs = 1_000L,
        timeFinishMs = 2_000L,
        timeModifiedMs = 2_000L,
        layout = "1,2,0,3,0",
        previewMode = false,
    )

    private fun answer(
        attemptId: Int,
        slot: Int,
        accountId: String = "acc-1",
    ) = QuizAttemptAnswerEntity(
        accountId = accountId,
        attemptId = attemptId,
        slot = slot,
        fieldsJson = """{"q$slot":"a"}""",
    )
}
