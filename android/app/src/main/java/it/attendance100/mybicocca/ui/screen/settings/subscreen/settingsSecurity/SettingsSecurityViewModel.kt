package it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsSecurity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.domain.model.security.UnlockResult
import it.attendance100.mybicocca.domain.usecase.security.ObserveAppLockEnabledUseCase
import it.attendance100.mybicocca.domain.usecase.security.ObserveLockTimeoutUseCase
import it.attendance100.mybicocca.domain.usecase.security.ObserveSecureScreenUseCase
import it.attendance100.mybicocca.domain.usecase.security.SetAppLockEnabledUseCase
import it.attendance100.mybicocca.domain.usecase.security.SetLockTimeoutUseCase
import it.attendance100.mybicocca.domain.usecase.security.SetSecureScreenUseCase
import it.attendance100.mybicocca.domain.usecase.security.UnlockAppUseCase
import it.attendance100.mybicocca.domain.usecase.security.VerifyAppLockPasswordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the "Sicurezza" settings sheet. Streams the persisted app-lock preferences as
 * eagerly-shared state — [enabled] (master toggle), [timeoutMinutes] (inactivity threshold) and
 * [secureScreen] (hide previews / block screenshots) — plus [verifying], the in-flight flag for
 * password checks that drops overlapping verification attempts. Actions persist the preferences
 * ([setEnabled], [setTimeout], [setSecureScreen]) and authorize the master toggle's password
 * fallback via [verifyPassword].
 */
@HiltViewModel
class SettingsSecurityViewModel @Inject constructor(
    observeAppLockEnabled: ObserveAppLockEnabledUseCase,
    observeLockTimeout: ObserveLockTimeoutUseCase,
    observeSecureScreen: ObserveSecureScreenUseCase,
    private val setAppLockEnabled: SetAppLockEnabledUseCase,
    private val setLockTimeoutMinutes: SetLockTimeoutUseCase,
    private val setSecureScreenEnabled: SetSecureScreenUseCase,
    private val verifyAppLockPassword: VerifyAppLockPasswordUseCase,
    private val unlockApp: UnlockAppUseCase,
) : ViewModel() {

    val enabled: StateFlow<Boolean> = observeAppLockEnabled()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val timeoutMinutes: StateFlow<Int> = observeLockTimeout()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val secureScreen: StateFlow<Boolean> = observeSecureScreen()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _verifying = MutableStateFlow(false)
    val verifying: StateFlow<Boolean> = _verifying.asStateFlow()

    /**
     * Commits the master toggle after the change has been authorized, treating the change as an
     * unlock so enabling the lock does not immediately re-challenge the user who just
     * authenticated.
     */
    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch {
            setAppLockEnabled(enabled)
            unlockApp()
        }
    }

    fun setTimeout(minutes: Int) {
        viewModelScope.launch { setLockTimeoutMinutes(minutes) }
    }

    fun setSecureScreen(enabled: Boolean) {
        viewModelScope.launch { setSecureScreenEnabled(enabled) }
    }

    /** Password-fallback authorization for the master toggle, used when biometrics are unavailable. */
    fun verifyPassword(password: String, onResult: (UnlockResult) -> Unit) {
        if (_verifying.value) return
        viewModelScope.launch {
            _verifying.value = true
            val result = verifyAppLockPassword(password)
            _verifying.value = false
            onResult(result)
        }
    }
}
