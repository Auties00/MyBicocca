package it.attendance100.mybicocca.ui.screen.settings

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.attendance100.mybicocca.core.os.ProvideHapticManager
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * State and behaviour coverage for the stateless settings landing directory. The screen owns no
 * ViewModel, so it is composed directly under the production [BicoccaTheme]; [ProvideHapticManager]
 * supplies the [it.attendance100.mybicocca.core.os.LocalHapticManager] the screen reads (its default
 * value errors). Tests anchor on [SettingsTestTags]: the directory renders the full Preferenze and
 * Informazioni groups, every entry is a clickable row, and tapping the Privacy Policy entry — the
 * one that only acknowledges with haptic feedback rather than opening a `hiltViewModel` sheet — keeps
 * the directory on screen. Entries that open modal sheets backed by a `hiltViewModel`
 * (Aspetto/Lingua/Sicurezza/Apertura file/About) and the Licenze entry that launches an external
 * Activity are asserted as present and clickable only, never tapped.
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
        compose.setContent {
            BicoccaTheme(dark = false) {
                ProvideHapticManager {
                    SettingsScreen()
                }
            }
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
    fun tapping_the_privacy_policy_entry_keeps_the_directory_on_screen() {
        setSettings()

        compose.onNodeWithTag(SettingsTestTags.entry("privacy")).performScrollTo().performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(SettingsTestTags.entry("privacy")).assertIsDisplayed()
    }
}
