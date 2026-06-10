package it.attendance100.mybicocca.domain.model.search

import it.attendance100.mybicocca.domain.model.calendar.CalendarEventId
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.model.map.RoomCode
import java.time.LocalDate

/**
 * One ranked hit of the unified search, rendered as a row of the search overlay. Hits come
 * from the static action and destination catalogs and from locally cached Room data
 * (courses, assignments, quizzes, calendar events, buildings, rooms,
 * transcript rows) — producing them never touches the network.
 *
 * @property category Coarse grouping, also the tie-break order between equal scores.
 * @property title Primary row text — the string the query was matched against.
 * @property subtitle Secondary row text (course name, date, place...), when available.
 * @property score Relevance in 0..1 as produced by the matcher, plus any boosts.
 * @property key Stable identity across sessions, used by the adaptive query-to-pick
 *   memory: tapping a result records the (query, key) pair so the same query later boosts
 *   the same result.
 */
sealed interface SearchResult {
    val category: SearchResultCategory
    val title: String
    val subtitle: String?
    val score: Double

    val key: String

    data class Action(
        val action: SearchAction,
        override val title: String,
        override val subtitle: String?,
        override val score: Double,
    ) : SearchResult {
        override val category get() = SearchResultCategory.Action
        override val key get() = "action:${action.name}"
    }

    data class Destination(
        val destination: SearchDestination,
        override val title: String,
        override val subtitle: String?,
        override val score: Double,
    ) : SearchResult {
        override val category get() = SearchResultCategory.Destination
        override val key get() = "dest:${destination.name}"
    }

    data class Course(
        val courseId: CourseId,
        override val title: String,
        override val subtitle: String?,
        override val score: Double,
    ) : SearchResult {
        override val category get() = SearchResultCategory.Course
        override val key get() = "course:${courseId.value}"
    }

    data class Assignment(
        val courseId: CourseId,
        val assignmentId: AssignmentId,
        override val title: String,
        override val subtitle: String?,
        override val score: Double,
    ) : SearchResult {
        override val category get() = SearchResultCategory.Assignment
        override val key get() = "assignment:${assignmentId.value}"
    }

    data class Quiz(
        val courseId: CourseId,
        val quizId: QuizId,
        override val title: String,
        override val subtitle: String?,
        override val score: Double,
    ) : SearchResult {
        override val category get() = SearchResultCategory.Quiz
        override val key get() = "quiz:${quizId.value}"
    }

    data class CalendarEntry(
        val eventId: CalendarEventId,
        val isExam: Boolean,
        val date: LocalDate,
        override val title: String,
        override val subtitle: String?,
        override val score: Double,
    ) : SearchResult {
        override val category get() = SearchResultCategory.CalendarEvent
        override val key get() = "event:${eventId.value}"
    }

    /** A recognized date expression ("domani", "lunedì", "22/06") — lands on that day. */
    data class CalendarDay(
        val date: LocalDate,
        override val title: String,
        override val subtitle: String?,
        override val score: Double,
    ) : SearchResult {
        override val category get() = SearchResultCategory.CalendarEvent
        override val key get() = "day"
    }

    data class Building(
        val code: BuildingCode,
        override val title: String,
        override val subtitle: String?,
        override val score: Double,
    ) : SearchResult {
        override val category get() = SearchResultCategory.Place
        override val key get() = "building:${code.value}"
    }

    data class Room(
        val buildingCode: BuildingCode,
        val roomCode: RoomCode,
        override val title: String,
        override val subtitle: String?,
        override val score: Double,
    ) : SearchResult {
        override val category get() = SearchResultCategory.Place
        override val key get() = "room:${roomCode.value}"
    }

    /**
     * A matched row of the student transcript.
     *
     * @property rowId Local persistence identity of the transcript row.
     */
    data class TranscriptEntry(
        val rowId: Long,
        override val title: String,
        override val subtitle: String?,
        override val score: Double,
    ) : SearchResult {
        override val category get() = SearchResultCategory.TranscriptEntry
        override val key get() = "transcript:$rowId"
    }
}
