package it.attendance100.mybicocca.ui.screen.settings.subscreen.appInfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.domain.model.update.DEFAULT_UPDATE_CHECK_INTERVAL_MINUTES
import it.attendance100.mybicocca.domain.model.update.DownloadState
import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.model.update.UpdateModalKind
import it.attendance100.mybicocca.domain.model.update.UpdateCheckResult
import it.attendance100.mybicocca.domain.model.update.UpdateStatus
import it.attendance100.mybicocca.domain.usecase.update.CheckForUpdatesUseCase
import it.attendance100.mybicocca.domain.usecase.update.GetUpdatePageUrlUseCase
import it.attendance100.mybicocca.domain.usecase.update.ObserveUpdateStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Backs the About sheet's update actions. Streams the persisted [status] (so the "Check for
 * Updates" tile already reflects an update the daily check found) and exposes a manual
 * [check] that forces a fresh look and hands its one-shot outcome back for the snackbar, with
 * [checking] guarding against overlapping taps. [updatePageUrl] resolves the store-aware tap
 * target for an available release.
 */
import it.attendance100.mybicocca.domain.usecase.update.ObserveNightlyEnabledUseCase
import it.attendance100.mybicocca.domain.usecase.update.ObserveNightlyStatusUseCase
import it.attendance100.mybicocca.domain.usecase.update.SetNightlyEnabledUseCase
import it.attendance100.mybicocca.domain.repository.UpdateRepository

@HiltViewModel
class AppInfoViewModel @Inject constructor(
    observeUpdateStatus: ObserveUpdateStatusUseCase,
    private val observeNightlyEnabled: ObserveNightlyEnabledUseCase,
    private val observeNightlyStatus: ObserveNightlyStatusUseCase,
    private val setNightlyEnabledUseCase: SetNightlyEnabledUseCase,
    private val checkForUpdates: CheckForUpdatesUseCase,
    private val getUpdatePageUrl: GetUpdatePageUrlUseCase,
    private val updateRepository: UpdateRepository,
) : ViewModel() {

    val status: StateFlow<UpdateStatus> = observeUpdateStatus()
        .stateIn(viewModelScope, SharingStarted.Eagerly, UpdateStatus.Unknown)

    private val _checking = MutableStateFlow(false)
    val checking: StateFlow<Boolean> = _checking.asStateFlow()

    /** The in-flight update download, surfaced to the UI without exposing the downloader itself. */
    val downloadState: StateFlow<DownloadState> = updateRepository.downloadState

    fun startDownload(release: AppRelease): Boolean = updateRepository.startDownload(release)

    /** Launches the installer for a finished download; call only from the foreground. */
    fun installDownload(file: File) = updateRepository.installApk(file)

    fun clearDownload() = updateRepository.resetDownload()

    fun dismissDownloadError() = updateRepository.dismissDownloadError()

    /** Forces a check; ignores re-taps while one is in flight. Delivers the outcome to [onResult]. */
    fun check(onResult: (UpdateCheckResult) -> Unit) {
        if (_checking.value) return
        viewModelScope.launch {
            _checking.value = true
            val result = checkForUpdates(force = true)
            _checking.value = false
            onResult(result)
        }
    }

    /**
     * Fetches the latest stable release for "restore to stable" — deliberately not [check], see
     * [UpdateRepository.getLatestStableRelease].
     */
    fun restoreToStable(onResult: (UpdateCheckResult) -> Unit) {
        viewModelScope.launch { onResult(updateRepository.getLatestStableRelease()) }
    }

    fun updatePageUrl(release: AppRelease): String = getUpdatePageUrl(release)
    
    val nightlyEnabled: StateFlow<Boolean> = observeNightlyEnabled()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
        
    val nightlyStatus: StateFlow<UpdateStatus> = observeNightlyStatus()
        .stateIn(viewModelScope, SharingStarted.Eagerly, UpdateStatus.Unknown)
        
    fun setNightlyEnabled(enabled: Boolean) {
        viewModelScope.launch {
            setNightlyEnabledUseCase(enabled)
        }
    }

    /**
     * Turns the beta channel on and reports the nightly to offer, or null when there is nothing to
     * offer - already running it, or the forced check found nothing. Enabling runs that check
     * itself, so the answer is ready by the time this returns.
     */
    fun enableNightlyAndOffer(onResult: (AppRelease?) -> Unit) {
        viewModelScope.launch {
            setNightlyEnabledUseCase(true)
            onResult(updateRepository.availableNightlyRelease())
        }
    }

    /** Backing out of a channel change: stop the download it started, not merely forget it. */
    fun cancelDownload() = updateRepository.cancelDownload()

    fun rememberOpenModal(release: AppRelease, kind: UpdateModalKind) {
        viewModelScope.launch { updateRepository.setPendingUpdateModal(release, kind) }
    }

    fun forgetOpenModal() {
        viewModelScope.launch { updateRepository.clearPendingUpdateModal() }
    }
    
    fun checkAndOfferStable(onOfferStable: () -> Unit) {
        if (!nightlyEnabled.value) return
        onOfferStable()
    }

    val stableAutoDownload: StateFlow<Boolean> = updateRepository.observeStableAutoDownload()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setStableAutoDownload(enabled: Boolean) {
        viewModelScope.launch { updateRepository.setStableAutoDownload(enabled) }
    }

    val nightlyAutoDownload: StateFlow<Boolean> = updateRepository.observeNightlyAutoDownload()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun setNightlyAutoDownload(enabled: Boolean) {
        viewModelScope.launch { updateRepository.setNightlyAutoDownload(enabled) }
    }

    val checkIntervalMinutes: StateFlow<Int> = updateRepository.observeCheckIntervalMinutes()
        .stateIn(viewModelScope, SharingStarted.Eagerly, DEFAULT_UPDATE_CHECK_INTERVAL_MINUTES)

    fun setCheckIntervalMinutes(minutes: Int) {
        viewModelScope.launch { updateRepository.setCheckIntervalMinutes(minutes) }
    }
}
