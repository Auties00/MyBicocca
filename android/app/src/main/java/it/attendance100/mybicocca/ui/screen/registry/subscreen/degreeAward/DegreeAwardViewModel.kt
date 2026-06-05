package it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.degreeaward.DiscussionMode
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationCallId
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationHub
import it.attendance100.mybicocca.domain.model.degreeaward.SupervisorAssignment
import it.attendance100.mybicocca.domain.model.degreeaward.SupervisorCandidate
import it.attendance100.mybicocca.domain.model.degreeaward.ThesisDraft
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.degreeAward.AssignSupervisorsUseCase
import it.attendance100.mybicocca.domain.usecase.degreeAward.CancelGraduationApplicationUseCase
import it.attendance100.mybicocca.domain.usecase.degreeAward.GetDiscussionModesUseCase
import it.attendance100.mybicocca.domain.usecase.degreeAward.GetGraduationHubUseCase
import it.attendance100.mybicocca.domain.usecase.degreeAward.SearchSupervisorsUseCase
import it.attendance100.mybicocca.domain.usecase.degreeAward.SetThesisDiscussionModeUseCase
import it.attendance100.mybicocca.domain.usecase.degreeAward.SubmitGraduationApplicationUseCase
import it.attendance100.mybicocca.domain.usecase.degreeAward.SubmitThesisUseCase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.state.DegreeAwardEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

// Graduation hub: no Room cache (see DegreeAwardRepository). The hub is fetched on the
// active career and held in memory; every mutation re-pulls it. Search results and the
// discussion-mode list are lazy side streams loaded on demand by the sheets.
@HiltViewModel
class DegreeAwardViewModel @Inject constructor(
    private val getGraduationHub: GetGraduationHubUseCase,
    private val submitApplication: SubmitGraduationApplicationUseCase,
    private val cancelApplication: CancelGraduationApplicationUseCase,
    private val submitThesis: SubmitThesisUseCase,
    private val searchSupervisors: SearchSupervisorsUseCase,
    private val assignSupervisors: AssignSupervisorsUseCase,
    private val getDiscussionModes: GetDiscussionModesUseCase,
    private val setDiscussionMode: SetThesisDiscussionModeUseCase,
    observeActiveAccount: ObserveActiveAccountUseCase,
) : ViewModel() {

    private val activeCareerId: StateFlow<CareerId?> = observeActiveAccount()
        .map { it?.academic?.selectedCareerId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _hub = MutableStateFlow<Loadable<GraduationHub>>(Loadable.NotYetLoaded)
    val hub: StateFlow<Loadable<GraduationHub>> = _hub.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _actionInProgress = MutableStateFlow(false)
    val actionInProgress: StateFlow<Boolean> = _actionInProgress.asStateFlow()

    // Supervisor search side stream, owned by the relatori sheet.
    private val _searchResults = MutableStateFlow<List<SupervisorCandidate>>(emptyList())
    val searchResults: StateFlow<List<SupervisorCandidate>> = _searchResults.asStateFlow()

    private val _searching = MutableStateFlow(false)
    val searching: StateFlow<Boolean> = _searching.asStateFlow()

    private val _discussionModes = MutableStateFlow<List<DiscussionMode>>(emptyList())
    val discussionModes: StateFlow<List<DiscussionMode>> = _discussionModes.asStateFlow()

    private val _events = Channel<DegreeAwardEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val refreshMutex = Mutex()

    init {
        viewModelScope.launch {
            activeCareerId.filterNotNull().collect { fetch(it) }
        }
    }

    fun refresh() {
        val careerId = activeCareerId.value ?: return
        viewModelScope.launch { fetch(careerId) }
    }

    private suspend fun fetch(careerId: CareerId) {
        if (!refreshMutex.tryLock()) return
        try {
            _syncStatus.value = SyncStatus.Refreshing
            runCatching { getGraduationHub(careerId) }.fold(
                onSuccess = {
                    _hub.value = Loadable.Loaded(it)
                    _syncStatus.value = SyncStatus.Idle
                },
                onFailure = { _syncStatus.value = SyncStatus.Failed(it) },
            )
        } finally {
            refreshMutex.unlock()
        }
    }

    fun searchSupervisors(query: String, includeExternal: Boolean) {
        val surname = query.trim()
        if (surname.length < 2) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _searching.value = true
            runCatching { searchSupervisors.invoke(surname, includeExternal) }.fold(
                onSuccess = { _searchResults.value = it },
                onFailure = { _searchResults.value = emptyList() },
            )
            _searching.value = false
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
    }

    fun loadDiscussionModes() {
        if (_discussionModes.value.isNotEmpty()) return
        viewModelScope.launch {
            runCatching { getDiscussionModes() }.onSuccess { _discussionModes.value = it }
        }
    }

    // --- Mutations. Each is gated in the UI; here we only guard against double-tap and
    // translate failures to one-shot error events. ---

    fun applyToCall(callId: GraduationCallId) = mutate(DegreeAwardEvent.ApplicationSubmitted) { careerId ->
        submitApplication(careerId, callId)
    }

    fun cancelApplication() = mutate(DegreeAwardEvent.ApplicationCancelled) { careerId ->
        val applicationId = currentHub()?.application?.id ?: error("Nessuna domanda da annullare.")
        cancelApplication.invoke(careerId, applicationId)
    }

    fun saveThesis(draft: ThesisDraft) = mutate(DegreeAwardEvent.ThesisSubmitted) { careerId ->
        val hub = currentHub() ?: error("Stato non disponibile.")
        val applicationId = hub.application?.id ?: error("Presenta prima la domanda di laurea.")
        val regId = hub.thesisTypes.firstOrNull { it.code == draft.thesisTypeCode }?.committeeRegulationId
            ?: hub.application.committeeRegulationId
        submitThesis(careerId, applicationId, regId, draft)
    }

    fun assignSupervisors(assignments: List<SupervisorAssignment>) =
        mutate(DegreeAwardEvent.SupervisorsAssigned) {
            val thesisId = currentHub()?.thesis?.id ?: error("Inserisci prima la tesi.")
            assignSupervisors.invoke(thesisId, assignments)
        }

    fun setDiscussionMode(modeCode: String) = mutate(DegreeAwardEvent.DiscussionModeSet) {
        val thesisId = currentHub()?.thesis?.id ?: error("Inserisci prima la tesi.")
        setDiscussionMode.invoke(thesisId, modeCode)
    }

    private fun currentHub(): GraduationHub? = _hub.value.valueOrNull()

    private fun mutate(success: DegreeAwardEvent, block: suspend (CareerId) -> Unit) {
        val careerId = activeCareerId.value ?: return
        if (_actionInProgress.value) return
        viewModelScope.launch {
            _actionInProgress.value = true
            try {
                block(careerId)
                _events.send(success)
                fetch(careerId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.send(DegreeAwardEvent.ShowError(e.friendlyMessage()))
            } finally {
                _actionInProgress.value = false
            }
        }
    }
}

private fun Throwable.friendlyMessage(): String {
    val raw = message ?: return "Operazione non riuscita. Riprova."
    return when {
        raw.contains("Security failed", ignoreCase = true) ||
            raw.contains("profilo", ignoreCase = true) ->
            "Operazione non consentita dal tuo profilo in questo momento."
        raw.contains("403") -> "Accesso negato: operazione non disponibile."
        raw.contains("scadenz", ignoreCase = true) -> "Sei fuori dalla finestra delle scadenze."
        else -> raw
    }
}
