package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.questionnaire.ActivityQuestionnaires
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivity
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.questionnaire.GetActivityQuestionnairesUseCase
import it.attendance100.mybicocca.domain.usecase.questionnaire.GetQuestionnaireActivitiesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

@HiltViewModel
class QuestionnairesViewModel @Inject constructor(
    private val getQuestionnaireActivities: GetQuestionnaireActivitiesUseCase,
    private val getActivityQuestionnaires: GetActivityQuestionnairesUseCase,
    observeActiveAccount: ObserveActiveAccountUseCase,
) : ViewModel() {

    private val activeCareerId: StateFlow<CareerId?> = observeActiveAccount()
        .map { it?.academic?.selectedCareerId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _activities = MutableStateFlow<Loadable<List<QuestionnaireActivity>>>(Loadable.NotYetLoaded)
    val activities: StateFlow<Loadable<List<QuestionnaireActivity>>> = _activities.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // Activity tapped to inspect its questionnaires — drives the units bottom sheet.
    private val _selectedActivity = MutableStateFlow<QuestionnaireActivity?>(null)
    val selectedActivity: StateFlow<QuestionnaireActivity?> = _selectedActivity.asStateFlow()

    private val _activityDetail = MutableStateFlow<Loadable<ActivityQuestionnaires>>(Loadable.NotYetLoaded)
    val activityDetail: StateFlow<Loadable<ActivityQuestionnaires>> = _activityDetail.asStateFlow()

    private val _detailStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val detailStatus: StateFlow<SyncStatus> = _detailStatus.asStateFlow()

    private val refreshMutex = Mutex()
    private var detailJob: Job? = null

    init {
        viewModelScope.launch {
            activeCareerId.filterNotNull().collect { careerId ->
                dismissActivity()
                fetch(careerId)
            }
        }
    }

    fun refresh() {
        val careerId = activeCareerId.value ?: return
        viewModelScope.launch { fetch(careerId) }
    }

    fun pullToRefresh() {
        val careerId = activeCareerId.value ?: return
        _activities.value = Loadable.NotYetLoaded
        viewModelScope.launch { fetch(careerId) }
    }

    fun openActivity(activity: QuestionnaireActivity) {
        _selectedActivity.value = activity
        fetchDetail(activity)
    }

    fun retryDetail() {
        val activity = _selectedActivity.value ?: return
        fetchDetail(activity)
    }

    fun dismissActivity() {
        detailJob?.cancel()
        _selectedActivity.value = null
        _activityDetail.value = Loadable.NotYetLoaded
        _detailStatus.value = SyncStatus.Idle
    }

    private fun fetchDetail(activity: QuestionnaireActivity) {
        val careerId = activeCareerId.value ?: return
        detailJob?.cancel()
        _activityDetail.value = Loadable.NotYetLoaded
        detailJob = viewModelScope.launch {
            _detailStatus.value = SyncStatus.Refreshing
            runCatching { getActivityQuestionnaires(careerId, activity.activityChoiceId) }.fold(
                onSuccess = { detail ->
                    _activityDetail.value = Loadable.Loaded(detail)
                    _detailStatus.value = SyncStatus.Idle
                },
                onFailure = { cause -> _detailStatus.value = SyncStatus.Failed(cause) },
            )
        }
    }

    private suspend fun fetch(careerId: CareerId) {
        if (!refreshMutex.tryLock()) return
        try {
            _syncStatus.value = SyncStatus.Refreshing
            runCatching { getQuestionnaireActivities(careerId) }.fold(
                onSuccess = { list ->
                    _activities.value = Loadable.Loaded(list)
                    _syncStatus.value = SyncStatus.Idle
                },
                onFailure = { cause -> _syncStatus.value = SyncStatus.Failed(cause) },
            )
        } finally {
            refreshMutex.unlock()
        }
    }
}
