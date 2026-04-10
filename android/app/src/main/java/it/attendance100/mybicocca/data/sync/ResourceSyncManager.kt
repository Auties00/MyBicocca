package it.attendance100.mybicocca.data.sync

import it.attendance100.mybicocca.di.IoDispatcher
import it.attendance100.mybicocca.util.NetworkMonitor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

private data class SyncSnapshot(
    val isRefreshing: Boolean = false,
    val lastAttemptAtMillis: Long? = null,
    val lastUpdatedAtMillis: Long? = null,
    val lastErrorMessage: String? = null,
) {
    fun toUiState(): SyncUiState = SyncUiState(
        isRefreshing = isRefreshing,
        errorMessage = lastErrorMessage,
        lastUpdatedAtMillis = lastUpdatedAtMillis,
    )
}

@Singleton
class ResourceSyncManager @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val snapshots = MutableStateFlow<Map<String, SyncSnapshot>>(emptyMap())

    fun observe(key: String): Flow<SyncUiState> = snapshots
        .map { state -> state[key]?.toUiState() ?: SyncUiState() }
        .distinctUntilChanged()

    fun peek(key: String): SyncUiState = snapshots.value[key]?.toUiState() ?: SyncUiState()

    suspend fun refresh(
        key: String,
        policy: SyncPolicy = SyncPolicies.Default,
        force: Boolean = true,
        refreshBlock: suspend () -> Result<Unit>,
    ): Result<Unit> {
        if (!force && !shouldRefresh(key, policy)) {
            return Result.success(Unit)
        }

        if (!markRefreshing(key)) {
            return Result.success(Unit)
        }

        val completedAt = System.currentTimeMillis()

        if (!networkMonitor.isOnline.value) {
            val offlineFailure = IllegalStateException(
                "Sei offline. Sto mostrando gli ultimi dati salvati, se disponibili."
            )
            finishRefresh(key, completedAt, Result.failure(offlineFailure))
            return Result.failure(offlineFailure)
        }

        val result = withContext(ioDispatcher) {
            runCatching {
                withTimeout(policy.timeoutMs) {
                    refreshBlock().getOrThrow()
                }
            }
        }

        finishRefresh(key, System.currentTimeMillis(), result)
        return result
    }

    suspend fun refreshIfStale(
        key: String,
        policy: SyncPolicy = SyncPolicies.Default,
        refreshBlock: suspend () -> Result<Unit>,
    ): Result<Unit> = refresh(
        key = key,
        policy = policy,
        force = false,
        refreshBlock = refreshBlock,
    )

    fun clearError(key: String) {
        snapshots.update { current ->
            val existing = current[key] ?: return@update current
            current + (key to existing.copy(lastErrorMessage = null))
        }
    }

    fun clearAll() {
        snapshots.value = emptyMap()
    }

    private fun shouldRefresh(key: String, policy: SyncPolicy): Boolean {
        val snapshot = snapshots.value[key] ?: return true
        if (snapshot.isRefreshing) return false

        val lastUpdatedAt = snapshot.lastUpdatedAtMillis ?: return true
        val lastAttemptAt = snapshot.lastAttemptAtMillis

        val failedAfterSuccess = snapshot.lastErrorMessage != null &&
            lastAttemptAt != null &&
            lastAttemptAt > lastUpdatedAt

        return failedAfterSuccess || System.currentTimeMillis() - lastUpdatedAt >= policy.staleAfterMs
    }

    private fun markRefreshing(key: String): Boolean {
        var started = false
        val startedAt = System.currentTimeMillis()

        snapshots.update { current ->
            val existing = current[key] ?: SyncSnapshot()
            if (existing.isRefreshing) {
                current
            } else {
                started = true
                current + (key to existing.copy(
                    isRefreshing = true,
                    lastAttemptAtMillis = startedAt,
                    lastErrorMessage = null,
                ))
            }
        }

        return started
    }

    private fun finishRefresh(
        key: String,
        completedAt: Long,
        result: Result<Unit>,
    ) {
        val isOnline = networkMonitor.isOnline.value

        snapshots.update { current ->
            val existing = current[key] ?: SyncSnapshot()
            val updated = result.fold(
                onSuccess = {
                    existing.copy(
                        isRefreshing = false,
                        lastAttemptAtMillis = completedAt,
                        lastUpdatedAtMillis = completedAt,
                        lastErrorMessage = null,
                    )
                },
                onFailure = { error ->
                    existing.copy(
                        isRefreshing = false,
                        lastAttemptAtMillis = completedAt,
                        lastErrorMessage = SyncErrorMapper.toMessage(error, isOnline),
                    )
                },
            )

            current + (key to updated)
        }
    }
}
