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
import it.attendance100.mybicocca.core.notification.NotificationRoute
import it.attendance100.mybicocca.domain.model.update.DownloadState
import it.attendance100.mybicocca.domain.model.update.PendingUpdateModal
import it.attendance100.mybicocca.domain.model.update.UpdateModalKind
import kotlinx.coroutines.launch
import it.attendance100.mybicocca.domain.usecase.notification.ConsumeNotificationRouteUseCase
import it.attendance100.mybicocca.domain.usecase.notification.ObservePendingNotificationRouteUseCase
import kotlinx.coroutines.flow.filterNotNull
import it.attendance100.mybicocca.domain.usecase.update.ObserveNightlyEventsUseCase
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import java.io.File

@HiltViewModel
class UpdateEventsViewModel @Inject constructor(
    observeUpdateEvents: ObserveUpdateEventsUseCase,
    observeNightlyEvents: ObserveNightlyEventsUseCase,
    private val updateRepository: it.attendance100.mybicocca.domain.repository.UpdateRepository,
    observePendingNotificationRoute: ObservePendingNotificationRouteUseCase,
    private val consumeNotificationRoute: ConsumeNotificationRouteUseCase,
) : ViewModel() {

    /** Routes carried in by a notification tap, for the shell to act on. */
    val notificationRoutes: Flow<NotificationRoute> = observePendingNotificationRoute()
        .filterNotNull()

    fun onNotificationRouteHandled() = consumeNotificationRoute()

    /**
     * The release a notification tap should open, resolved from the store rather than carried in
     * the intent: a notification can outlive the process that posted it, and by the time it is
     * tapped the stored state is the only thing still true.
     */
    suspend fun availableRelease(): AppRelease? = updateRepository.availableRelease()
    val events: Flow<AppRelease> = observeUpdateEvents()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000))
    val nightlyEvents: Flow<AppRelease> = observeNightlyEvents()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000))

    val downloadState: StateFlow<DownloadState> = updateRepository.downloadState

    /**
     * Read at decision time rather than kept as a cached snapshot: a snapshot has to be seeded
     * with a placeholder default until DataStore's first read lands, and a one-shot "should this
     * download now" branch that reads the placeholder gets the wrong answer with nothing to
     * signal it went wrong. A suspend read always waits for the real value.
     */
    suspend fun stableAutoDownload(): Boolean = updateRepository.observeStableAutoDownload().first()
    suspend fun nightlyAutoDownload(): Boolean = updateRepository.observeNightlyAutoDownload().first()

    fun startDownload(release: AppRelease): Boolean = updateRepository.startDownload(release)

    fun installApk(file: File) = updateRepository.installApk(file)

    fun clearDownload() = updateRepository.resetDownload()

    /** Backing out of a channel change: stop the download it started, not merely forget it. */
    fun cancelDownload() = updateRepository.cancelDownload()

    fun setNightlyEnabled(enabled: Boolean) {
        viewModelScope.launch { updateRepository.setNightlyEnabled(enabled) }
    }

    /**
     * The sheet that was open when the process died, so the shell can put it back. Read once at
     * startup rather than observed: the sheet's own hosts write this slot as they open, and a live
     * collector here would answer by opening a second copy on top.
     */
    suspend fun pendingUpdateModal(): PendingUpdateModal? = updateRepository.pendingUpdateModal()

    fun rememberOpenModal(release: AppRelease, kind: UpdateModalKind) {
        viewModelScope.launch { updateRepository.setPendingUpdateModal(release, kind) }
    }

    fun forgetOpenModal() {
        viewModelScope.launch { updateRepository.clearPendingUpdateModal() }
    }

    fun dismissDownloadError() = updateRepository.dismissDownloadError()
}
