package it.attendance100.mybicocca.ui.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.deeplink.PendingPresenceScan
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PresenceDeepLinkViewModel @Inject constructor(
    pendingPresenceScan: PendingPresenceScan,
) : ViewModel() {
    val pending: StateFlow<String?> = pendingPresenceScan.pending
}
