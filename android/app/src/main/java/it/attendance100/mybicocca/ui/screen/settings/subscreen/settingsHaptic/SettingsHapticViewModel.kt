package it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsHaptic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.domain.usecase.settings.ObserveHapticEnabledUseCase
import it.attendance100.mybicocca.domain.usecase.settings.SetHapticEnabledUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Backs the "Vibrazione" settings sheet, exposing the persisted haptic-enabled flag. */
@HiltViewModel
class SettingsHapticViewModel @Inject constructor(
    observeHapticEnabled: ObserveHapticEnabledUseCase,
    private val setHapticEnabledUseCase: SetHapticEnabledUseCase,
) : ViewModel() {

    val enabled: StateFlow<Boolean> = observeHapticEnabled()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch { setHapticEnabledUseCase(enabled) }
    }
}
