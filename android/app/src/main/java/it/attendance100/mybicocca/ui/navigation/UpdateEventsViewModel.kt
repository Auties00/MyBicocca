package it.attendance100.mybicocca.ui.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.usecase.update.ObserveUpdateEventsUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Exposes the "a newer version was just found" events to the signed-in shell so it can raise the
 * app-wide "new version available" snackbar. Only the daily background check feeds this stream
 * (and only once per newly-discovered version); the manual Settings check reports its own
 * outcome through its sheet instead.
 */
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import androidx.lifecycle.viewModelScope
import it.attendance100.mybicocca.data.local.settings.UpdateStateStore
import it.attendance100.mybicocca.data.update.ApkDownloader
import it.attendance100.mybicocca.data.update.DownloadState
import it.attendance100.mybicocca.domain.usecase.update.ObserveNightlyEventsUseCase
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

@HiltViewModel
class UpdateEventsViewModel @Inject constructor(
    observeUpdateEvents: ObserveUpdateEventsUseCase,
    observeNightlyEvents: ObserveNightlyEventsUseCase,
    private val downloader: ApkDownloader,
    private val updateRepository: it.attendance100.mybicocca.domain.repository.UpdateRepository
) : ViewModel() {
    val events: Flow<AppRelease> = observeUpdateEvents()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000))
    val nightlyEvents: Flow<AppRelease> = observeNightlyEvents()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000))

    val downloadState: StateFlow<DownloadState> = downloader.downloadState

    val stableAutoDownload: StateFlow<Boolean> = updateRepository.observeStableAutoDownload()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val nightlyAutoDownload: StateFlow<Boolean> = updateRepository.observeNightlyAutoDownload()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val nightlyAutoInstall: StateFlow<Boolean> = updateRepository.observeNightlyAutoInstall()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * Fresh reads of the auto-download/auto-install settings, bypassing the cached StateFlows
     * above — those are eagerly shared from ViewModel creation, so right after a process restart
     * (every silent install causes one) there's a real window where they still show their
     * placeholder default rather than the actual persisted value, since DataStore's read hasn't
     * completed yet. That's exactly the kind of race a one-shot "should this install silently"
     * decision can't afford to get wrong; a suspend read on the underlying flow always waits for
     * the real value instead.
     */
    suspend fun freshStableAutoDownload(): Boolean = updateRepository.observeStableAutoDownload().first()
    suspend fun freshNightlyAutoDownload(): Boolean = updateRepository.observeNightlyAutoDownload().first()
    suspend fun freshNightlyAutoInstall(): Boolean = updateRepository.observeNightlyAutoInstall().first()

    fun startDownload(release: AppRelease) {
        downloader.startDownload(release)
    }

    fun installApk(file: File, silent: Boolean) {
        viewModelScope.launch {
            downloader.installApk(file, silent)
        }
    }

    fun clearDownload() {
        downloader.resetState()
    }

    fun dismissDownloadError() {
        downloader.dismissError()
    }
}
