package it.attendance100.mybicocca.ui.screen.segreterie.taxes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.datastore.AuthTokenStore
import it.attendance100.mybicocca.data.model.career.Career
import it.attendance100.mybicocca.data.model.tax.TaxCharge
import it.attendance100.mybicocca.data.repository.CareerRepository
import it.attendance100.mybicocca.data.repository.TaxRepository
import it.attendance100.mybicocca.data.sync.ResourceSyncManager
import it.attendance100.mybicocca.data.sync.SyncKeys
import it.attendance100.mybicocca.data.sync.SyncPolicies
import it.attendance100.mybicocca.data.sync.SyncUiState
import it.attendance100.mybicocca.util.NetworkMonitor
import it.attendance100.mybicocca.util.awaitFirstNonNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TaxesViewModel @Inject constructor(
    private val taxRepository: TaxRepository,
    private val careerRepository: CareerRepository,
    private val authTokenStore: AuthTokenStore,
    private val resourceSyncManager: ResourceSyncManager,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    private val activeCareer = careerRepository.observeAll()
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val charges: StateFlow<List<TaxCharge>> = activeCareer
        .flatMapLatest { career ->
            career?.let { taxRepository.observeCharges(it.studentId) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val syncState: StateFlow<SyncUiState> = activeCareer
        .flatMapLatest { career ->
            career?.let { resourceSyncManager.observe(SyncKeys.taxes(it.studentId)) } ?: flowOf(SyncUiState())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncUiState())

    val isRefreshing: StateFlow<Boolean> = syncState
        .map { it.isRefreshing }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val error: StateFlow<String?> = syncState
        .map { it.errorMessage }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            activeCareer.collectLatest { career ->
                if (career != null) {
                    refreshCareer(career, force = false)
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            refreshCareer(activeCareer.awaitFirstNonNull(), force = true)
        }
    }

    fun clearError() {
        val career = activeCareer.value ?: return
        resourceSyncManager.clearError(SyncKeys.taxes(career.studentId))
    }

    private suspend fun refreshCareer(career: Career, force: Boolean) {
        val key = SyncKeys.taxes(career.studentId)
        val personId = authTokenStore.esse3PersonId
        val refreshBlock: suspend () -> Result<Unit> = {
            runCatching {
                taxRepository.refreshCharges(career.studentId, career.studentId).getOrThrow()
                if (personId != -1L) {
                    taxRepository.refreshInvoices(personId, career.studentId).getOrThrow()
                }
            }
        }

        if (force) {
            resourceSyncManager.refresh(key, SyncPolicies.Default, refreshBlock = refreshBlock)
        } else {
            resourceSyncManager.refreshIfStale(key, SyncPolicies.Default, refreshBlock = refreshBlock)
        }
    }
}
