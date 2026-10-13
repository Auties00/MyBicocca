package it.attendance100.mybicocca.demo.repository

import android.content.Context
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.data.local.settings.UpdateStateStore
import it.attendance100.mybicocca.data.repository.UpdateRepositoryImpl
import it.attendance100.mybicocca.data.update.ApkDownloadWorker
import it.attendance100.mybicocca.di.ApplicationScope
import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.model.update.PendingUpdateModal
import it.attendance100.mybicocca.domain.model.update.UpdateCheckResult
import it.attendance100.mybicocca.domain.model.update.UpdateStatus
import it.attendance100.mybicocca.domain.repository.UpdateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The real repository with every question about newer versions answered "up to date", so no
 * update banner, modal or notification ever shows. Settings and the release list still work.
 *
 * Answering is not enough to stop a download. The background workers read the saved update
 * state themselves, and a demo build shares its application id with debug, so it can inherit an
 * offer, or a download already queued, from the build it was installed over. That state is
 * wiped at startup and again at every check, which is the first thing the periodic worker does.
 */
@Singleton
class DemoUpdateRepository @Inject constructor(
    private val real: UpdateRepositoryImpl,
    private val store: UpdateStateStore,
    @ApplicationContext context: Context,
    @ApplicationScope scope: CoroutineScope,
) : UpdateRepository by real {

    init {
        WorkManager.getInstance(context).cancelUniqueWork(ApkDownloadWorker.UNIQUE_WORK_NAME)
        scope.launch { forgetOffers() }
    }

    override fun observeStatus(): Flow<UpdateStatus> = flowOf(UpdateStatus.UpToDate)

    override fun observeNightlyStatus(): Flow<UpdateStatus> = flowOf(UpdateStatus.UpToDate)

    override val newUpdateEvents: Flow<AppRelease> = emptyFlow()

    override val newNightlyUpdateEvents: Flow<AppRelease> = emptyFlow()

    override suspend fun checkForUpdates(force: Boolean, announce: Boolean): UpdateCheckResult {
        forgetOffers()
        return UpdateCheckResult.UpToDate
    }

    override suspend fun getLatestStableRelease(): UpdateCheckResult = UpdateCheckResult.UpToDate

    override suspend fun availableRelease(): AppRelease? = null

    override suspend fun availableNightlyRelease(): AppRelease? = null

    override suspend fun pendingUpdateModal(): PendingUpdateModal? = null

    override fun startDownload(release: AppRelease): Boolean = false

    // The real one follows the switch with a forced check against GitHub, which would save an offer.
    override suspend fun setNightlyEnabled(enabled: Boolean) {
        store.setNightlyEnabled(enabled)
        if (!enabled) store.clearNightlyState()
    }

    private suspend fun forgetOffers() {
        store.setUpToDate(System.currentTimeMillis())
        store.clearNightlyState()
        store.clearPendingDownloadRelease()
        store.clearPendingUpdateModal()
    }
}
