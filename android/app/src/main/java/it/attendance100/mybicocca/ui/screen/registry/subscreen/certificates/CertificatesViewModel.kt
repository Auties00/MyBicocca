package it.attendance100.mybicocca.ui.screen.registry.subscreen.certificates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.document.Certificate
import it.attendance100.mybicocca.domain.model.document.CertificateId
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.document.DownloadCertificateUseCase
import it.attendance100.mybicocca.domain.usecase.document.GetCertificatesUseCase
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

@HiltViewModel
class CertificatesViewModel @Inject constructor(
    private val getCertificates: GetCertificatesUseCase,
    private val downloadCertificate: DownloadCertificateUseCase,
    observeActiveAccount: ObserveActiveAccountUseCase,
) : ViewModel() {

    // The VM hosts a modal sheet, not a route, so it outlives any single open. The
    // career is tracked to re-fetch on switches instead of relying on per-route init.
    private val activeCareerId: StateFlow<CareerId?> = observeActiveAccount()
        .map { it?.academic?.selectedCareerId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _certificates = MutableStateFlow<Loadable<List<Certificate>>>(Loadable.NotYetLoaded)
    val certificates: StateFlow<Loadable<List<Certificate>>> = _certificates.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // Ids currently being downloaded, so each row can show its own progress spinner.
    private val _downloadingCertificates = MutableStateFlow<Set<CertificateId>>(emptySet())
    val downloadingCertificates: StateFlow<Set<CertificateId>> = _downloadingCertificates.asStateFlow()

    private val _events = Channel<CertificateEvent>(Channel.BUFFERED)
    val events: Flow<CertificateEvent> = _events.receiveAsFlow()

    private val refreshMutex = Mutex()

    init {
        viewModelScope.launch {
            activeCareerId.filterNotNull().collect {
                // A switch invalidates the list outright: drop to the loading state
                // rather than flashing the previous career's certificates.
                _certificates.value = Loadable.NotYetLoaded
                load()
            }
        }
    }

    fun refresh() {
        // No-op until the first career lands: the init collector owns the initial fetch.
        if (activeCareerId.value == null) return
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        // Coalesce concurrent callers (sheet-open refresh + the career collector).
        if (!refreshMutex.tryLock()) return
        try {
            _syncStatus.value = SyncStatus.Refreshing
            runCatching { getCertificates() }
                .onSuccess {
                    _certificates.value = Loadable.Loaded(it)
                    _syncStatus.value = SyncStatus.Idle
                }
                .onFailure { _syncStatus.value = SyncStatus.Failed(it) }
        } finally {
            refreshMutex.unlock()
        }
    }

    // Downloads a certificate PDF and emits a one-shot OpenPdf event for the UI to open it.
    // Guards against double-taps via the per-id downloading set.
    fun download(certificate: Certificate) {
        if (certificate.id in _downloadingCertificates.value) return
        viewModelScope.launch {
            _downloadingCertificates.update { it + certificate.id }
            runCatching { downloadCertificate(certificate.id) }.fold(
                onSuccess = { bytes ->
                    _events.send(CertificateEvent.OpenPdf(bytes, certificate.fileName()))
                },
                onFailure = {
                    _events.send(CertificateEvent.ShowMessage("Impossibile scaricare il certificato"))
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
}

// One-shot effects (certificate download). Channel-backed, consumed once, never replayed
// across rotation.
sealed interface CertificateEvent {
    data class ShowMessage(val message: String) : CertificateEvent

    // Carries a freshly downloaded certificate PDF to the UI, which opens it in an
    // external viewer.
    data class OpenPdf(val bytes: ByteArray, val fileName: String) : CertificateEvent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is OpenPdf) return false
            return fileName == other.fileName && bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int = 31 * fileName.hashCode() + bytes.contentHashCode()
    }
}
