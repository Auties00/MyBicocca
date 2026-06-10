package it.attendance100.mybicocca.data.mapper.elearning

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.elearning.deadline.DeadlineEntity
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCalendarEvent
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningEventCourse
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.deadline.Deadline
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import org.junit.Test
import java.time.Instant

/**
 * Covers the Moodle calendar-event -> deadline-entity mapping: only course-bound
 * assignment/quiz events survive, the cmid stored in `instance` becomes instanceId, the
 * due time prefers timesort over timestart and normalizes seconds to milliseconds, and the
 * entity -> domain dispatch selects the right Deadline variant by kind.
 */
class ElearningDeadlineMapperTest {

    private val account = AccountId("acc-1")

    private fun event(
        id: Int = 42,
        courseId: Int? = 5,
        instance: Int? = 88,
        moduleName: String? = "assign",
        name: String = "Compito 1",
        timeStart: Long = 1_000L,
        timeSort: Long? = 2_000L,
    ) = ElearningCalendarEvent(
        id = id,
        name = name,
        moduleName = moduleName,
        instance = instance,
        timeStart = timeStart,
        timeSort = timeSort,
        course = courseId?.let { ElearningEventCourse(id = it) },
    )

    @Test
    fun `assignment event maps to an ASSIGNMENT row`() {
        val entity = event(moduleName = "assign").toDeadlineEntity(account)
        assertThat(entity).isNotNull()
        requireNotNull(entity)
        assertThat(entity.kind).isEqualTo(DeadlineEntity.Kind.ASSIGNMENT)
        assertThat(entity.accountId).isEqualTo("acc-1")
        assertThat(entity.eventId).isEqualTo(42)
        assertThat(entity.courseId).isEqualTo(5)
        assertThat(entity.instanceId).isEqualTo(88)
        assertThat(entity.title).isEqualTo("Compito 1")
    }

    @Test
    fun `quiz event maps to a QUIZ row`() {
        val entity = event(moduleName = "quiz").toDeadlineEntity(account)
        assertThat(entity?.kind).isEqualTo(DeadlineEntity.Kind.QUIZ)
    }

    @Test
    fun `module name is matched case-insensitively`() {
        assertThat(event(moduleName = "ASSIGN").toDeadlineEntity(account)?.kind)
            .isEqualTo(DeadlineEntity.Kind.ASSIGNMENT)
    }

    @Test
    fun `event without a course is dropped`() {
        assertThat(event(courseId = null).toDeadlineEntity(account)).isNull()
    }

    @Test
    fun `event without an instance is dropped`() {
        assertThat(event(instance = null).toDeadlineEntity(account)).isNull()
    }

    @Test
    fun `event without a module name is dropped`() {
        assertThat(event(moduleName = null).toDeadlineEntity(account)).isNull()
    }

    @Test
    fun `event of an unrelated module kind is dropped`() {
        assertThat(event(moduleName = "forum").toDeadlineEntity(account)).isNull()
    }

    @Test
    fun `due time prefers timesort and normalizes seconds to milliseconds`() {
        val entity = event(timeStart = 1_000L, timeSort = 2_000L).toDeadlineEntity(account)
        assertThat(entity?.dueAtMs).isEqualTo(2_000_000L)
    }

    @Test
    fun `due time falls back to timestart when timesort is absent`() {
        val entity = event(timeStart = 1_000L, timeSort = null).toDeadlineEntity(account)
        assertThat(entity?.dueAtMs).isEqualTo(1_000_000L)
    }

    @Test
    fun `assignment row toDomain builds a Deadline Assignment`() {
        val entity = DeadlineEntity(
            accountId = "acc-1",
            eventId = 42,
            courseId = 5,
            kind = DeadlineEntity.Kind.ASSIGNMENT,
            instanceId = 88,
            title = "Compito 1",
            dueAtMs = 2_000_000L,
        )
        val domain = entity.toDomain()
        assertThat(domain).isInstanceOf(Deadline.Assignment::class.java)
        domain as Deadline.Assignment
        assertThat(domain.id).isEqualTo(AssignmentId(88))
        assertThat(domain.courseId).isEqualTo(CourseId(5))
        assertThat(domain.title).isEqualTo("Compito 1")
        assertThat(domain.dueAt).isEqualTo(Instant.ofEpochMilli(2_000_000L))
    }

    @Test
    fun `quiz row toDomain builds a Deadline Quiz`() {
        val entity = DeadlineEntity(
            accountId = "acc-1",
            eventId = 43,
            courseId = 5,
            kind = DeadlineEntity.Kind.QUIZ,
            instanceId = 99,
            title = "Quiz finale",
            dueAtMs = 3_000_000L,
        )
        val domain = entity.toDomain()
        assertThat(domain).isInstanceOf(Deadline.Quiz::class.java)
        assertThat((domain as Deadline.Quiz).id).isEqualTo(QuizId(99))
    }

    @Test
    fun `row with an unknown kind toDomain returns null`() {
        val entity = DeadlineEntity(
            accountId = "acc-1",
            eventId = 44,
            courseId = 5,
            kind = "wiki",
            instanceId = 1,
            title = "x",
            dueAtMs = 1L,
        )
        assertThat(entity.toDomain()).isNull()
    }
}
