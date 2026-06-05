package it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsSecurity

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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsSecurityViewModel @Inject constructor(
    private val store: SecuritySettingsStore,
    private val verifier: AppLockVerifier,
    private val appLockManager: AppLockManager,
) : ViewModel() {

    val enabled: StateFlow<Boolean> = store.appLockEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val timeoutMinutes: StateFlow<Int> = store.lockTimeoutMinutes
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val secureScreen: StateFlow<Boolean> = store.secureScreenEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _verifying = MutableStateFlow(false)
    val verifying: StateFlow<Boolean> = _verifying.asStateFlow()

    /**
     * Commits the master toggle after the change has been authorized. Treated as an unlock so
     * enabling the lock doesn't immediately re-challenge the user who just authenticated.
     */
    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch {
            store.setAppLockEnabled(enabled)
            appLockManager.unlock()
        }
    }

    fun setTimeout(minutes: Int) {
        viewModelScope.launch { store.setLockTimeoutMinutes(minutes) }
    }

    fun setSecureScreen(enabled: Boolean) {
        viewModelScope.launch { store.setSecureScreenEnabled(enabled) }
    }

    /** Password-fallback authorization for the toggle. */
    fun verifyPassword(password: String, onResult: (UnlockResult) -> Unit) {
        if (_verifying.value) return
        viewModelScope.launch {
            _verifying.value = true
            val result = verifier.verifyPassword(password)
            _verifying.value = false
            onResult(result)
        }
    }
}
