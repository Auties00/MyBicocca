package it.attendance100.mybicocca.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.domain.usecase.attendance.ObservePendingPresenceScanUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Exposes the pending mod_attendance deep link to the shell, which routes to the Presenze
 * screen when one shows up. The link itself is consumed there, not here.
 */
@HiltViewModel
class PresenceDeepLinkViewModel @Inject constructor(
    observePendingPresenceScan: ObservePendingPresenceScanUseCase,
) : ViewModel() {
    val pending: StateFlow<String?> = observePendingPresenceScan()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
