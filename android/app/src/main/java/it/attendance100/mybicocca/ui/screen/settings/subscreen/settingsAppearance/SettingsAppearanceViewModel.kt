package it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.domain.model.settings.AppTheme
import it.attendance100.mybicocca.domain.model.settings.BadgeCardTheme
import it.attendance100.mybicocca.domain.model.settings.ThemeMode
import it.attendance100.mybicocca.domain.usecase.settings.ObserveAppThemeUseCase
import it.attendance100.mybicocca.domain.usecase.settings.ObserveBadgeCardThemeUseCase
import it.attendance100.mybicocca.domain.usecase.settings.ObserveThemeModeUseCase
import it.attendance100.mybicocca.domain.usecase.settings.SetAppThemeUseCase
import it.attendance100.mybicocca.domain.usecase.settings.SetBadgeCardThemeUseCase
import it.attendance100.mybicocca.domain.usecase.settings.SetThemeModeUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the "Aspetto" settings sheet. Exposes the persisted appearance preferences as
 * eagerly-shared streams — [appTheme] (the color palette), [themeMode] (system/light/dark), and
 * [badgeCardTheme] (the student-badge finish) — so the sheet's selection state and live previews
 * track the values the whole app themes with. [setAppTheme], [setThemeMode], and
 * [setBadgeCardTheme] persist a pick, which flows back through the streams.
 */
@HiltViewModel
class SettingsAppearanceViewModel @Inject constructor(
    observeAppTheme: ObserveAppThemeUseCase,
    observeThemeMode: ObserveThemeModeUseCase,
    observeBadgeCardTheme: ObserveBadgeCardThemeUseCase,
    private val setAppThemeUseCase: SetAppThemeUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val setBadgeCardThemeUseCase: SetBadgeCardThemeUseCase,
) : ViewModel() {

    val appTheme: StateFlow<AppTheme> = observeAppTheme()
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppTheme.Default)

    val themeMode: StateFlow<ThemeMode> = observeThemeMode()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.System)

    val badgeCardTheme: StateFlow<BadgeCardTheme> = observeBadgeCardTheme()
        .stateIn(viewModelScope, SharingStarted.Eagerly, BadgeCardTheme.Default)

    fun setAppTheme(theme: AppTheme) {
        viewModelScope.launch { setAppThemeUseCase(theme) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { setThemeModeUseCase(mode) }
    }

    fun setBadgeCardTheme(theme: BadgeCardTheme) {
        viewModelScope.launch { setBadgeCardThemeUseCase(theme) }
    }
}
