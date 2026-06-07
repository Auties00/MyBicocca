package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogSearchHit
import it.attendance100.mybicocca.domain.model.elearning.catalog.ElearningCatalog
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.catalog.LoadElearningCatalogUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.catalog.SearchElearningCatalogUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.EnrolIntoCourseUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ObserveEnrolledCoursesUseCase
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.AddCourseOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.CatalogStackEntry
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.EnrolmentStatus
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddCourseViewModel @Inject constructor(
    observeActiveAccount: ObserveActiveAccountUseCase,
    observeEnrolledCourses: ObserveEnrolledCoursesUseCase,
    private val loadCatalog: LoadElearningCatalogUseCase,
    private val searchCatalog: SearchElearningCatalogUseCase,
    private val enrolIntoCourse: EnrolIntoCourseUseCase,
) : ViewModel() {

    private val activeAccountId = observeActiveAccount()
        .map { it?.id }
        .distinctUntilChanged()

    // Courses the user is already subscribed to (from the local Moodle cache). Surfaced so the
    // catalog shows them as "Enrolled" instead of offering a re-subscribe the API would reject.
    // Eager so it is already populated by the time the catalog finishes loading (and the user can
    // tap) — otherwise a tap in the brief subscription warm-up would see an empty set and re-enrol.
    private val enrolledCourseIds: StateFlow<Set<CourseId>> = activeAccountId
        .flatMapLatest { accountId ->
            if (accountId == null) flowOf(emptySet())
            else observeEnrolledCourses(accountId).map { loadable ->
                (loadable as? Loadable.Loaded)?.value?.mapTo(HashSet()) { it.id }.orEmpty()
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private val _catalog = MutableStateFlow<Loadable<ElearningCatalog>>(Loadable.NotYetLoaded)
    val catalog: StateFlow<Loadable<ElearningCatalog>> = _catalog.asStateFlow()

    private val _stack = MutableStateFlow<List<CatalogStackEntry>>(emptyList())
    val stack: StateFlow<List<CatalogStackEntry>> = _stack.asStateFlow()

    val searchQuery = MutableStateFlow("")

    // Transient statuses driven by the user's own enrol taps this session.
    private val _localEnrolment = MutableStateFlow<Map<CourseId, EnrolmentStatus>>(emptyMap())

    // Effective status per course. Server enrolment is authoritative and wins: a course that is
    // already joined always shows "Enrolled", even if a stale local tap left it Failed/InProgress —
    // otherwise the row would offer a re-enrol the API rejects. Local statuses only surface for
    // courses not (yet) in the server set: in-progress / just-enrolled / failed taps this session.
    val enrolment: StateFlow<Map<CourseId, EnrolmentStatus>> =
        combine(enrolledCourseIds, _localEnrolment) { enrolled, local ->
            if (enrolled.isEmpty()) local
            else HashMap<CourseId, EnrolmentStatus>(enrolled.size + local.size).apply {
                putAll(local)
                enrolled.forEach { put(it, EnrolmentStatus.Enrolled) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val oneShotChannel = Channel<AddCourseOneShotEvent>(Channel.BUFFERED)
    val oneShotEvents: Flow<AddCourseOneShotEvent> = oneShotChannel.receiveAsFlow()

    val searchResults: StateFlow<List<CatalogSearchHit>> = combine(
        searchQuery.debounce(120).map { it.trim() }.distinctUntilChanged(),
        _catalog,
    ) { q, cat -> q to cat }
        .flatMapLatest { (q, cat) ->
            if (q.isEmpty() || cat !is Loadable.Loaded) flowOf(emptyList())
            else flowOf(searchCatalog(q, limit = 80))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var loadJob: Job? = null

    fun ensureCatalogLoaded() {
        if (_catalog.value is Loadable.Loaded || loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            runCatching { loadCatalog() }
                .onSuccess { _catalog.value = Loadable.Loaded(it) }
                .onFailure { _catalog.value = Loadable.NotYetLoaded }
        }
    }

    fun open(entry: CatalogStackEntry) {
        _stack.value = _stack.value + entry
        searchQuery.value = ""
    }

    fun back() {
        val current = _stack.value
        if (current.isEmpty()) return
        _stack.value = current.dropLast(1)
        searchQuery.value = ""
    }

    fun resetStack() {
        _stack.value = emptyList()
        searchQuery.value = ""
    }

    fun setSearch(query: String) {
        searchQuery.value = query
    }

    fun enrol(courseId: CourseId, courseName: String) {
        // Short-circuit (haptic only, no request) when already subscribed on the server or a tap is
        // already in flight. Both sources are read directly/synchronously — NOT via the combined
        // `enrolment` flow, whose .value lags _localEnrolment, so two fast taps would otherwise both
        // pass this guard and fire duplicate enrol requests.
        if (courseId in enrolledCourseIds.value) return
        val local = _localEnrolment.value[courseId]
        if (local == EnrolmentStatus.InProgress || local == EnrolmentStatus.Enrolled) return
        _localEnrolment.update { it + (courseId to EnrolmentStatus.InProgress) }
        viewModelScope.launch {
            val accountId = activeAccountId.first()
            if (accountId == null) {
                _localEnrolment.update { it + (courseId to EnrolmentStatus.Idle) }
                oneShotChannel.trySend(AddCourseOneShotEvent.RequireSignIn)
                return@launch
            }
            runCatching { enrolIntoCourse(accountId, courseId) }
                .onSuccess {
                    _localEnrolment.update { it + (courseId to EnrolmentStatus.Enrolled) }
                    oneShotChannel.trySend(AddCourseOneShotEvent.EnrolSucceeded(courseId, courseName))
                }
                .onFailure { cause ->
                    _localEnrolment.update { it + (courseId to EnrolmentStatus.Failed) }
                    oneShotChannel.trySend(AddCourseOneShotEvent.EnrolFailed(courseId, cause))
                }
        }
    }
}
