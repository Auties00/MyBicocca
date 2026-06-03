package it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.ObserveAssignmentUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.RefreshCourseAssignmentsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.RefreshSubmissionStatusUseCase
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.state.AssignmentDetailOneShotEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import it.attendance100.mybicocca.ui.navigation.AppRoute

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = AssignmentDetailViewModel.Factory::class)
class AssignmentDetailViewModel @AssistedInject constructor(
    @Assisted private val key: AppRoute.AssignmentDetail,
    savedState: SavedStateHandle,
    observeActiveAccount: ObserveActiveAccountUseCase,
    private val observeAssignment: ObserveAssignmentUseCase,
    private val refreshSubmissionStatus: RefreshSubmissionStatusUseCase,
    private val refreshCourseAssignments: RefreshCourseAssignmentsUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(key: AppRoute.AssignmentDetail): AssignmentDetailViewModel
    }

    private val assignmentId: AssignmentId = AssignmentId(key.assignId)
    private val courseId: CourseId = CourseId(key.courseId)

    private val activeAccountId: Flow<AccountId?> = observeActiveAccount()
        .map { it?.id }
        .distinctUntilChanged()

    val assignment: StateFlow<Loadable<Assignment>> = activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(Loadable.NotYetLoaded) else observeAssignment(id, assignmentId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val oneShotChannel = Channel<AssignmentDetailOneShotEvent>(Channel.BUFFERED)
    val oneShotEvents: Flow<AssignmentDetailOneShotEvent> = oneShotChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            activeAccountId.filterNotNull().distinctUntilChanged().collect { id ->
                runRefresh(id)
            }
        }
    }

    fun pullToRefresh() {
        viewModelScope.launch { runRefresh(activeAccountId.filterNotNull().first()) }
    }

    fun openFile(url: String, fileName: String?) {
        oneShotChannel.trySend(AssignmentDetailOneShotEvent.OpenFile(url, fileName))
    }

    private suspend fun runRefresh(accountId: AccountId) {
        _syncStatus.value = SyncStatus.Refreshing
        runCatching {
            // Deep-opens (e.g. from the home deadlines rail) can land here before the
            // course's assignments were ever cached; the status-only refresh no-ops on a
            // missing row, so make sure the row exists first.
            if (assignment.value !is Loadable.Loaded) {
                refreshCourseAssignments(accountId, courseId)
            }
            refreshSubmissionStatus(accountId, assignmentId)
        }
            .onSuccess { _syncStatus.value = SyncStatus.Idle }
            .onFailure {
                _syncStatus.value = SyncStatus.Failed(it)
                oneShotChannel.trySend(AssignmentDetailOneShotEvent.RefreshFailed(it))
            }
    }

    private companion object {
        const val KEY_ASSIGNMENT_ID = "assignmentId"
        const val STATE_KEEP_ALIVE_MS = 5_000L
    }
}
