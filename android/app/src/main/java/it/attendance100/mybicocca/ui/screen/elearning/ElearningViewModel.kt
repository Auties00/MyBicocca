package it.attendance100.mybicocca.ui.screen.elearning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseFilter
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourseGroup
import it.attendance100.mybicocca.domain.model.elearning.deadline.Deadline
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ObserveAvailableStudyYearsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ObserveCourseFilterUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ObserveEnrolledCoursesUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ObserveFilteredCoursesUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.RefreshEnrolledCoursesUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.SetCourseFilterUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.SetCourseHiddenUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ToggleCourseFavouriteUseCase
import it.attendance100.mybicocca.ui.screen.elearning.state.ElearningOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.state.InitialFetchState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ElearningViewModel @Inject constructor(
    observeActiveAccount: ObserveActiveAccountUseCase,
    observeCourseFilter: ObserveCourseFilterUseCase,
    observeEnrolledCourses: ObserveEnrolledCoursesUseCase,
    private val observeFilteredCourses: ObserveFilteredCoursesUseCase,
    private val observeAvailableStudyYears: ObserveAvailableStudyYearsUseCase,
    private val refreshEnrolledCourses: RefreshEnrolledCoursesUseCase,
    private val setCourseFilter: SetCourseFilterUseCase,
    private val toggleCourseFavourite: ToggleCourseFavouriteUseCase,
    private val setCourseHidden: SetCourseHiddenUseCase,
) : ViewModel() {

    val filter: StateFlow<CourseFilter> = observeCourseFilter()
        .stateIn(viewModelScope, SharingStarted.Eagerly, CourseFilter.All)

    // Moodle enrolment is account-scoped, not career-scoped, and the course year now comes
    // from each course's idNumber — so nothing here depends on the selected career.
    private val activeAccountId: Flow<AccountId?> = observeActiveAccount()
        .map { it?.id }
        .distinctUntilChanged()

    val availableStudyYears: StateFlow<List<StudyYear>> =
        activeAccountId
            .flatMapLatest { accountId -> observeAvailableStudyYears(accountId) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Eagerly so Room is subscribed at VM construction. Combined with the VM being
    // hoisted at MainShell, this means visibleCourses is already Loaded by the time
    // the user picks the Elearning tab — no NotYetLoaded flash on first visit. The
    // underlying transform is pure Room, so this never waits on the network.
    val visibleCourses: StateFlow<Loadable<List<EnrolledCourseGroup>>> =
        activeAccountId
            .flatMapLatest { accountId ->
                if (accountId == null) flowOf(Loadable.NotYetLoaded)
                else observeFilteredCourses(accountId)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, Loadable.NotYetLoaded)

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // Raw Room snapshot, pre-filter, so a filter that matches nothing is never mistaken
    // for a cold cache.
    private val rawCourses: StateFlow<Loadable<List<EnrolledCourse>>> =
        activeAccountId
            .flatMapLatest { accountId ->
                if (accountId == null) flowOf(Loadable.NotYetLoaded)
                else observeEnrolledCourses(accountId)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, Loadable.NotYetLoaded)

    // Drives the full-screen loading/error swap (filter bar hidden) until the cache is
    // first populated. Once there is any cached course, the regular states take over.
    val initialFetch: StateFlow<InitialFetchState> =
        combine(rawCourses, _syncStatus) { raw, sync ->
            when {
                raw !is Loadable.Loaded -> InitialFetchState.InProgress
                raw.value.isNotEmpty() -> InitialFetchState.Settled
                sync is SyncStatus.Refreshing -> InitialFetchState.InProgress
                sync is SyncStatus.Failed -> InitialFetchState.Failed(sync.cause)
                else -> InitialFetchState.Settled
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, InitialFetchState.InProgress)

    private val oneShotChannel = Channel<ElearningOneShotEvent>(Channel.BUFFERED)
    val oneShotEvents: Flow<ElearningOneShotEvent> = oneShotChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            activeAccountId.filterNotNull().distinctUntilChanged().collect { id ->
                runRefresh(id, force = false)
            }
        }
    }

    fun setFilter(filter: CourseFilter) {
        viewModelScope.launch { setCourseFilter(filter) }
    }

    fun toggleFavourite(courseId: CourseId, favourite: Boolean) {
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runCatching { toggleCourseFavourite(accountId, courseId, favourite) }
        }
    }

    // Mirrors a favourite toggle to every edition of the group so per-edition
    // Room state stays consistent with the group-level UI state.
    fun toggleGroupFavourite(group: EnrolledCourseGroup, favourite: Boolean) {
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            group.editions.forEach { edition ->
                runCatching { toggleCourseFavourite(accountId, edition.id, favourite) }
            }
        }
    }

    fun setHidden(courseId: CourseId, hidden: Boolean) {
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runCatching { setCourseHidden(accountId, courseId, hidden) }
        }
    }

    fun openCourse(courseId: CourseId) {
        oneShotChannel.trySend(ElearningOneShotEvent.OpenCourse(courseId))
    }

    fun openDeadline(deadline: Deadline) {
        val event = when (deadline) {
            is Deadline.Assignment -> ElearningOneShotEvent.OpenAssignment(deadline.courseId, deadline.id)
            is Deadline.Quiz -> ElearningOneShotEvent.OpenQuiz(deadline.courseId, deadline.id)
        }
        oneShotChannel.trySend(event)
    }

    fun pullToRefresh() {
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runRefresh(accountId, force = true)
        }
    }

    private suspend fun runRefresh(accountId: AccountId, force: Boolean) {
        _syncStatus.value = SyncStatus.Refreshing
        runCatching { refreshEnrolledCourses(accountId, force) }
            .onSuccess { _syncStatus.value = SyncStatus.Idle }
            .onFailure { _syncStatus.value = SyncStatus.Failed(it) }
    }

}
