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
        preferredBehaviour = preferredBehaviour,
        reviewBeforeBitmask = null,
        reviewAfterBitmask = null,
    )

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
        preferredBehaviour = preferredBehaviour,
        reviewBeforeBitmask = reviewBeforeBitmask,
        reviewAfterBitmask = reviewAfterBitmask,
    )

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

internal fun ElearningGetUserBestGradeResponse.toEntity(
    accountId: AccountId,
    quizId: QuizId,
): QuizBestGradeEntity =
    QuizBestGradeEntity(
        accountId = accountId.value,
        quizId = quizId.value,
        grade = grade,
        maxGrade = gradeToPass,
    )

internal fun QuizBestGradeEntity.toDomain(): BestGrade =
    BestGrade(quizId = QuizId(quizId), grade = grade, maxGrade = maxGrade)

internal fun ElearningGetAttemptDataResponse.toDomain(attemptId: AttemptId, page: Int): AttemptPage =
    AttemptPage(
        attemptId = attemptId,
        pageIndex = page,
        nextPage = nextPage?.takeIf { it >= 0 },
        questions = questions.map { it.toDomain(page) },
    )

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

internal fun AttemptAnswer.toEntity(accountId: AccountId, attemptId: AttemptId): QuizAttemptAnswerEntity =
    QuizAttemptAnswerEntity(
        accountId = accountId.value,
        attemptId = attemptId.value,
        slot = slot,
        fieldsJson = ElearningJson.encodeToString(fields),
    )

internal fun QuizAttemptAnswerEntity.toDomain(): AttemptAnswer {
    val fields = runCatching {
        ElearningJson.decodeFromString(
            MapSerializer(String.serializer(), String.serializer()),
            fieldsJson,
        )
    }.getOrDefault(emptyMap())
    return AttemptAnswer(slot = slot, fields = fields)
}

private fun Long?.toMillisOrNullSec(): Long? = this?.takeIf { it > 0 }?.let { it * 1000L }
