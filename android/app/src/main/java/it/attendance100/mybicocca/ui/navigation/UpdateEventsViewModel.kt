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

    /**
     * Read at decision time rather than kept as a cached snapshot: a snapshot has to be seeded
     * with a placeholder default until DataStore's first read lands, and a one-shot "should this
     * download now" branch that reads the placeholder gets the wrong answer with nothing to
     * signal it went wrong. A suspend read always waits for the real value.
     */
    suspend fun stableAutoDownload(): Boolean = updateRepository.observeStableAutoDownload().first()
    suspend fun nightlyAutoDownload(): Boolean = updateRepository.observeNightlyAutoDownload().first()

    fun startDownload(release: AppRelease) {
        downloader.startDownload(release)
    }

    fun installApk(file: File) {
        downloader.installApk(file)
    }

    fun clearDownload() {
        downloader.resetState()
    }

    fun dismissDownloadError() {
        downloader.dismissError()
    }
}
