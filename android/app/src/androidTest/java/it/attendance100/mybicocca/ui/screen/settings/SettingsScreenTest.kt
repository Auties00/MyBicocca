package it.attendance100.mybicocca.ui.screen.settings

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.attendance100.mybicocca.core.os.ProvideHapticManager
import it.attendance100.mybicocca.testing.setBicoccaContent
import it.attendance100.mybicocca.ui.navigation.AppNavigator
import it.attendance100.mybicocca.ui.navigation.LocalAppNavigator
import it.attendance100.mybicocca.ui.navigation.rememberAppNavigator
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * State and behaviour coverage for the stateless settings landing directory. The screen owns no
 * ViewModel, so it is composed directly under the production [BicoccaTheme]; [ProvideHapticManager]
 * supplies the [it.attendance100.mybicocca.core.os.LocalHapticManager] the screen reads (its default
 * value errors). Tests anchor on [SettingsTestTags]: the directory renders the full Preferenze and
 * Informazioni groups, every entry is a clickable row, and tapping the Privacy Policy entry — the
 * one that only acknowledges with haptic feedback rather than opening a sheet — keeps the
 * directory on screen. The sheet entries (Aspetto/Lingua/Sicurezza/Apertura file/Vibrazione/About)
 * push their [SheetRoute] on the shell's [AppNavigator]; with a bare navigator provided (and no
 * NavDisplay rendering the stack, so no `hiltViewModel` page is composed) a tap is asserted to push
 * exactly that route. The Licenze entry, which launches an external Activity, is never tapped.
 */
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val entryIds = listOf(
        "appearance",
        "language",
        "app_lock",
        "file_open",
        "about",
        "privacy",
        "license",
    )

    private fun setSettings() {
        compose.setBicoccaContent {
            SettingsScreen()
        }
    }

    @Test
    fun the_directory_renders_its_root_and_every_settings_entry() {
        setSettings()

        compose.onNodeWithTag(SettingsTestTags.ROOT).assertIsDisplayed()
        entryIds.forEach { id ->
            compose.onNodeWithTag(SettingsTestTags.entry(id)).assertExists()
        }
    }

    @Test
    fun every_settings_entry_is_a_clickable_row() {
        setSettings()

        entryIds.forEach { id ->
            compose.onNodeWithTag(SettingsTestTags.entry(id)).assertHasClickAction()
        }
    }

    @Test
    fun tapping_a_sheet_entry_pushes_its_sheet_route() {
        val sheetRoutes = mapOf(
            "appearance" to SheetRoute.SettingsAppearance,
            "language" to SheetRoute.SettingsLanguage,
            "app_lock" to SheetRoute.SettingsSecurity,
            "file_open" to SheetRoute.FileAssociations,
            "haptic" to SheetRoute.SettingsHaptic,
            "about" to SheetRoute.AppInfo,
        )
        lateinit var navigator: AppNavigator
        compose.setBicoccaContent {
            navigator = rememberAppNavigator()
            CompositionLocalProvider(LocalAppNavigator provides navigator) {
                SettingsScreen()
            }
        }

        sheetRoutes.forEach { (id, route) ->
            compose.onNodeWithTag(SettingsTestTags.entry(id)).performScrollTo().performClick()
            compose.waitForIdle()

            assertEquals(route, navigator.top.route)
            compose.runOnIdle { navigator.popToRoot() }
        }
    }

    @Test
    fun tapping_the_privacy_policy_entry_keeps_the_directory_on_screen() {
        setSettings()

        compose.onNodeWithTag(SettingsTestTags.entry("privacy")).performScrollTo().performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(SettingsTestTags.entry("privacy")).assertIsDisplayed()
    }
}
