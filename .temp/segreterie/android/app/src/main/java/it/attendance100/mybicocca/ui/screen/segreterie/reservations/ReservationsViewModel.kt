package it.attendance100.mybicocca.ui.screen.segreterie.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.model.appointment.Appointment
import it.attendance100.mybicocca.data.repository.AppointmentRepository
import it.attendance100.mybicocca.data.sync.ResourceSyncManager
import it.attendance100.mybicocca.data.sync.SyncKeys
import it.attendance100.mybicocca.data.sync.SyncPolicies
import it.attendance100.mybicocca.data.sync.SyncUiState
import it.attendance100.mybicocca.util.NetworkMonitor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReservationsViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val resourceSyncManager: ResourceSyncManager,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    val appointments: StateFlow<List<Appointment>> = appointmentRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val syncState: StateFlow<SyncUiState> = resourceSyncManager.observe(SyncKeys.APPOINTMENTS_STUDENT)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncUiState())

    val isRefreshing: StateFlow<Boolean> = syncState
        .map { it.isRefreshing }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val error: StateFlow<String?> = syncState
        .map { it.errorMessage }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            resourceSyncManager.refreshIfStale(SyncKeys.APPOINTMENTS_STUDENT, SyncPolicies.Default) {
                appointmentRepository.refresh("STUDENTE")
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            resourceSyncManager.refresh(SyncKeys.APPOINTMENTS_STUDENT, SyncPolicies.Default) {
                appointmentRepository.refresh("STUDENTE")
            }
        }
    }

    fun clearError() {
        resourceSyncManager.clearError(SyncKeys.APPOINTMENTS_STUDENT)
    }
}
