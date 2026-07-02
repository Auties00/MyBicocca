package it.attendance100.mybicocca.ui.screen.settings.subscreen.appInfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.update.ApkDownloader
import it.attendance100.mybicocca.data.update.DownloadState
import it.attendance100.mybicocca.domain.model.update.AppRelease
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
@HiltViewModel
class AppInfoViewModel @Inject constructor(
    observeUpdateStatus: ObserveUpdateStatusUseCase,
    private val checkForUpdates: CheckForUpdatesUseCase,
    private val getUpdatePageUrl: GetUpdatePageUrlUseCase,
    private val downloader: ApkDownloader,
) : ViewModel() {

    val status: StateFlow<UpdateStatus> = observeUpdateStatus()
        .stateIn(viewModelScope, SharingStarted.Eagerly, UpdateStatus.Unknown)

    private val _checking = MutableStateFlow(false)
    val checking: StateFlow<Boolean> = _checking.asStateFlow()

    /** The in-flight update download, surfaced to the UI without exposing the downloader itself. */
    val downloadState: StateFlow<DownloadState> = downloader.downloadState

    fun startDownload(release: AppRelease) = downloader.startDownload(release)

    /** Launches the installer for a finished download; call only from the foreground. */
    fun installDownload(file: File) = downloader.installApk(file)

    fun clearDownload() = downloader.resetState()

    fun dismissDownloadError() = downloader.dismissError()

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

    fun updatePageUrl(release: AppRelease): String = getUpdatePageUrl(release)
}
