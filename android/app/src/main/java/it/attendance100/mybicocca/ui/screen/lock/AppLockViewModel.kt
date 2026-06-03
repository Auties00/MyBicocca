package it.attendance100.mybicocca.ui.screen.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.manager.AppLockManager
import it.attendance100.mybicocca.manager.AppLockVerifier
import it.attendance100.mybicocca.manager.UnlockResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val appLockManager: AppLockManager,
    private val verifier: AppLockVerifier,
    observeActiveAccount: ObserveActiveAccountUseCase,
) : ViewModel() {

    val locked: StateFlow<Boolean> = appLockManager.locked

    val username: StateFlow<String?> = observeActiveAccount()
        .map { it?.username }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _verifying = MutableStateFlow(false)
    val verifying: StateFlow<Boolean> = _verifying.asStateFlow()

    fun onBiometricSuccess() = appLockManager.unlock()

    fun verifyPassword(password: String, onResult: (UnlockResult) -> Unit) {
        if (_verifying.value) return

        viewModelScope.launch {
            _verifying.value = true
            val result = verifier.verifyPassword(password)
            if (result == UnlockResult.Success) appLockManager.unlock()

            _verifying.value = false
            onResult(result)
        }
    }
}
