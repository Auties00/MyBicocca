package it.attendance100.mybicocca.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.text.UiText
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.settings.BadgeCardTheme
import it.attendance100.mybicocca.domain.model.transcript.GradeRollup
import it.attendance100.mybicocca.domain.model.transcript.PrerequisiteStatus
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRowState
import it.attendance100.mybicocca.domain.model.transcript.TranscriptStats
import it.attendance100.mybicocca.domain.usecase.account.GetUserPhotoUseCase
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.settings.ObserveBadgeCardThemeUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.GetPrerequisiteStatusUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.ObserveGradeRollupUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.ObserveTranscriptRowsUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.ObserveTranscriptStatsUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.RefreshTranscriptUseCase
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Drives the profile body: the signed-in student's identity plus the transcript-derived
 * career statistics.
 *
 * Identity streams: [account], [activeCareer], [photoFile], and the persisted [badgeCardTheme]
 * finish the identity badge renders in. Data streams, cache-backed and scoped to the active
 * career: [stats], [gradeRollup], [transcriptRows], and the lazily filled [prerequisiteStatuses].
 * Sync streams: [syncStatus] with the derived [isRefreshing] and [errorMessage]. One-shot stream:
 * [openCalculatorRequests].
 *
 * A non-forced transcript refresh runs whenever the active career changes. Actions:
 * [refresh] forces a re-sync, [clearError] dismisses the failure message, and
 * [requestHypotheticalCalculator] asks the visible profile body to open the
 * hypothetical-average calculator.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeActiveAccount: ObserveActiveAccountUseCase,
    private val observeStats: ObserveTranscriptStatsUseCase,
    private val observeGradeRollup: ObserveGradeRollupUseCase,
    private val observeRows: ObserveTranscriptRowsUseCase,
    private val refreshTranscript: RefreshTranscriptUseCase,
    private val getPrerequisiteStatus: GetPrerequisiteStatusUseCase,
    private val getUserPhoto: GetUserPhotoUseCase,
    observeBadgeCardTheme: ObserveBadgeCardThemeUseCase,
) : ViewModel() {

    private val openCalculatorChannel = Channel<Unit>(Channel.BUFFERED)

    /**
     * Guided-search landing hook: each emission asks whichever profile body is on screen
     * to open the hypothetical-average calculator sheet.
     */
    val openCalculatorRequests: Flow<Unit> = openCalculatorChannel.receiveAsFlow()

    fun requestHypotheticalCalculator() {
        openCalculatorChannel.trySend(Unit)
    }

    /**
     * The persisted finish the identity badge renders in. Read-only here; the picker that changes
     * it lives in the Impostazioni > Aspetto sheet ([SettingsAppearanceViewModel]).
     */
    val badgeCardTheme: StateFlow<BadgeCardTheme> = observeBadgeCardTheme()
        .stateIn(viewModelScope, SharingStarted.Eagerly, BadgeCardTheme.Default)

    val account: StateFlow<Account?> = observeActiveAccount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val activeCareer: StateFlow<Career?> = account
        .map { acc -> acc?.academic?.careers?.firstOrNull { it.id == acc.academic.selectedCareerId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(KEEP_ALIVE_MS), null)

    private val activeCareerId: StateFlow<CareerId?> = account
        .map { it?.academic?.selectedCareerId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val photoFile: StateFlow<File?> = account
        .map { it?.id }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            if (id == null) flowOf<File?>(null)
            else flow<File?> {
                emit(null)
                runCatching { getUserPhoto(id) }.getOrNull()?.let { emit(File(it)) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(KEEP_ALIVE_MS), null)

    val stats: StateFlow<Loadable<TranscriptStats>> = activeCareerId
        .flatMapLatest { id ->
            if (id == null) flowOf(Loadable.NotYetLoaded)
            else observeStats(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    val gradeRollup: StateFlow<Loadable<GradeRollup>> = activeCareerId
        .flatMapLatest { id ->
            if (id == null) flowOf(Loadable.NotYetLoaded)
            else observeGradeRollup(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    /** Transcript rows limited to study-plan activities; supernumerary ones are dropped. */
    val transcriptRows: StateFlow<Loadable<List<TranscriptRow>>> = activeCareerId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(Loadable.NotYetLoaded)
            } else {
                observeRows(id).map { loadable ->
                    when (loadable) {
                        Loadable.NotYetLoaded -> Loadable.NotYetLoaded
                        is Loadable.Loaded -> Loadable.Loaded(loadable.value.filter { it.inStudyPlan })
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    val isRefreshing: StateFlow<Boolean> = syncStatus
        .map { it is SyncStatus.Refreshing }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(KEEP_ALIVE_MS), false)

    val errorMessage: StateFlow<UiText?> = syncStatus
        .map { status -> if (status is SyncStatus.Failed) UiText.StringResource(R.string.profile_refresh_error) else null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(KEEP_ALIVE_MS), null)

    private val _prerequisiteStatuses = MutableStateFlow<Map<Long, PrerequisiteStatus>>(emptyMap())

    /**
     * Propedeuticità per pending activity, keyed by activityChoiceId and fanned out lazily
     * once the rows load. Only [PrerequisiteStatus.NotSatisfied] entries drive the warning
     * badge in the exam list; passed rows are skipped because the prerequisite endpoint
     * rejects them.
     */
    val prerequisiteStatuses: StateFlow<Map<Long, PrerequisiteStatus>> =
        _prerequisiteStatuses.asStateFlow()

    init {
        viewModelScope.launch {
            activeCareerId.filterNotNull().distinctUntilChanged().collect { id ->
                runRefresh(id, force = false)
            }
        }
        viewModelScope.launch {
            transcriptRows.collect { loadable ->
                if (loadable is Loadable.Loaded) loadPrerequisites(loadable.value)
            }
        }
    }

    private fun loadPrerequisites(rows: List<TranscriptRow>) {
        val careerId = activeCareerId.value ?: return
        val pending = rows.filter { it.state != TranscriptRowState.Passed }
        val known = _prerequisiteStatuses.value
        pending.forEach { row ->
            if (row.id in known) return@forEach
            launchPrerequisiteFetch(careerId, row.id)
        }
    }

    private fun launchPrerequisiteFetch(careerId: CareerId, activityChoiceId: Long) {
        viewModelScope.launch {
            val status = getPrerequisiteStatus(careerId, activityChoiceId)
            _prerequisiteStatuses.value += (activityChoiceId to status)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val id = activeCareerId.filterNotNull().first()
            runRefresh(id, force = true)
        }
    }

    fun clearError() {
        if (_syncStatus.value is SyncStatus.Failed) _syncStatus.value = SyncStatus.Idle
    }

    private suspend fun runRefresh(careerId: CareerId, force: Boolean) {
        _syncStatus.value = SyncStatus.Refreshing
        runCatching { refreshTranscript(careerId, force) }
            .onSuccess { _syncStatus.value = SyncStatus.Idle }
            .onFailure { _syncStatus.value = SyncStatus.Failed(it) }
    }

    private companion object {
        const val KEEP_ALIVE_MS = 5_000L
    }
}
