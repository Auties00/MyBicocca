package it.attendance100.mybicocca.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.local.settings.SecuritySettingsStore
import it.attendance100.mybicocca.manager.AppLockManager
import it.attendance100.mybicocca.manager.AppLockVerifier
import it.attendance100.mybicocca.manager.UnlockResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val store: SecuritySettingsStore,
    private val verifier: AppLockVerifier,
    private val appLockManager: AppLockManager,
) : ViewModel() {

    val appLockEnabled: StateFlow<Boolean> = store.appLockEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val verifying = MutableStateFlow(false)

    // Commits the master toggle after the change has been authorized. Treated as an unlock so
    // enabling the lock doesn't immediately re-challenge the user who just authenticated.
    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            store.setAppLockEnabled(enabled)
            appLockManager.unlock()
        }
    }

    // Password-fallback authorization for the toggle.
    fun verifyPassword(password: String, onResult: (UnlockResult) -> Unit) {
        if (verifying.value) return
        viewModelScope.launch {
            verifying.value = true
            val result = verifier.verifyPassword(password)
            verifying.value = false
            onResult(result)
        }
    }
}
