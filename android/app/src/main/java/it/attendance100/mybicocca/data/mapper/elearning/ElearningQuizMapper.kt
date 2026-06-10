package it.attendance100.mybicocca.data.mapper.elearning

import it.attendance100.mybicocca.data.local.elearning.quiz.QuizAttemptAnswerEntity
import it.attendance100.mybicocca.data.local.elearning.quiz.QuizAttemptEntity
import it.attendance100.mybicocca.data.local.elearning.quiz.QuizBestGradeEntity
import it.attendance100.mybicocca.data.local.elearning.quiz.QuizEntity
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningJson
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
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptPage
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptQuestion
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptReview
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptState
import it.attendance100.mybicocca.domain.model.elearning.quiz.BestGrade
import it.attendance100.mybicocca.domain.model.elearning.quiz.Quiz
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizAttempt
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import java.time.Instant

/**
 * Maps one quiz of the mod_quiz_get_quizzes_by_courses web service into its cache row,
 * normalizing epoch-second timestamps to milliseconds. Pass grade and review bitmasks
 * are not part of the list payload and store as null.
 */
internal fun ElearningQuiz.toEntity(accountId: AccountId): QuizEntity =
    QuizEntity(
        accountId = accountId.value,
        quizId = id,
        courseId = courseId,
        cmId = courseModuleId,
        name = name,
        intro = introduction,
        timeOpenMs = openTimestamp.toMillisOrNullSec(),
        timeCloseMs = closeTimestamp.toMillisOrNullSec(),
        timeLimitSeconds = timeLimitSeconds?.toLong(),
        gracePeriodSeconds = gracePeriodSeconds?.toLong(),
        maxAttempts = maximumAttempts,
        passGrade = null,
        sumGrades = sumGrades,
        maxGrade = maximumGrade,
        preferredBehaviour = preferredBehaviour,
        reviewBeforeBitmask = null,
        reviewAfterBitmask = null,
    )

/** Maps a cached quiz row to the domain model. */
internal fun QuizEntity.toDomain(): Quiz =
    Quiz(
        id = QuizId(quizId),
        courseId = CourseId(courseId),
        cmId = cmId,
        name = name,
        intro = intro,
        timeOpen = timeOpenMs?.let(Instant::ofEpochMilli),
        timeClose = timeCloseMs?.let(Instant::ofEpochMilli),
        timeLimitSeconds = timeLimitSeconds,
        gracePeriodSeconds = gracePeriodSeconds,
        maxAttempts = maxAttempts,
        passGrade = passGrade,
        sumGrades = sumGrades,
        maxGrade = maxGrade,
        preferredBehaviour = preferredBehaviour,
        reviewBeforeBitmask = reviewBeforeBitmask,
        reviewAfterBitmask = reviewAfterBitmask,
    )

/**
 * Maps one attempt of the mod_quiz attempts web service into its cache row, defaulting
 * absent numeric fields to 0 and the state to the unknown sentinel. The raw `layout`
 * string (slot ids with ",0" page breaks) is stored verbatim.
 */
internal fun ElearningQuizAttempt.toEntity(accountId: AccountId): QuizAttemptEntity =
    QuizAttemptEntity(
        accountId = accountId.value,
        attemptId = id,
        quizId = quizId ?: 0,
        userId = userId ?: 0,
        attemptNumber = attemptNumber ?: 0,
        stateRaw = state ?: AttemptState.Unknown.raw,
        sumGrades = sumGrades,
        timeStartMs = startTimestamp.toMillisOrNullSec(),
        timeFinishMs = finishTimestamp.toMillisOrNullSec(),
        timeModifiedMs = modifiedTimestamp.toMillisOrNullSec(),
        layout = layout,
        previewMode = isPreview == 1,
    )

/** Maps a cached attempt row to the domain model, resolving the state from its raw value. */
internal fun QuizAttemptEntity.toDomain(): QuizAttempt =
    QuizAttempt(
        id = AttemptId(attemptId),
        quizId = QuizId(quizId),
        userId = userId,
        attemptNumber = attemptNumber,
        state = AttemptState.fromRaw(stateRaw),
        sumGrades = sumGrades,
        timeStart = timeStartMs?.let(Instant::ofEpochMilli),
        timeFinish = timeFinishMs?.let(Instant::ofEpochMilli),
        timeModified = timeModifiedMs?.let(Instant::ofEpochMilli),
        layout = layout,
        previewMode = previewMode,
    )

/**
 * Maps a best-grade response into its cache row for the given quiz. The display ceiling is
 * [quizMaxGrade], the quiz's own maximum from the quiz list payload — the best-grade web
 * service reports only the achieved grade and the pass threshold, and the pass threshold is
 * not a maximum.
 */
internal fun ElearningGetUserBestGradeResponse.toEntity(
    accountId: AccountId,
    quizId: QuizId,
    quizMaxGrade: Double?,
): QuizBestGradeEntity =
    QuizBestGradeEntity(
        accountId = accountId.value,
        quizId = quizId.value,
        grade = grade,
        maxGrade = quizMaxGrade,
    )

/** Maps a cached best-grade row to the domain model. */
internal fun QuizBestGradeEntity.toDomain(): BestGrade =
    BestGrade(quizId = QuizId(quizId), grade = grade, maxGrade = maxGrade)

/**
 * Maps an attempt-data response to one in-progress attempt page, reading Moodle's -1
 * next-page marker as the end of the quiz.
 */
internal fun ElearningGetAttemptDataResponse.toDomain(attemptId: AttemptId, page: Int): AttemptPage =
    AttemptPage(
        attemptId = attemptId,
        pageIndex = page,
        nextPage = nextPage?.takeIf { it >= 0 },
        questions = questions.map { it.toDomain(page) },
    )

/**
 * Maps an attempt-review response to the domain model, regrouping the flat question
 * list back into pages by each question's page index. The overall feedback comes from
 * the first additional-data block.
 */
internal fun ElearningGetAttemptReviewResponse.toDomain(attemptId: AttemptId): AttemptReview {
    val grouped = questions.groupBy { it.page ?: 0 }.toSortedMap()
    val pages = grouped.map { (pageIndex, qs) ->
        AttemptPage(
            attemptId = attemptId,
            pageIndex = pageIndex,
            nextPage = null,
            questions = qs.map { it.toDomain(pageIndex) },
        )
    }
    return AttemptReview(
        attemptId = attemptId,
        pages = pages,
        sumGrades = attempt.sumGrades,
        maxGrade = null,
        gradeFormatted = grade,
        feedback = additionalData?.firstOrNull()?.content,
    )
}

/**
 * Maps one rendered question to the domain model. The sequence check is kept as a
 * string because it must round-trip verbatim into the answer-saving calls.
 */
internal fun ElearningQuizQuestion.toDomain(pageIndex: Int): AttemptQuestion =
    AttemptQuestion(
        slot = slot,
        type = type ?: "unknown",
        pageIndex = pageIndex,
        html = html.orEmpty(),
        state = state,
        mark = mark?.toDoubleOrNull(),
        maxMark = maximumMark,
        flagged = flagged == true,
        sequenceCheck = sequenceCheck?.toString(),
    )

/**
 * Maps one slot's draft answer to its cache row, packing the form fields into the JSON
 * column so an interrupted attempt can be resumed with its answers intact.
 */
internal fun AttemptAnswer.toEntity(accountId: AccountId, attemptId: AttemptId): QuizAttemptAnswerEntity =
    QuizAttemptAnswerEntity(
        accountId = accountId.value,
        attemptId = attemptId.value,
        slot = slot,
        fieldsJson = ElearningJson.encodeToString(fields),
    )

/**
 * Maps a cached draft-answer row to the domain model, decoding the JSON field map
 * defensively to an empty map on failure.
 */
internal fun QuizAttemptAnswerEntity.toDomain(): AttemptAnswer {
    val fields = runCatching {
        ElearningJson.decodeFromString(
            MapSerializer(String.serializer(), String.serializer()),
            fieldsJson,
        )
    }.getOrDefault(emptyMap())
    return AttemptAnswer(slot = slot, fields = fields)
}

/** Converts a Moodle epoch-second timestamp to milliseconds, reading 0 as absent. */
private fun Long?.toMillisOrNullSec(): Long? = this?.takeIf { it > 0 }?.let { it * 1000L }
