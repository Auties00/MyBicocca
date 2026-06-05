package it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.local.settings.AppearanceSettingsStore
import it.attendance100.mybicocca.data.local.settings.ThemeMode
import it.attendance100.mybicocca.ui.theme.AppTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsAppearanceViewModel @Inject constructor(
    private val store: AppearanceSettingsStore,
) : ViewModel() {

    val appTheme: StateFlow<AppTheme> = store.appTheme
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppTheme.Default)

    val themeMode: StateFlow<ThemeMode> = store.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.System)

    fun setAppTheme(theme: AppTheme) {
        viewModelScope.launch { store.setAppTheme(theme) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { store.setThemeMode(mode) }
    }
}
