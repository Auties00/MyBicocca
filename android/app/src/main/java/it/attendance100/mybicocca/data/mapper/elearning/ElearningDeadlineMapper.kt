package it.attendance100.mybicocca.data.mapper.elearning

import it.attendance100.mybicocca.data.local.elearning.deadline.DeadlineEntity
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCalendarEvent
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.deadline.Deadline
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import java.time.Instant

internal fun ElearningCalendarEvent.toDeadlineEntity(accountId: AccountId): DeadlineEntity? {
    val courseId = course?.id ?: return null
    val instanceId = instance ?: return null
    val module = moduleName?.lowercase() ?: return null
    val kind = when (module) {
        "assign" -> DeadlineEntity.Kind.ASSIGNMENT
        "quiz" -> DeadlineEntity.Kind.QUIZ
        else -> return null
    }
    val dueAtSec = timeSort ?: timeStart
    return DeadlineEntity(
        accountId = accountId.value,
        eventId = id,
        courseId = courseId,
        kind = kind,
        instanceId = instanceId,
        title = name,
        dueAtMs = dueAtSec * 1000L,
    )
}

internal fun DeadlineEntity.toDomain(): Deadline? = when (kind) {
    DeadlineEntity.Kind.ASSIGNMENT -> Deadline.Assignment(
        id = AssignmentId(instanceId),
        courseId = CourseId(courseId),
        title = title,
        dueAt = Instant.ofEpochMilli(dueAtMs),
    )
    DeadlineEntity.Kind.QUIZ -> Deadline.Quiz(
        id = QuizId(instanceId),
        courseId = CourseId(courseId),
        title = title,
        dueAt = Instant.ofEpochMilli(dueAtMs),
    )
    else -> null
}
