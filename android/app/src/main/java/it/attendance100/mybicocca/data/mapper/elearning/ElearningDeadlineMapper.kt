package it.attendance100.mybicocca.data.mapper.elearning

import it.attendance100.mybicocca.data.local.elearning.deadline.DeadlineEntity
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCalendarEvent
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.deadline.Deadline
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import java.time.Instant

/**
 * Maps one event of the Moodle calendar web service into a deadline cache row, or null
 * for events that aren't course-bound assignment/quiz deadlines. Despite the name, the
 * exported `instance` is the course-module id; the course repository rewrites it to
 * the real module instance id via batched assignment/quiz lookups before persisting.
 * The due time prefers `timesort` over `timestart` and is normalized from epoch
 * seconds to milliseconds.
 */
internal fun ElearningCalendarEvent.toDeadlineEntity(accountId: AccountId): DeadlineEntity? {
    val courseId = course?.id ?: return null
    val cmId = instance ?: return null
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
        instanceId = cmId,
        title = name,
        dueAtMs = dueAtSec * 1000L,
    )
}

/**
 * Maps a cached deadline row to the matching domain variant by its kind discriminator,
 * or null for an unrecognized kind.
 */
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
