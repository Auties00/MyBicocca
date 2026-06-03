package it.attendance100.mybicocca.domain.usecase.search

import it.attendance100.mybicocca.core.search.MatchInput
import it.attendance100.mybicocca.core.search.SearchMatcher
import it.attendance100.mybicocca.core.search.normalizeForSearch
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.model.map.MapBuilding
import it.attendance100.mybicocca.domain.model.search.SearchResult
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.domain.repository.CalendarRepository
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import it.attendance100.mybicocca.domain.repository.MapRepository
import it.attendance100.mybicocca.domain.repository.SearchHistoryRepository
import it.attendance100.mybicocca.domain.repository.TranscriptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

// Ranks pages + cached content (courses, upcoming events, buildings, transcript rows)
// against the live query. No threading inside — the ViewModel applies flowOn(Default).
class GlobalSearchUseCase @Inject constructor(
    private val searchDestinations: SearchDestinationsUseCase,
    private val courseRepository: ElearningCourseRepository,
    private val calendarRepository: CalendarRepository,
    private val mapRepository: MapRepository,
    private val transcriptRepository: TranscriptRepository,
    private val historyRepository: SearchHistoryRepository,
) {

    operator fun invoke(
        query: Flow<String>,
        accountId: AccountId,
        careerId: CareerId,
    ): Flow<List<SearchResult>> {
        val sources = combine(
            courseRepository.observeEnrolledCourses(accountId),
            calendarRepository.observeUpcoming(careerId, LocalDate.now(), UPCOMING_WINDOW),
            mapRepository.observeBuildings(),
            transcriptRepository.observeRows(careerId),
            historyRepository.observeHistory(accountId),
        ) { courses, events, buildings, rows, history ->
            Sources(
                courses = courses.valueOrNull().orEmpty(),
                events = events.valueOrNull().orEmpty(),
                buildings = buildings.valueOrNull().orEmpty(),
                rows = rows.valueOrNull().orEmpty(),
                recentQueries = history.map { normalizeForSearch(it.query) }.toSet(),
            )
        }
        return combine(query, sources) { q, s -> rank(q.trim(), s) }
    }

    private fun rank(query: String, sources: Sources): List<SearchResult> {
        if (query.isEmpty()) return emptyList()
        val groups = listOf(
            searchDestinations(query),
            sources.courses.mapNotNull { it.toResult(query) },
            // Recurring lessons of the same course collapse to the soonest occurrence.
            sources.events
                .distinctBy { normalizeForSearch(it.title) to (it is CalendarEvent.Exam) }
                .mapNotNull { it.toResult(query) },
            sources.buildings.mapNotNull { it.toResult(query) },
            sources.rows.mapNotNull { it.toResult(query) },
        )
        return groups
            .flatMap { group ->
                group
                    .map { it.withRecencyBoost(sources.recentQueries) }
                    .sortedByDescending { it.score }
                    .take(MAX_PER_CATEGORY)
            }
            .sortedWith(compareByDescending<SearchResult> { it.score }.thenBy { it.category.priority })
            .take(MAX_RESULTS)
    }

    private fun EnrolledCourse.toResult(query: String): SearchResult.Course? {
        val title = displayName.ifBlank { fullName }
        val aliases = listOf(shortName, fullName, idNumber.orEmpty()).filter { it.isNotBlank() }
        val score = SearchMatcher.score(query, MatchInput(title, aliases)) ?: return null
        val subtitle = shortName.takeIf { it.isNotBlank() && it != title }
        return SearchResult.Course(id, title, subtitle, score)
    }

    private fun CalendarEvent.toResult(query: String): SearchResult.CalendarEntry? {
        val people = when (this) {
            is CalendarEvent.Lesson -> teachers
            is CalendarEvent.Exam -> examiners
        }
        val codes = listOfNotNull(shortLabel, (this as? CalendarEvent.Lesson)?.subjectCode)
        val score = SearchMatcher.score(query, MatchInput(title, codes + people)) ?: return null
        val place = location?.room ?: location?.building
        val subtitle = listOfNotNull(date.format(EventDateFormat), place).joinToString(" · ")
        return SearchResult.CalendarEntry(id, this is CalendarEvent.Exam, date, title, subtitle, score)
    }

    private fun MapBuilding.toResult(query: String): SearchResult.Building? {
        val aliases = listOfNotNull(code.value, address).filter { it.isNotBlank() }
        val score = SearchMatcher.score(query, MatchInput(name, aliases)) ?: return null
        return SearchResult.Building(code, name, address, score)
    }

    private fun TranscriptRow.toResult(query: String): SearchResult.TranscriptEntry? {
        val aliases = listOfNotNull(activityCode).filter { it.isNotBlank() }
        val score = SearchMatcher.score(query, MatchInput(activityName, aliases)) ?: return null
        val gradeLabel = grade?.let { "Voto $it${if (cumLaude) "L" else ""}" }
        val subtitle = listOfNotNull(activityCode, gradeLabel).joinToString(" · ").ifBlank { null }
        return SearchResult.TranscriptEntry(id, activityName, subtitle, score)
    }

    // Small nudge for things the user searched before, never enough to jump a whole tier.
    private fun SearchResult.withRecencyBoost(recentQueries: Set<String>): SearchResult {
        if (normalizeForSearch(title) !in recentQueries) return this
        val boosted = (score + RECENCY_BOOST).coerceAtMost(1.0)
        return when (this) {
            is SearchResult.Destination -> copy(score = boosted)
            is SearchResult.Course -> copy(score = boosted)
            is SearchResult.CalendarEntry -> copy(score = boosted)
            is SearchResult.Building -> copy(score = boosted)
            is SearchResult.TranscriptEntry -> copy(score = boosted)
        }
    }

    private data class Sources(
        val courses: List<EnrolledCourse>,
        val events: List<CalendarEvent>,
        val buildings: List<MapBuilding>,
        val rows: List<TranscriptRow>,
        val recentQueries: Set<String>,
    )

    private companion object {
        const val UPCOMING_WINDOW = 100
        const val MAX_PER_CATEGORY = 8
        const val MAX_RESULTS = 30
        const val RECENCY_BOOST = 0.05
        val EventDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ITALIAN)
    }
}
