package it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance

import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.domain.model.settings.AppTheme
import it.attendance100.mybicocca.domain.model.settings.BadgeCardTheme
import it.attendance100.mybicocca.domain.model.settings.ThemeMode
import it.attendance100.mybicocca.domain.usecase.settings.ObserveAppThemeUseCase
import it.attendance100.mybicocca.domain.usecase.settings.ObserveBadgeCardThemeUseCase
import it.attendance100.mybicocca.domain.usecase.settings.ObserveThemeModeUseCase
import it.attendance100.mybicocca.domain.usecase.settings.SetAppThemeUseCase
import it.attendance100.mybicocca.domain.usecase.settings.SetBadgeCardThemeUseCase
import it.attendance100.mybicocca.domain.usecase.settings.SetThemeModeUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * Coverage for the appearance settings ViewModel: the three persisted preference streams
 * (palette, light/dark mode, badge finish) are mirrored as eagerly-shared state, and each
 * setter forwards the picked value to its persistence use case.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsAppearanceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeAppTheme: ObserveAppThemeUseCase = mockk()
    private val observeThemeMode: ObserveThemeModeUseCase = mockk()
    private val observeBadgeCardTheme: ObserveBadgeCardThemeUseCase = mockk()
    private val setAppThemeUseCase: SetAppThemeUseCase = mockk(relaxed = true)
    private val setThemeModeUseCase: SetThemeModeUseCase = mockk(relaxed = true)
    private val setBadgeCardThemeUseCase: SetBadgeCardThemeUseCase = mockk(relaxed = true)

    private fun viewModel(
        appTheme: AppTheme = AppTheme.Default,
        themeMode: ThemeMode = ThemeMode.System,
        badgeCardTheme: BadgeCardTheme = BadgeCardTheme.Default,
    ): SettingsAppearanceViewModel {
        every { observeAppTheme() } returns flowOf(appTheme)
        every { observeThemeMode() } returns flowOf(themeMode)
        every { observeBadgeCardTheme() } returns flowOf(badgeCardTheme)
        return SettingsAppearanceViewModel(
            observeAppTheme,
            observeThemeMode,
            observeBadgeCardTheme,
            setAppThemeUseCase,
            setThemeModeUseCase,
            setBadgeCardThemeUseCase,
        )
    }

    @Test
    fun `appTheme mirrors the persisted palette`() = runTest {
        val vm = viewModel(appTheme = AppTheme.Oceano)

        assertThat(vm.appTheme.value).isEqualTo(AppTheme.Oceano)
    }

    @Test
    fun `themeMode mirrors the persisted light dark preference`() = runTest {
        val vm = viewModel(themeMode = ThemeMode.Dark)

        assertThat(vm.themeMode.value).isEqualTo(ThemeMode.Dark)
    }

    @Test
    fun `badgeCardTheme mirrors the persisted badge finish`() = runTest {
        val vm = viewModel(badgeCardTheme = BadgeCardTheme.White)

        assertThat(vm.badgeCardTheme.value).isEqualTo(BadgeCardTheme.White)
    }

    @Test
    fun `setAppTheme persists the picked palette`() = runTest {
        val vm = viewModel()

        vm.setAppTheme(AppTheme.Bosco)

        coVerify { setAppThemeUseCase(AppTheme.Bosco) }
    }

    @Test
    fun `setThemeMode persists the picked light dark mode`() = runTest {
        val vm = viewModel()

        vm.setThemeMode(ThemeMode.Light)

        coVerify { setThemeModeUseCase(ThemeMode.Light) }
    }

    @Test
    fun `setBadgeCardTheme persists the picked badge finish`() = runTest {
        val vm = viewModel()

        vm.setBadgeCardTheme(BadgeCardTheme.White)

        coVerify { setBadgeCardThemeUseCase(BadgeCardTheme.White) }
    }
}
