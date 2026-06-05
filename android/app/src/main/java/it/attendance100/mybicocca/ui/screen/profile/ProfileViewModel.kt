package it.attendance100.mybicocca.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.document.AcademicTitle
import it.attendance100.mybicocca.domain.model.document.Certificate
import it.attendance100.mybicocca.domain.model.document.CertificateId
import it.attendance100.mybicocca.domain.model.transcript.GradeRollup
import it.attendance100.mybicocca.domain.model.transcript.PrerequisiteStatus
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRowState
import it.attendance100.mybicocca.domain.model.transcript.TranscriptStats
import it.attendance100.mybicocca.domain.usecase.account.GetUserPhotoUseCase
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.document.DownloadCertificateUseCase
import it.attendance100.mybicocca.domain.usecase.document.GetAcademicTitlesUseCase
import it.attendance100.mybicocca.domain.usecase.document.GetCertificatesUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.GetPrerequisiteStatusUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.ObserveGradeRollupUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.ObserveTranscriptRowsUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.ObserveTranscriptStatsUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.RefreshTranscriptUseCase
import it.attendance100.mybicocca.ui.screen.profile.state.DocumentEvent
import it.attendance100.mybicocca.util.NetworkMonitor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

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
    private val getAcademicTitles: GetAcademicTitlesUseCase,
    private val getCertificates: GetCertificatesUseCase,
    private val downloadCertificate: DownloadCertificateUseCase,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

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

    // Only study-plan activities are surfaced on the profile; supernumerary ones are dropped.
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

    val errorMessage: StateFlow<String?> = syncStatus
        .map { status -> if (status is SyncStatus.Failed) "Impossibile aggiornare i dati" else null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(KEEP_ALIVE_MS), null)

    // Propedeuticità per not-yet-passed activity, keyed by activityChoiceId. Fanned out
    // lazily once the rows are loaded; only the NotSatisfied ones drive the orange warning
    // icon in the libretto list. /prop is skipped for passed rows (the endpoint 422s).
    private val _prerequisiteStatuses = MutableStateFlow<Map<Long, PrerequisiteStatus>>(emptyMap())
    val prerequisiteStatuses: StateFlow<Map<Long, PrerequisiteStatus>> =
        _prerequisiteStatuses.asStateFlow()

    // Titoli + certificati surfaced at the bottom of the profile. Secondary streams: any
    // failure resolves to Loaded(emptyList()) so the documents sections simply hide and the
    // main profile error/refresh state is never affected.
    private val _titles = MutableStateFlow<Loadable<List<AcademicTitle>>>(Loadable.NotYetLoaded)
    val titles: StateFlow<Loadable<List<AcademicTitle>>> = _titles.asStateFlow()

    private val _certificates = MutableStateFlow<Loadable<List<Certificate>>>(Loadable.NotYetLoaded)
    val certificates: StateFlow<Loadable<List<Certificate>>> = _certificates.asStateFlow()

    // Ids currently being downloaded, so each row can show its own progress spinner.
    private val _downloadingCertificates = MutableStateFlow<Set<CertificateId>>(emptySet())
    val downloadingCertificates: StateFlow<Set<CertificateId>> = _downloadingCertificates.asStateFlow()

    private val _events = Channel<DocumentEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

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
        viewModelScope.launch {
            activeCareerId.filterNotNull().collect { fetchDocuments(it) }
        }
    }

    // Titoli + certificati fetch. Both are secondary and degrade to empty on failure, so a
    // network/SAML error simply hides the sections rather than blanking the profile.
    private suspend fun fetchDocuments(careerId: CareerId) {
        runCatching { getAcademicTitles(careerId) }.fold(
            onSuccess = { _titles.value = Loadable.Loaded(it) },
            onFailure = { _titles.value = Loadable.Loaded(emptyList()) },
        )
        runCatching { getCertificates() }.fold(
            onSuccess = { _certificates.value = Loadable.Loaded(it) },
            onFailure = { _certificates.value = Loadable.Loaded(emptyList()) },
        )
    }

    // Downloads a certificate PDF and emits a one-shot OpenPdf event for the UI to open it.
    // Guards against double-taps via the per-id downloading set.
    fun download(certificate: Certificate) {
        if (certificate.id in _downloadingCertificates.value) return
        viewModelScope.launch {
            _downloadingCertificates.update { it + certificate.id }
            runCatching { downloadCertificate(certificate.id) }.fold(
                onSuccess = { bytes ->
                    _events.send(DocumentEvent.OpenPdf(bytes, certificate.fileName()))
                },
                onFailure = {
                    _events.send(DocumentEvent.ShowMessage("Impossibile scaricare il certificato. Riprova."))
                },
            )
            _downloadingCertificates.update { it - certificate.id }
        }
    }

    // Sanitizes the description into a stable .pdf file name for the cache + viewer.
    private fun Certificate.fileName(): String {
        val base = description.ifBlank { "certificato" }
            .replace(Regex("[^A-Za-z0-9 _-]"), "")
            .trim()
            .replace(Regex("\\s+"), "_")
            .take(80)
            .ifBlank { "certificato" }
        val suffix = solarYear?.let { "_$it" }.orEmpty()
        return "$base$suffix.pdf"
    }

    private suspend fun loadPrerequisites(rows: List<TranscriptRow>) {
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
            _prerequisiteStatuses.value = _prerequisiteStatuses.value + (activityChoiceId to status)
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
