package it.attendance100.mybicocca.data.mapper.elearning

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.elearning.quiz.QuizAttemptAnswerEntity
import it.attendance100.mybicocca.data.local.elearning.quiz.QuizAttemptEntity
import it.attendance100.mybicocca.data.local.elearning.quiz.QuizBestGradeEntity
import it.attendance100.mybicocca.data.local.elearning.quiz.QuizEntity
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetAttemptDataResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetAttemptReviewResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetUserBestGradeResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningQuiz
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningQuizAttempt
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningQuizQuestion
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptAnswer
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptState
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import org.junit.Test
import java.time.Instant

/**
 * Covers the mod_quiz mappers: quiz/attempt DTO -> entity normalization (epoch seconds,
 * absent-defaults, raw layout, preview flag, the unknown-state sentinel), best-grade
 * ceiling injection, the -1 next-page end marker, review page regrouping, question
 * rendering (mark parse, sequence-check round-trip), and the draft-answer JSON round-trip.
 */
class ElearningQuizMapperTest {

    private val account = AccountId("acc-1")

    private fun quiz(
        id: Int = 10,
        courseId: Int = 5,
        courseModuleId: Int = 77,
        timeLimitSeconds: Int? = 3600,
        gracePeriodSeconds: Int? = 600,
        sumGrades: Double? = 10.0,
        maximumGrade: Double? = 30.0,
        openTimestamp: Long? = 1_000L,
        closeTimestamp: Long? = 2_000L,
    ) = ElearningQuiz(
        id = id,
        courseId = courseId,
        courseModuleId = courseModuleId,
        name = "Quiz 1",
        introduction = "intro",
        openTimestamp = openTimestamp,
        closeTimestamp = closeTimestamp,
        timeLimitSeconds = timeLimitSeconds,
        gracePeriodSeconds = gracePeriodSeconds,
        maximumAttempts = 3,
        sumGrades = sumGrades,
        maximumGrade = maximumGrade,
        preferredBehaviour = "deferredfeedback",
    )

    @Test
    fun `quiz toEntity normalizes timestamps and widens time fields to long`() {
        val entity = quiz().toEntity(account)
        assertThat(entity.accountId).isEqualTo("acc-1")
        assertThat(entity.quizId).isEqualTo(10)
        assertThat(entity.courseId).isEqualTo(5)
        assertThat(entity.cmId).isEqualTo(77)
        assertThat(entity.timeOpenMs).isEqualTo(1_000_000L)
        assertThat(entity.timeCloseMs).isEqualTo(2_000_000L)
        assertThat(entity.timeLimitSeconds).isEqualTo(3600L)
        assertThat(entity.gracePeriodSeconds).isEqualTo(600L)
        assertThat(entity.preferredBehaviour).isEqualTo("deferredfeedback")
    }

    @Test
    fun `quiz toEntity leaves pass grade and review bitmasks null`() {
        val entity = quiz().toEntity(account)
        assertThat(entity.passGrade).isNull()
        assertThat(entity.reviewBeforeBitmask).isNull()
        assertThat(entity.reviewAfterBitmask).isNull()
    }

    @Test
    fun `quiz toEntity reads a zero open timestamp as absent`() {
        assertThat(quiz(openTimestamp = 0L).toEntity(account).timeOpenMs).isNull()
    }

    @Test
    fun `quiz toEntity leaves time limit null when untimed`() {
        assertThat(quiz(timeLimitSeconds = null).toEntity(account).timeLimitSeconds).isNull()
    }

    @Test
    fun `quiz entity toDomain wraps the ids and restores instants`() {
        val entity = QuizEntity(
            accountId = "acc-1",
            quizId = 10,
            courseId = 5,
            cmId = 77,
            name = "Quiz 1",
            intro = "intro",
            timeOpenMs = 1_000_000L,
            timeCloseMs = null,
            timeLimitSeconds = 3600L,
            gracePeriodSeconds = null,
            maxAttempts = 3,
            passGrade = 18.0,
            sumGrades = 10.0,
            maxGrade = 30.0,
            preferredBehaviour = "deferredfeedback",
            reviewBeforeBitmask = 7,
            reviewAfterBitmask = 256,
        )
        val domain = entity.toDomain()
        assertThat(domain.id).isEqualTo(QuizId(10))
        assertThat(domain.courseId).isEqualTo(CourseId(5))
        assertThat(domain.timeOpen).isEqualTo(Instant.ofEpochMilli(1_000_000L))
        assertThat(domain.timeClose).isNull()
        assertThat(domain.passGrade).isEqualTo(18.0)
        assertThat(domain.reviewBeforeBitmask).isEqualTo(7)
    }

    private fun attempt(
        id: Int = 100,
        quizId: Int? = 10,
        userId: Int? = 7,
        attemptNumber: Int? = 1,
        state: String? = "inprogress",
        layout: String? = "1,2,0,3,0",
        isPreview: Int? = 0,
        startTimestamp: Long? = 1_000L,
    ) = ElearningQuizAttempt(
        id = id,
        quizId = quizId,
        userId = userId,
        attemptNumber = attemptNumber,
        state = state,
        layout = layout,
        isPreview = isPreview,
        startTimestamp = startTimestamp,
        sumGrades = 8.0,
    )

    @Test
    fun `attempt toEntity stores the raw layout and preview flag`() {
        val entity = attempt(layout = "1,2,0,3,0", isPreview = 1).toEntity(account)
        assertThat(entity.layout).isEqualTo("1,2,0,3,0")
        assertThat(entity.previewMode).isTrue()
        assertThat(entity.stateRaw).isEqualTo("inprogress")
        assertThat(entity.timeStartMs).isEqualTo(1_000_000L)
    }

    @Test
    fun `attempt toEntity defaults absent numeric fields to zero`() {
        val entity = attempt(quizId = null, userId = null, attemptNumber = null).toEntity(account)
        assertThat(entity.quizId).isEqualTo(0)
        assertThat(entity.userId).isEqualTo(0)
        assertThat(entity.attemptNumber).isEqualTo(0)
    }

    @Test
    fun `attempt toEntity falls back to the unknown state sentinel`() {
        assertThat(attempt(state = null).toEntity(account).stateRaw).isEqualTo("unknown")
    }

    @Test
    fun `attempt entity toDomain resolves the state from its raw value`() {
        val entity = QuizAttemptEntity(
            accountId = "acc-1",
            attemptId = 100,
            quizId = 10,
            userId = 7,
            attemptNumber = 1,
            stateRaw = "finished",
            sumGrades = 8.0,
            timeStartMs = 1_000_000L,
            timeFinishMs = 2_000_000L,
            timeModifiedMs = null,
            layout = "1,2,0",
            previewMode = false,
        )
        val domain = entity.toDomain()
        assertThat(domain.id).isEqualTo(AttemptId(100))
        assertThat(domain.quizId).isEqualTo(QuizId(10))
        assertThat(domain.state).isEqualTo(AttemptState.Finished)
        assertThat(domain.timeStart).isEqualTo(Instant.ofEpochMilli(1_000_000L))
        assertThat(domain.timeModified).isNull()
    }

    @Test
    fun `attempt entity toDomain maps an unrecognized raw state to unknown`() {
        val entity = QuizAttemptEntity(
            accountId = "acc-1",
            attemptId = 1,
            quizId = 1,
            userId = 1,
            attemptNumber = 1,
            stateRaw = "weird",
            sumGrades = null,
            timeStartMs = null,
            timeFinishMs = null,
            timeModifiedMs = null,
            layout = null,
            previewMode = false,
        )
        assertThat(entity.toDomain().state).isEqualTo(AttemptState.Unknown)
    }

    @Test
    fun `best grade response toEntity injects the quiz max grade as the ceiling`() {
        val response = ElearningGetUserBestGradeResponse(hasGrade = true, grade = 27.0, gradeToPass = 18.0)
        val entity = response.toEntity(account, QuizId(10), quizMaxGrade = 30.0)
        assertThat(entity.quizId).isEqualTo(10)
        assertThat(entity.grade).isEqualTo(27.0)
        assertThat(entity.maxGrade).isEqualTo(30.0)
    }

    @Test
    fun `best grade entity toDomain wraps the quiz id`() {
        val entity = QuizBestGradeEntity(accountId = "acc-1", quizId = 10, grade = 27.0, maxGrade = 30.0)
        val domain = entity.toDomain()
        assertThat(domain.quizId).isEqualTo(QuizId(10))
        assertThat(domain.grade).isEqualTo(27.0)
        assertThat(domain.maxGrade).isEqualTo(30.0)
    }

    private fun question(slot: Int, page: Int?, type: String? = "multichoice") = ElearningQuizQuestion(
        slot = slot,
        type = type,
        page = page,
        html = "<div>Q$slot</div>",
        state = "todo",
        mark = "1.5",
        maximumMark = 2.0,
        flagged = false,
        sequenceCheck = 3,
    )

    @Test
    fun `attempt data toDomain reads the minus one next-page marker as the end`() {
        val response = ElearningGetAttemptDataResponse(
            attempt = attempt(),
            nextPage = -1,
            questions = listOf(question(slot = 1, page = 0)),
        )
        val page = response.toDomain(AttemptId(100), page = 0)
        assertThat(page.attemptId).isEqualTo(AttemptId(100))
        assertThat(page.pageIndex).isEqualTo(0)
        assertThat(page.nextPage).isNull()
        assertThat(page.questions).hasSize(1)
    }

    @Test
    fun `attempt data toDomain keeps a non-negative next page`() {
        val response = ElearningGetAttemptDataResponse(
            attempt = attempt(),
            nextPage = 2,
            questions = emptyList(),
        )
        assertThat(response.toDomain(AttemptId(100), page = 1).nextPage).isEqualTo(2)
    }

    @Test
    fun `question toDomain parses the mark, keeps sequence-check as string, defaults type`() {
        val response = ElearningGetAttemptDataResponse(
            attempt = attempt(),
            nextPage = -1,
            questions = listOf(question(slot = 4, page = 0, type = null)),
        )
        val q = response.toDomain(AttemptId(100), page = 0).questions.single()
        assertThat(q.slot).isEqualTo(4)
        assertThat(q.type).isEqualTo("unknown")
        assertThat(q.pageIndex).isEqualTo(0)
        assertThat(q.mark).isEqualTo(1.5)
        assertThat(q.maxMark).isEqualTo(2.0)
        assertThat(q.sequenceCheck).isEqualTo("3")
        assertThat(q.html).isEqualTo("<div>Q4</div>")
    }

    @Test
    fun `attempt review toDomain regroups the flat question list into ordered pages`() {
        val response = ElearningGetAttemptReviewResponse(
            grade = "27,00",
            attempt = attempt().copy(sumGrades = 9.0),
            questions = listOf(
                question(slot = 1, page = 1),
                question(slot = 2, page = 0),
                question(slot = 3, page = 0),
            ),
        )
        val review = response.toDomain(AttemptId(100))
        assertThat(review.attemptId).isEqualTo(AttemptId(100))
        assertThat(review.gradeFormatted).isEqualTo("27,00")
        assertThat(review.sumGrades).isEqualTo(9.0)
        assertThat(review.maxGrade).isNull()
        assertThat(review.pages.map { it.pageIndex }).containsExactly(0, 1).inOrder()
        assertThat(review.pages.first().questions.map { it.slot }).containsExactly(2, 3).inOrder()
        assertThat(review.pages.first().nextPage).isNull()
    }

    @Test
    fun `attempt review toDomain takes overall feedback from the first additional-data block`() {
        val response = ElearningGetAttemptReviewResponse(
            grade = null,
            attempt = attempt(),
            additionalData = listOf(
                it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAdditionalData(
                    id = "feedback",
                    title = "Feedback",
                    content = "Ottimo lavoro",
                ),
            ),
            questions = listOf(question(slot = 1, page = 0)),
        )
        assertThat(response.toDomain(AttemptId(100)).feedback).isEqualTo("Ottimo lavoro")
    }

    @Test
    fun `attempt review toDomain defaults a null page index to zero`() {
        val response = ElearningGetAttemptReviewResponse(
            grade = null,
            attempt = attempt(),
            questions = listOf(question(slot = 1, page = null)),
        )
        val review = response.toDomain(AttemptId(100))
        assertThat(review.pages.single().pageIndex).isEqualTo(0)
    }

    @Test
    fun `draft answer toEntity serializes the field map`() {
        val answer = AttemptAnswer(slot = 2, fields = mapOf("q2:1_answer" to "Paris"))
        val entity = answer.toEntity(account, AttemptId(100))
        assertThat(entity.accountId).isEqualTo("acc-1")
        assertThat(entity.attemptId).isEqualTo(100)
        assertThat(entity.slot).isEqualTo(2)
        assertThat(entity.fieldsJson).contains("q2:1_answer")
        assertThat(entity.fieldsJson).contains("Paris")
    }

    @Test
    fun `draft answer round-trips through the JSON column`() {
        val original = AttemptAnswer(slot = 2, fields = mapOf("a" to "1", "b" to "2"))
        val restored = original.toEntity(account, AttemptId(100)).toDomain()
        assertThat(restored).isEqualTo(original)
    }

    @Test
    fun `draft answer entity toDomain decodes a malformed blob to an empty map`() {
        val entity = QuizAttemptAnswerEntity(
            accountId = "acc-1",
            attemptId = 100,
            slot = 2,
            fieldsJson = "{not valid json",
        )
        val domain = entity.toDomain()
        assertThat(domain.slot).isEqualTo(2)
        assertThat(domain.fields).isEmpty()
    }
}
