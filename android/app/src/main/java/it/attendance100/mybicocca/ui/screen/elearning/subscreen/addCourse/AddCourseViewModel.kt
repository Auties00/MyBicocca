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
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.AddCourseLevel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.AddCourseOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.CatalogStackEntry
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.CourseItem
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
    private val loadCatalog: LoadElearningCatalogUseCase,
    private val searchCatalog: SearchElearningCatalogUseCase,
    private val enrolIntoCourse: EnrolIntoCourseUseCase,
) : ViewModel() {

    private val activeAccountId = observeActiveAccount()
        .map { it?.id }
        .distinctUntilChanged()

    private val _catalog = MutableStateFlow<Loadable<ElearningCatalog>>(Loadable.NotYetLoaded)
    val catalog: StateFlow<Loadable<ElearningCatalog>> = _catalog.asStateFlow()

    private val _stack = MutableStateFlow<List<CatalogStackEntry>>(emptyList())
    val stack: StateFlow<List<CatalogStackEntry>> = _stack.asStateFlow()

    val searchQuery = MutableStateFlow("")

    private val _enrolment = MutableStateFlow<Map<CourseId, EnrolmentStatus>>(emptyMap())
    val enrolment: StateFlow<Map<CourseId, EnrolmentStatus>> = _enrolment.asStateFlow()

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
        val current = _enrolment.value[courseId]
        if (current == EnrolmentStatus.InProgress || current == EnrolmentStatus.Enrolled) return
        _enrolment.update { it + (courseId to EnrolmentStatus.InProgress) }
        viewModelScope.launch {
            val accountId = activeAccountId.first()
            if (accountId == null) {
                _enrolment.update { it + (courseId to EnrolmentStatus.Idle) }
                oneShotChannel.trySend(AddCourseOneShotEvent.RequireSignIn)
                return@launch
            }
            runCatching { enrolIntoCourse(accountId, courseId) }
                .onSuccess {
                    _enrolment.update { it + (courseId to EnrolmentStatus.Enrolled) }
                    oneShotChannel.trySend(AddCourseOneShotEvent.EnrolSucceeded(courseId, courseName))
                }
                .onFailure { cause ->
                    _enrolment.update { it + (courseId to EnrolmentStatus.Failed) }
                    oneShotChannel.trySend(AddCourseOneShotEvent.EnrolFailed(courseId, cause))
                }
        }
    }

    val level: StateFlow<AddCourseLevel?> = combine(_catalog, _stack, _enrolment) { catalog, stack, enrolment ->
        val cat = (catalog as? Loadable.Loaded)?.value ?: return@combine null
        if (stack.isEmpty()) {
            AddCourseLevel.Root(cat.sections)
        } else {
            val node = stack.last().node
            AddCourseLevel.Inside(
                node = node,
                children = node.children,
                courses = node.courses.map { course ->
                    CourseItem(course, enrolment[course.id] ?: EnrolmentStatus.Idle)
                },
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
