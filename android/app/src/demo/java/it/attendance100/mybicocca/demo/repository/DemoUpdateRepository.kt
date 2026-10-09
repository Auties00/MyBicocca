package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.data.repository.UpdateRepositoryImpl
import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.model.update.PendingUpdateModal
import it.attendance100.mybicocca.domain.model.update.UpdateCheckResult
import it.attendance100.mybicocca.domain.model.update.UpdateStatus
import it.attendance100.mybicocca.domain.repository.UpdateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The real repository with every question about newer versions answered "up to date", so no
 * update banner, modal or notification ever shows. Settings and the release list still work.
 */
@Singleton
class DemoUpdateRepository @Inject constructor(
    private val real: UpdateRepositoryImpl,
) : UpdateRepository by real {

    override fun observeStatus(): Flow<UpdateStatus> = flowOf(UpdateStatus.UpToDate)

    override fun observeNightlyStatus(): Flow<UpdateStatus> = flowOf(UpdateStatus.UpToDate)

    override val newUpdateEvents: Flow<AppRelease> = emptyFlow()

    override val newNightlyUpdateEvents: Flow<AppRelease> = emptyFlow()

    override suspend fun checkForUpdates(force: Boolean, announce: Boolean): UpdateCheckResult = UpdateCheckResult.UpToDate

    override suspend fun getLatestStableRelease(): UpdateCheckResult = UpdateCheckResult.UpToDate

    override suspend fun availableRelease(): AppRelease? = null

    override suspend fun availableNightlyRelease(): AppRelease? = null

    override suspend fun pendingUpdateModal(): PendingUpdateModal? = null
}
