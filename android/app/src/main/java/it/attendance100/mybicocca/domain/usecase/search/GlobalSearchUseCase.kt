package it.attendance100.mybicocca.domain.usecase.search

import it.attendance100.mybicocca.core.search.MatchInput
import it.attendance100.mybicocca.core.search.SearchMatcher
import it.attendance100.mybicocca.core.search.normalizeForSearch
import it.attendance100.mybicocca.core.search.parseDateQuery
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.model.elearning.quiz.Quiz
import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.model.map.MapBuilding
import it.attendance100.mybicocca.domain.model.map.MapRoom
import it.attendance100.mybicocca.domain.model.search.RelativeDay
import it.attendance100.mybicocca.domain.model.search.SearchHistoryEntry
import it.attendance100.mybicocca.domain.model.search.SearchResult
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.domain.repository.CalendarRepository
import it.attendance100.mybicocca.domain.repository.ElearningAssignmentRepository
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import it.attendance100.mybicocca.domain.repository.ElearningQuizRepository
import it.attendance100.mybicocca.domain.repository.MapRepository
import it.attendance100.mybicocca.domain.repository.SearchHistoryRepository
import it.attendance100.mybicocca.domain.repository.TranscriptRepository
import it.attendance100.mybicocca.domain.usecase.search.GlobalSearchUseCase.Companion.MAX_RESULTS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/**
 * Streams ranked search results for a live query: pages, actions and cached content —
 * courses, assignments, quizzes, upcoming calendar events, buildings, rooms,
 * transcript rows. Everything is served from Room and static catalogs, so
 * searching never touches the network. No threading inside — the ViewModel applies
 * flowOn(Default).
 *
 * Ranking, per query emission:
 * - each source group is scored through [SearchMatcher], sorted, and capped per category
 *   so no single group floods the list
 * - a recognized date expression adds a jump-to-day calendar result
 * - boosts apply on top of the matcher score: a small recency nudge for titles the user
 *   searched before, and a pick-memory boost large enough to jump tiers
 * - the merged list is ordered by boosted score, tie-broken by category priority, and
 *   truncated to [MAX_RESULTS]
 */
class GlobalSearchUseCase @Inject constructor(
    private val searchDestinations: SearchDestinationsUseCase,
    private val searchActions: SearchActionsUseCase,
    private val courseRepository: ElearningCourseRepository,
    private val calendarRepository: CalendarRepository,
    private val mapRepository: MapRepository,
    private val transcriptRepository: TranscriptRepository,
    private val assignmentRepository: ElearningAssignmentRepository,
    private val quizRepository: ElearningQuizRepository,
    private val historyRepository: SearchHistoryRepository,
) {

    /**
     * Re-ranks on every change of the query or of any cached source. The sources are split
     * into two halves because [combine] is typed up to five flows.
     */
    operator fun invoke(
        query: Flow<String>,
        accountId: AccountId,
        careerId: CareerId,
    ): Flow<List<SearchResult>> {
        val cachedContent = combine(
            courseRepository.observeEnrolledCourses(accountId),
            calendarRepository.observeUpcoming(careerId, LocalDate.now(), UPCOMING_WINDOW),
            mapRepository.observeBuildings(),
            transcriptRepository.observeRows(careerId),
            historyRepository.observeHistory(accountId),
        ) { courses, events, buildings, rows, history ->
            PrimarySources(
                courses = courses.valueOrNull().orEmpty(),
                events = events.valueOrNull().orEmpty(),
                buildings = buildings.valueOrNull().orEmpty(),
                rows = rows.valueOrNull().orEmpty(),
                history = history,
            )
        }
        val deepContent = combine(
            assignmentRepository.observeAllForAccount(accountId),
            quizRepository.observeAllForAccount(accountId),
            mapRepository.observeAllRooms(),
        ) { assignments, quizzes, rooms ->
            DeepSources(
                assignments = assignments,
                quizzes = quizzes,
                rooms = rooms,
            )
        }
        val sources = combine(cachedContent, deepContent) { primary, deep ->
            Sources(primary, deep)
        }
        return combine(query, sources) { q, s -> rank(q.trim(), s) }
    }

    /**
     * Recurring lessons of the same course collapse to their soonest occurrence before
     * scoring, keyed on normalized title plus the exam flag.
     */
    private fun rank(query: String, sources: Sources): List<SearchResult> {
        if (query.isEmpty()) return emptyList()
        val primary = sources.primary
        val deep = sources.deep
        val courseNames = primary.courses.associateBy(
            { it.id },
            { it.displayName.ifBlank { it.fullName } },
        )
        val buildingNames = primary.buildings.associateBy({ it.code }, { it.name })
        val eventDateFormat = DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())
        val groups = listOf(
            searchActions(query),
            searchDestinations(query),
            dateResults(query),
            primary.courses.mapNotNull { it.toResult(query) },
            primary.events
                .distinctBy { normalizeForSearch(it.title) to (it is CalendarEvent.Exam) }
                .mapNotNull { it.toResult(query, eventDateFormat) },
            deep.assignments.mapNotNull { it.toResult(query, courseNames) },
            deep.quizzes.mapNotNull { it.toResult(query, courseNames) },
            primary.buildings.mapNotNull { it.toResult(query) },
            deep.rooms.mapNotNull { it.toResult(query, buildingNames) },
            primary.rows.mapNotNull { it.toResult(query) },
        )
        val recentQueries = primary.history.map { normalizeForSearch(it.query) }.toSet()
        val pickBoosts = pickBoosts(query, primary.history)
        return groups
            .flatMap { group ->
                group
                    .map { it.boosted(recentQueries, pickBoosts) }
                    .sortedByDescending { it.score }
                    .take(MAX_PER_CATEGORY)
            }
            .sortedWith(compareByDescending<SearchResult> { it.score }.thenBy { it.category.priority })
            .take(MAX_RESULTS)
    }

    /** "domani", "lunedì", "22/06" — a result that jumps straight to that calendar day. */
    private fun dateResults(query: String): List<SearchResult> {
        val match = parseDateQuery(query, LocalDate.now(), Locale.getDefault())
            ?: return emptyList()
        return listOf(
            SearchResult.CalendarDay(
                date = match.date,
                relativeDay = match.relativeDayOffset?.let(RelativeDay::fromOffset),
                // Weekday/numeric hits rank on their date label; relative-day hits have no
                // label, so rank on the matched query, preserving the recency boost.
                title = match.label ?: query,
                score = DATE_SCORE,
            ),
        )
    }

    private fun EnrolledCourse.toResult(query: String): SearchResult.Course? {
        val title = displayName.ifBlank { fullName }
        val aliases = listOf(shortName, fullName, idNumber.orEmpty()).filter { it.isNotBlank() }
        val score = SearchMatcher.score(query, MatchInput(title, aliases)) ?: return null
        val subtitle = shortName.takeIf { it.isNotBlank() && it != title }
        return SearchResult.Course(id, title, subtitle, score)
    }

    private fun CalendarEvent.toResult(
        query: String,
        eventDateFormat: DateTimeFormatter
    ): SearchResult.CalendarEntry? {
        val people = when (this) {
            is CalendarEvent.Lesson -> teachers
            else -> emptyList()
        }
        val codes = listOfNotNull(shortLabel, (this as? CalendarEvent.Lesson)?.subjectCode)
        val score = SearchMatcher.score(query, MatchInput(title, codes + people)) ?: return null
        val place = location?.room ?: location?.building
        val subtitle = listOfNotNull(date.format(eventDateFormat), place).joinToString(" · ")
        return SearchResult.CalendarEntry(id, this is CalendarEvent.Exam, date, title, subtitle, score)
    }

    /**
     * The combined "name + course" alias lets "consegna analisi" hit an assignment of the
     * Analisi course even though neither string alone contains both tokens; quizzes and
     * rooms use the same trick.
     */
    private fun Assignment.toResult(
        query: String,
        courseNames: Map<CourseId, String>,
    ): SearchResult.Assignment? {
        val courseName = courseNames[courseId]
        val aliases = listOfNotNull(courseName, courseName?.let { "$name $it" })
        val score = SearchMatcher.score(query, MatchInput(name, aliases)) ?: return null
        return SearchResult.Assignment(courseId, id, name, courseName, score)
    }

    private fun Quiz.toResult(
        query: String,
        courseNames: Map<CourseId, String>,
    ): SearchResult.Quiz? {
        val courseName = courseNames[courseId]
        val aliases = listOfNotNull(courseName, courseName?.let { "$name $it" })
        val score = SearchMatcher.score(query, MatchInput(name, aliases)) ?: return null
        return SearchResult.Quiz(courseId, id, name, courseName, score)
    }

    private fun MapBuilding.toResult(query: String): SearchResult.Building? {
        val aliases = listOfNotNull(code.value, address).filter { it.isNotBlank() }
        val score = SearchMatcher.score(query, MatchInput(name, aliases)) ?: return null
        return SearchResult.Building(code, name, address, score)
    }

    private fun MapRoom.toResult(
        query: String,
        buildingNames: Map<BuildingCode, String>,
    ): SearchResult.Room? {
        val buildingName = buildingNames[buildingCode]
        val aliases = listOfNotNull(code.value, buildingName, buildingName?.let { "$name $it" })
        val score = SearchMatcher.score(query, MatchInput(name, aliases)) ?: return null
        return SearchResult.Room(buildingCode, code, name, buildingName, score)
    }

    private fun TranscriptRow.toResult(query: String): SearchResult.TranscriptEntry? {
        val aliases = listOfNotNull(activityCode).filter { it.isNotBlank() }
        val score = SearchMatcher.score(query, MatchInput(activityName, aliases)) ?: return null
        return SearchResult.TranscriptEntry(
            rowId = id,
            title = activityName,
            activityCode = activityCode,
            grade = grade,
            cumLaude = cumLaude,
            score = score,
        )
    }

    /**
     * Adaptive pick memory: a result the user opened for this same query — or for a more
     * specific query this one prefixes — gets a boost big enough to jump tiers, because
     * "same query, same pick" is the strongest relevance signal there is. Exact-query
     * picks outrank prefix-query picks; per key, the strongest boost wins.
     */
    private fun pickBoosts(query: String, history: List<SearchHistoryEntry>): Map<String, Double> {
        val queryNorm = normalizeForSearch(query)
        if (queryNorm.isEmpty()) return emptyMap()
        val boosts = HashMap<String, Double>()
        for (entry in history) {
            val pickedKey = entry.pickedKey ?: continue
            val storedNorm = normalizeForSearch(entry.query)
            val boost = when {
                storedNorm == queryNorm -> PICK_BOOST_EXACT
                storedNorm.startsWith(queryNorm) -> PICK_BOOST_PREFIX
                else -> continue
            }
            val current = boosts[pickedKey] ?: 0.0
            if (boost > current) boosts[pickedKey] = boost
        }
        return boosts
    }

    /**
     * Applies the recency nudge — a title the user searched before gets a small lift,
     * never enough to jump a tier — plus the pick-memory boost, clamping the total at 1.0.
     */
    private fun SearchResult.boosted(
        recentQueries: Set<String>,
        pickBoosts: Map<String, Double>,
    ): SearchResult {
        var boost = if (normalizeForSearch(title) in recentQueries) RECENCY_BOOST else 0.0
        pickBoosts[key]?.let { boost += it }
        if (boost == 0.0) return this
        val boostedScore = (score + boost).coerceAtMost(1.0)
        return when (this) {
            is SearchResult.Action -> copy(score = boostedScore)
            is SearchResult.Destination -> copy(score = boostedScore)
            is SearchResult.Course -> copy(score = boostedScore)
            is SearchResult.Assignment -> copy(score = boostedScore)
            is SearchResult.Quiz -> copy(score = boostedScore)
            is SearchResult.CalendarEntry -> copy(score = boostedScore)
            is SearchResult.CalendarDay -> copy(score = boostedScore)
            is SearchResult.Building -> copy(score = boostedScore)
            is SearchResult.Room -> copy(score = boostedScore)
            is SearchResult.TranscriptEntry -> copy(score = boostedScore)
        }
    }

    private data class PrimarySources(
        val courses: List<EnrolledCourse>,
        val events: List<CalendarEvent>,
        val buildings: List<MapBuilding>,
        val rows: List<TranscriptRow>,
        val history: List<SearchHistoryEntry>,
    )

    private data class DeepSources(
        val assignments: List<Assignment>,
        val quizzes: List<Quiz>,
        val rooms: List<MapRoom>,
    )

    private data class Sources(
        val primary: PrimarySources,
        val deep: DeepSources,
    )

    private companion object {
        /** Maximum number of upcoming calendar events fed into the index, not a day span. */
        const val UPCOMING_WINDOW = 100
        const val MAX_PER_CATEGORY = 8
        const val MAX_RESULTS = 30
        const val RECENCY_BOOST = 0.05
        const val PICK_BOOST_EXACT = 0.4
        const val PICK_BOOST_PREFIX = 0.25
        const val DATE_SCORE = 0.95
    }
}