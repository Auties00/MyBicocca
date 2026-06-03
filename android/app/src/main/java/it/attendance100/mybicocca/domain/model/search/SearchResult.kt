package it.attendance100.mybicocca.domain.model.search

import it.attendance100.mybicocca.domain.model.calendar.CalendarEventId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.map.BuildingCode
import java.time.LocalDate

sealed interface SearchResult {
    val category: SearchResultCategory
    val title: String
    val subtitle: String?
    val score: Double

    data class Destination(
        val destination: SearchDestination,
        override val title: String,
        override val subtitle: String?,
        override val score: Double,
    ) : SearchResult {
        override val category get() = SearchResultCategory.Destination
    }

    data class Course(
        val courseId: CourseId,
        override val title: String,
        override val subtitle: String?,
        override val score: Double,
    ) : SearchResult {
        override val category get() = SearchResultCategory.Course
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
    }

    data class Building(
        val code: BuildingCode,
        override val title: String,
        override val subtitle: String?,
        override val score: Double,
    ) : SearchResult {
        override val category get() = SearchResultCategory.Building
    }

    data class TranscriptEntry(
        val rowId: Long,
        override val title: String,
        override val subtitle: String?,
        override val score: Double,
    ) : SearchResult {
        override val category get() = SearchResultCategory.TranscriptEntry
    }
}
