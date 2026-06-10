package it.attendance100.mybicocca.ui.screen.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.domain.model.security.UnlockResult
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.security.ObserveAppLockUseCase
import it.attendance100.mybicocca.domain.usecase.security.UnlockAppUseCase
import it.attendance100.mybicocca.domain.usecase.security.VerifyAppLockPasswordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the app-lock gate. [locked] mirrors the domain lock state and [username] labels
 * the lock screen with the active account; [verifying] is true while a password check is
 * in flight, with re-entrant submissions dropped. [onBiometricSuccess] unlocks directly,
 * while [verifyPassword] checks the fallback password, unlocks on success, and reports the
 * outcome to the caller for inline error copy.
 */
@HiltViewModel
class AppLockViewModel @Inject constructor(
    observeAppLock: ObserveAppLockUseCase,
    private val unlockApp: UnlockAppUseCase,
    private val verifyAppLockPassword: VerifyAppLockPasswordUseCase,
    observeActiveAccount: ObserveActiveAccountUseCase,
) : ViewModel() {

    val locked: StateFlow<Boolean> = observeAppLock()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val username: StateFlow<String?> = observeActiveAccount()
        .map { it?.username }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _verifying = MutableStateFlow(false)
    val verifying: StateFlow<Boolean> = _verifying.asStateFlow()

    fun onBiometricSuccess() = unlockApp()

    fun verifyPassword(password: String, onResult: (UnlockResult) -> Unit) {
        if (_verifying.value) return

        viewModelScope.launch {
            _verifying.value = true
            val result = verifyAppLockPassword(password)
            if (result == UnlockResult.Success) unlockApp()

            _verifying.value = false
            onResult(result)
        }
    }
}
