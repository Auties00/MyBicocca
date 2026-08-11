package it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.core.text.UiText
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.StudyPath
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlan
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.studyplan.GetStudyPathUseCase
import it.attendance100.mybicocca.domain.usecase.studyplan.GetStudyPlanPrintUseCase
import it.attendance100.mybicocca.domain.usecase.studyplan.GetStudyPlanUseCase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan.state.StudyPlanEvent
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
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

/**
 * Backs the "Percorso e piano" sheet with the active career's study plan and path.
 *
 * Streams: [plan] carries the fetched plan — Loaded(null) means the career genuinely has
 * no plan, a valid empty state distinct from "not yet fetched"; [studyPath] carries the
 * percorso facet independently so a path failure never blanks the course list;
 * [syncStatus] tracks the in-flight refresh; [editable] gates the compiler entry;
 * [actionInProgress] guards the print action; [events] carries one-shot effects (open
 * the printed PDF, show a message) that never replay across rotation. A career switch
 * triggers a reload automatically.
 *
 * Actions: [refresh] re-fetches plan and path together; [printPlan] fetches the plan PDF
 * and emits it as an event.
 */
@HiltViewModel
class StudyPlanViewModel @Inject constructor(

    private val getStudyPlan: GetStudyPlanUseCase,
    private val getStudyPath: GetStudyPathUseCase,
    private val getStudyPlanPrint: GetStudyPlanPrintUseCase,
    observeActiveAccount: ObserveActiveAccountUseCase,
) : ViewModel() {

    /**
     * Exposed for the no-plan entry: the edit flow needs a studentId (== careerId) even
     * when there's no plan to take it from.
     */
    val activeCareerId: StateFlow<CareerId?> = observeActiveAccount()
        .map { it?.academic?.selectedCareerId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _plan = MutableStateFlow<Loadable<StudyPlan?>>(Loadable.NotYetLoaded)
    val plan: StateFlow<Loadable<StudyPlan?>> = _plan.asStateFlow()

    private val _studyPath = MutableStateFlow<Loadable<StudyPath?>>(Loadable.NotYetLoaded)

    /**
     * The path section (percorso / orientamento / profilo / part-time). Independent of
     * the plan stream: a path lookup failure must not blank the course list.
     */
    val studyPath: StateFlow<Loadable<StudyPath?>> = _studyPath.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _editable = MutableStateFlow(false)

    /** True while the compilation window for the plan's choice regulation is open. */
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
            } catch (_: Exception) {
                _events.send(
                    StudyPlanEvent.ShowMessage(UiText.StringResource(R.string.studyplan_print_unavailable)),
                )
            } finally {
                _actionInProgress.value = false
            }
        }
    }

    /**
     * Plan and path fan out together so the sheet paints once, complete — the path must
     * never pop in after the header is already on screen. A failed path lookup degrades
     * silently to Loaded(null) rather than failing the page; only the plan call drives
     * [syncStatus]. The compilation-window state rides on the same path lookup, with no
     * separate call.
     */
    private suspend fun fetch(careerId: CareerId) {
        if (!refreshMutex.tryLock()) return
        try {
            _syncStatus.value = SyncStatus.Refreshing
            coroutineScope {
                val pathDeferred = async { runCatching { getStudyPath(careerId) }.getOrNull() }
                runCatching { getStudyPlan(careerId) }.fold(
                    onSuccess = { plan ->
                        _plan.value = Loadable.Loaded(plan)
                        _syncStatus.value = SyncStatus.Idle
                    },
                    onFailure = { cause -> _syncStatus.value = SyncStatus.Failed(cause) },
                )
                val path = pathDeferred.await()
                _studyPath.value = Loadable.Loaded(path)
                _editable.value = path?.editingOpen == true
            }
        } finally {
            refreshMutex.unlock()
        }
    }
}
