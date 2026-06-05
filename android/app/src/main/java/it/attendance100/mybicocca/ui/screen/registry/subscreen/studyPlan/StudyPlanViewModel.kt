package it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.BuildConfig
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.StudyPath
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlan
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.studyplan.GetStudyPathUseCase
import it.attendance100.mybicocca.domain.usecase.studyplan.GetStudyPlanPrintUseCase
import it.attendance100.mybicocca.domain.usecase.studyplan.GetStudyPlanUseCase
import it.attendance100.mybicocca.domain.usecase.studyplan.IsStudyPlanEditableUseCase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan.state.StudyPlanEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
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

// Loadable<StudyPlan?>: Loaded(null) means the career genuinely has no plan, which is a
// valid empty state distinct from "not yet fetched".
@HiltViewModel
class StudyPlanViewModel @Inject constructor(
    private val getStudyPlan: GetStudyPlanUseCase,
    private val getStudyPath: GetStudyPathUseCase,
    private val isStudyPlanEditable: IsStudyPlanEditableUseCase,
    private val getStudyPlanPrint: GetStudyPlanPrintUseCase,
    observeActiveAccount: ObserveActiveAccountUseCase,
) : ViewModel() {

    private val activeCareerId: StateFlow<CareerId?> = observeActiveAccount()
        .map { it?.academic?.selectedCareerId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _plan = MutableStateFlow<Loadable<StudyPlan?>>(Loadable.NotYetLoaded)
    val plan: StateFlow<Loadable<StudyPlan?>> = _plan.asStateFlow()

    // The path section (percorso / orientamento / profilo / part-time). Independent of
    // the plan stream: a path lookup failure must not blank the course list.
    private val _studyPath = MutableStateFlow<Loadable<StudyPath?>>(Loadable.NotYetLoaded)
    val studyPath: StateFlow<Loadable<StudyPath?>> = _studyPath.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // True while the compilation window for the plan's choice regulation is open.
    private val _editable = MutableStateFlow(false)
    val editable: StateFlow<Boolean> = _editable.asStateFlow()

    private val _actionInProgress = MutableStateFlow(false)
    val actionInProgress: StateFlow<Boolean> = _actionInProgress.asStateFlow()

    private val _events = Channel<StudyPlanEvent>(Channel.BUFFERED)
    val events: Flow<StudyPlanEvent> = _events.receiveAsFlow()

    private val refreshMutex = Mutex()

    init {
        viewModelScope.launch {
            activeCareerId.filterNotNull().collect { careerId ->
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
        _plan.value = Loadable.NotYetLoaded
        _studyPath.value = Loadable.NotYetLoaded
        viewModelScope.launch { fetch(careerId) }
    }

    fun printPlan() {
        val careerId = activeCareerId.value ?: return
        val planId = _plan.value.valueOrNull()?.planId ?: return
        if (_actionInProgress.value) return
        viewModelScope.launch {
            _actionInProgress.value = true
            try {
                val bytes = getStudyPlanPrint(careerId, planId)
                _events.send(StudyPlanEvent.OpenPdf(bytes, "piano_di_studi_$planId.pdf"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.send(StudyPlanEvent.ShowMessage("Stampa del piano non disponibile."))
            } finally {
                _actionInProgress.value = false
            }
        }
    }

    // Selecting a different path option means recompiling the plan against the chosen
    // schema and POSTing it to /piani/{stuId} — the same mutating endpoint the plan
    // editor uses. It is only ever reachable when a window is open AND >1 distinct option
    // exists (UI-gated) and re-checked here.
    //
    // TODO: wire the real submit once it can be exercised on a multi-percorso account.
    //  The flow is: getStudyPlanDraft(careerId, planId=null, regsceId, schemaId) to build
    //  the rules for the picked schema (pre-selecting mandatory + carry-over activities),
    //  then submitStudyPlan(careerId, rules) which POSTs Esse3PostPlanBody(type="S",
    //  state="P", cancelValidPlanFlag=true, activity=[...]) to /piani/{stuId}. Left as a
    //  read-only preview for now to avoid a destructive POST during testing.
    fun chooseStudyPath(schemaId: Long) {
        val path = _studyPath.value.valueOrNull() ?: return
        if (!path.choiceAvailable) return
        if (path.options.none { it.schemaId == schemaId }) return
        if (_actionInProgress.value) return
        viewModelScope.launch {
            _events.send(
                StudyPlanEvent.ShowMessage("Il cambio percorso non è ancora disponibile in app.")
            )
        }
    }

    private suspend fun fetch(careerId: CareerId) {
        if (!refreshMutex.tryLock()) return
        try {
            _syncStatus.value = SyncStatus.Refreshing
            runCatching { getStudyPlan(careerId) }.fold(
                onSuccess = { plan ->
                    _plan.value = Loadable.Loaded(plan)
                    _syncStatus.value = SyncStatus.Idle
                    // Secondary: a failed window lookup must not surface as a page error,
                    // it just keeps the edit action disabled. Debug builds force the edit
                    // action on so the editor can be exercised outside compilation windows.
                    _editable.value = BuildConfig.DEBUG || (plan?.choiceRegulationId?.let { regulationId ->
                        runCatching { isStudyPlanEditable(regulationId) }.getOrDefault(false)
                    } ?: false)
                },
                onFailure = { cause -> _syncStatus.value = SyncStatus.Failed(cause) },
            )
            // Secondary stream: the path section degrades to "non disponibile" on failure
            // rather than failing the whole page.
            _studyPath.value = Loadable.Loaded(
                runCatching { getStudyPath(careerId) }.getOrNull()
            )
        } finally {
            refreshMutex.unlock()
        }
    }
}
