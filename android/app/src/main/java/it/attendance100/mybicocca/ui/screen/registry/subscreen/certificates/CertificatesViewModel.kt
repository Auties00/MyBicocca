package it.attendance100.mybicocca.ui.screen.registry.subscreen.certificates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.text.UiText
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.document.Certificate
import it.attendance100.mybicocca.domain.model.document.CertificateId
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.document.DownloadCertificateUseCase
import it.attendance100.mybicocca.domain.usecase.document.GetCertificatesUseCase
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Backs the "Certificati" sheet with the active career's downloadable certificate list.
 * Hosts a modal sheet, not a route, so it outlives any single open; the active career is
 * tracked so a switch drops the list back to loading (the previous career's certificates
 * never flash) and re-fetches.
 *
 * Streams: [certificates] carries the fetched list as a [Loadable]; [syncStatus] tracks
 * the in-flight refresh; [downloadingCertificates] and [downloadedCertificates] drive
 * the per-row download indicators; [events] carries one-shot effects (open a cached PDF,
 * show a message) that never replay across rotation.
 *
 * Actions: [refresh] re-fetches the list; [download] fetches a certificate's PDF (or
 * reuses the disk cache) and emits the open-file event.
 */
@HiltViewModel
class CertificatesViewModel @Inject constructor(
    private val certificateStorage: CertificateStorage,
    private val getCertificates: GetCertificatesUseCase,
    private val downloadCertificate: DownloadCertificateUseCase,
    observeActiveAccount: ObserveActiveAccountUseCase,
) : ViewModel() {

    private val activeCareerId: StateFlow<CareerId?> = observeActiveAccount()
        .map { it?.academic?.selectedCareerId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _certificates = MutableStateFlow<Loadable<List<Certificate>>>(Loadable.NotYetLoaded)
    val certificates: StateFlow<Loadable<List<Certificate>>> = _certificates.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _downloadingCertificates = MutableStateFlow<Set<CertificateId>>(emptySet())

    /** Ids currently being downloaded, so each row can show its own progress spinner. */
    val downloadingCertificates: StateFlow<Set<CertificateId>> = _downloadingCertificates.asStateFlow()

    private val _downloadedCertificates = MutableStateFlow<Set<CertificateId>>(emptySet())

    /** Ids whose PDF is already cached on disk, so each row shows a downloaded vs download icon. */
    val downloadedCertificates: StateFlow<Set<CertificateId>> = _downloadedCertificates.asStateFlow()

    private val _events = Channel<CertificateEvent>(Channel.BUFFERED)
    val events: Flow<CertificateEvent> = _events.receiveAsFlow()

    private val refreshMutex = Mutex()

    init {
        viewModelScope.launch {
            activeCareerId.filterNotNull().collect {
                _certificates.value = Loadable.NotYetLoaded
                load()
            }
        }
    }

    /** No-op until the first career lands: the career collector owns the initial fetch. */
    fun refresh() {
        if (activeCareerId.value == null) return
        viewModelScope.launch { load() }
    }

    /** Coalesces concurrent callers (sheet-open refresh + the career collector): only the first proceeds. */
    private suspend fun load() {
        if (!refreshMutex.tryLock()) return
        try {
            _syncStatus.value = SyncStatus.Refreshing
            runCatching { getCertificates() }
                .onSuccess {
                    _certificates.value = Loadable.Loaded(it)
                    _syncStatus.value = SyncStatus.Idle
                    recomputeDownloaded(it)
                }
                .onFailure { _syncStatus.value = SyncStatus.Failed(it) }
        } finally {
            refreshMutex.unlock()
        }
    }

    private suspend fun recomputeDownloaded(certificates: List<Certificate>) {
        val cached = withContext(Dispatchers.IO) {
            certificates.filter { certificateStorage.isDownloaded(it) }.map { it.id }.toSet()
        }
        _downloadedCertificates.value = cached
    }

    /**
     * Opens a certificate's PDF, downloading it to its predictable cache path first when
     * it isn't already there. Emits a one-shot [CertificateEvent.OpenFile] for the UI to
     * hand off to a viewer. Guards against double-taps via the per-id downloading set.
     */
    fun download(certificate: Certificate) {
        if (certificate.id in _downloadingCertificates.value) return
        viewModelScope.launch {
            val cached = certificateStorage.getFile(certificate)
            if (cached.exists() && cached.length() > 0) {
                _events.send(CertificateEvent.OpenFile(cached.absolutePath))
                return@launch
            }
            _downloadingCertificates.update { it + certificate.id }
            runCatching { downloadCertificate(certificate.id) }
                .mapCatching { bytes -> certificateStorage.write(certificate, bytes) }
                .fold(
                    onSuccess = { file ->
                        _downloadedCertificates.update { it + certificate.id }
                        _events.send(CertificateEvent.OpenFile(file.absolutePath))
                    },
                    onFailure = {
                        _events.send(CertificateEvent.ShowMessage(UiText.StringResource(R.string.certificates_download_error)))
                    },
                )
            _downloadingCertificates.update { it - certificate.id }
        }
    }
}

/**
 * One-shot effects of the certificates sheet. Channel-backed, consumed once, never
 * replayed across rotation.
 */
sealed interface CertificateEvent {
    data class ShowMessage(val message: UiText) : CertificateEvent

    /** Path of a certificate PDF cached on disk; the UI opens it in an external viewer. */
    data class OpenFile(val path: String) : CertificateEvent
}
