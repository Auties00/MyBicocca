package it.attendance100.mybicocca.ui.screen.registry.subscreen.internships

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.internship.InternshipApplication
import it.attendance100.mybicocca.domain.model.internship.InternshipApplicationDetail
import it.attendance100.mybicocca.domain.model.internship.InternshipAttachment
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.internship.GetInternshipApplicationAttachmentsUseCase
import it.attendance100.mybicocca.domain.usecase.internship.GetInternshipApplicationDetailUseCase
import it.attendance100.mybicocca.domain.usecase.internship.GetInternshipApplicationsUseCase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.internships.state.InternshipEvent
import kotlinx.coroutines.Job
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

@HiltViewModel
class InternshipsViewModel @Inject constructor(
    private val getInternshipApplications: GetInternshipApplicationsUseCase,
    private val getInternshipApplicationDetail: GetInternshipApplicationDetailUseCase,
    private val getInternshipApplicationAttachments: GetInternshipApplicationAttachmentsUseCase,
    observeActiveAccount: ObserveActiveAccountUseCase,
) : ViewModel() {

    private val activeCareerId: StateFlow<CareerId?> = observeActiveAccount()
        .map { it?.academic?.selectedCareerId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _applications = MutableStateFlow<Loadable<List<InternshipApplication>>>(Loadable.NotYetLoaded)
    val applications: StateFlow<Loadable<List<InternshipApplication>>> = _applications.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // Application tapped to inspect its training project — drives the detail bottom sheet.
    private val _selectedApplication = MutableStateFlow<InternshipApplication?>(null)
    val selectedApplication: StateFlow<InternshipApplication?> = _selectedApplication.asStateFlow()

    private val _applicationDetail = MutableStateFlow<Loadable<InternshipApplicationDetail>>(Loadable.NotYetLoaded)
    val applicationDetail: StateFlow<Loadable<InternshipApplicationDetail>> = _applicationDetail.asStateFlow()

    private val _attachments = MutableStateFlow<Loadable<List<InternshipAttachment>>>(Loadable.NotYetLoaded)
    val attachments: StateFlow<Loadable<List<InternshipAttachment>>> = _attachments.asStateFlow()

    private val _detailStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val detailStatus: StateFlow<SyncStatus> = _detailStatus.asStateFlow()

    private val _events = Channel<InternshipEvent>(Channel.BUFFERED)
    val events: Flow<InternshipEvent> = _events.receiveAsFlow()

    private val refreshMutex = Mutex()
    private var detailJob: Job? = null

    init {
        viewModelScope.launch {
            activeCareerId.filterNotNull().collect { careerId ->
                dismissApplication()
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
        _applications.value = Loadable.NotYetLoaded
        viewModelScope.launch { fetch(careerId) }
    }

    fun openApplication(application: InternshipApplication) {
        _selectedApplication.value = application
        fetchDetail(application)
    }

    fun retryDetail() {
        val application = _selectedApplication.value ?: return
        fetchDetail(application)
    }

    fun dismissApplication() {
        detailJob?.cancel()
        _selectedApplication.value = null
        _applicationDetail.value = Loadable.NotYetLoaded
        _attachments.value = Loadable.NotYetLoaded
        _detailStatus.value = SyncStatus.Idle
    }

    // Tirocini management actions (ritira / registra ore / firma PF / carica allegato) have
    // no student REST endpoint; they'll be wired to Esse3LegacyApi (scrape) once a test
    // account with an active internship is available. UI is in place; for now they report it.
    fun withdrawApplication(application: InternshipApplication) = notifyComingSoon()

    fun registerHours(application: InternshipApplication) = notifyComingSoon()

    fun signTrainingProject(application: InternshipApplication) = notifyComingSoon()

    fun uploadAttachment(application: InternshipApplication) = notifyComingSoon()

    private fun notifyComingSoon() {
        // TODO: route through an InternshipManagementRepository → Esse3LegacyApi once implemented.
        _events.trySend(InternshipEvent.ShowMessage("Questa funzione sarà disponibile a breve."))
    }

    private fun fetchDetail(application: InternshipApplication) {
        val careerId = activeCareerId.value ?: return
        detailJob?.cancel()
        _applicationDetail.value = Loadable.NotYetLoaded
        _attachments.value = Loadable.NotYetLoaded
        detailJob = viewModelScope.launch {
            _detailStatus.value = SyncStatus.Refreshing
            coroutineScope {
                launch {
                    // Attachments are secondary: their failure must not blank the detail
                    // sheet, so resolve to an empty list instead of an error state.
                    runCatching { getInternshipApplicationAttachments(careerId, application.id) }.fold(
                        onSuccess = { _attachments.value = Loadable.Loaded(it) },
                        onFailure = { _attachments.value = Loadable.Loaded(emptyList()) },
                    )
                }
                runCatching { getInternshipApplicationDetail(careerId, application.id) }.fold(
                    onSuccess = { detail ->
                        _applicationDetail.value = Loadable.Loaded(detail)
                        _detailStatus.value = SyncStatus.Idle
                    },
                    onFailure = { cause -> _detailStatus.value = SyncStatus.Failed(cause) },
                )
            }
        }
    }

    private suspend fun fetch(careerId: CareerId) {
        if (!refreshMutex.tryLock()) return
        try {
            _syncStatus.value = SyncStatus.Refreshing
            runCatching { getInternshipApplications(careerId) }.fold(
                onSuccess = { list ->
                    _applications.value = Loadable.Loaded(list)
                    _syncStatus.value = SyncStatus.Idle
                },
                onFailure = { cause -> _syncStatus.value = SyncStatus.Failed(cause) },
            )
        } finally {
            refreshMutex.unlock()
        }
    }
}
