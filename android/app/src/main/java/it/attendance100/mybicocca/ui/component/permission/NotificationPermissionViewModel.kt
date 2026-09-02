package it.attendance100.mybicocca.ui.component.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.local.settings.NotificationSettingsStore
import it.attendance100.mybicocca.data.notification.NotificationPermissions
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationPermissionViewModel @Inject constructor(
    private val permissions: NotificationPermissions,
    private val store: NotificationSettingsStore,
) : ViewModel() {

    /**
     * Whether to explain ourselves before triggering the system prompt. Starts false so a slow
     * DataStore read can never flash a dialog at someone who has already answered.
     */
    val shouldAsk: StateFlow<Boolean> = store.permissionAsked
        .map { asked -> !asked && permissions.needsPermissionRequest() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * Marks the ask as spent, whichever way the user answered. A decline is recorded exactly like
     * an accept: Android surfaces its dialog for the first two requests only, and re-asking on
     * every launch would burn that on someone who has already said no.
     */
    fun markAsked() {
        viewModelScope.launch { store.setPermissionAsked() }
    }
}
