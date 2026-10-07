package it.attendance100.mybicocca.ui.navigation.scene

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.core.os.ProvideHapticManager
import it.attendance100.mybicocca.ui.component.modal.SheetHeaderSpec
import it.attendance100.mybicocca.ui.navigation.AppNavigator
import it.attendance100.mybicocca.ui.navigation.DisposableEffectOnPop
import it.attendance100.mybicocca.ui.navigation.LocalAppNavigator
import it.attendance100.mybicocca.ui.navigation.rememberEntryPopActionsDecorator
import it.attendance100.mybicocca.ui.navigation.destinationEntryProvider
import it.attendance100.mybicocca.ui.navigation.rememberAppNavigator
import it.attendance100.mybicocca.ui.navigation.route.AppRoute
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * End-to-end behaviour of the modal scene on a real NavDisplay, wired like MainShell: the
 * regressions behind issues #44 (a second copy of a sheet composed next to the first, crashing
 * with "Key … was used multiple times"), #14 (the sheet torn down and re-created on every page
 * change instead of morphing) and #22 (dismissals popping more than the sheet).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ModalSceneStrategyTest {

    @get:Rule
    val rule = createComposeRule()

    /** How many times each sheet page's content was freshly composed (not just recomposed). */
    private val compositions = mutableMapOf<String, Int>()

    /** Sheet page contents currently in composition, per label. */
    private val live = mutableMapOf<String, Int>()

    /** How many times each page's "left the back stack" action ran. */
    private val popped = mutableMapOf<String, Int>()

    @Composable
    private fun Page(label: String) {
        remember { compositions[label] = (compositions[label] ?: 0) + 1 }
        DisposableEffect(Unit) {
            live[label] = (live[label] ?: 0) + 1
            onDispose { live[label] = (live[label] ?: 1) - 1 }
        }
        DisposableEffectOnPop { popped[label] = (popped[label] ?: 0) + 1 }
        // Saveable state is what crashes when one entry is composed twice at the same time.
        val restored = rememberSaveable { label }
        Text(restored)
    }

    @Composable
    private fun Shell(navigator: AppNavigator, recompositionKey: Int = 0) {
        @Suppress("UNUSED_EXPRESSION") recompositionKey
        val entries = rememberDecoratedNavEntries(
            backStack = navigator.keys,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberEntryPopActionsDecorator(),
            ),
            entryProvider = destinationEntryProvider(entryProvider {
                entry<AppRoute.TabRoot> { Text("tabs") }
                entry<AppRoute.Profile> { Text("profile page") }
                entry<AppRoute.FileViewer> { key -> Text("viewer ${key.fileName}") }
                entry<SheetRoute.Isee>(
                    metadata = sheetHeader<SheetRoute.Isee> { SheetHeaderSpec(title = "ISEE") },
                ) { Page("isee list") }
                entry<SheetRoute.IseeDetail>(
                    metadata = sheetHeader<SheetRoute.IseeDetail> { key ->
                        SheetHeaderSpec(title = "ISEE ${key.year}")
                    },
                ) { key -> Page("isee ${key.year}") }
                entry<SheetRoute.Appelli> { Page("appelli") }
            }),
        )
        val strategies = remember(navigator) {
            listOf(ModalSceneStrategy(navigator), SinglePaneSceneStrategy())
        }
        ProvideHapticManager(enabled = false) {
            CompositionLocalProvider(
                LocalAppNavigator provides navigator,
                LocalModalEntries provides entries,
            ) {
                NavDisplay(
                    entries = entries,
                    onBack = { navigator.back() },
                    sceneStrategies = strategies,
                )
            }
        }
    }

    @Test
    fun `shell recompositions never re-create an open sheet`() {
        var navigator: AppNavigator? = null
        var trigger by mutableIntStateOf(0)
        rule.setContent {
            navigator = rememberAppNavigator()
            Shell(navigator!!, trigger)
        }
        rule.runOnIdle { navigator!!.navigate(SheetRoute.Isee) }
        rule.waitForIdle()
        repeat(5) {
            trigger++
            rule.waitForIdle()
        }
        rule.onNodeWithText("isee list").assertExists()
        assertThat(compositions["isee list"]).isEqualTo(1)
        assertThat(live["isee list"]).isEqualTo(1)
    }

    @Test
    fun `in-sheet pages navigate inside one sheet and back`() {
        var navigator: AppNavigator? = null
        rule.setContent {
            navigator = rememberAppNavigator()
            Shell(navigator!!)
        }
        rule.runOnIdle { navigator!!.navigate(SheetRoute.Isee) }
        rule.waitForIdle()
        rule.runOnIdle { navigator!!.navigate(SheetRoute.IseeDetail(2024)) }
        rule.waitForIdle()
        rule.onNodeWithText("isee 2024").assertExists()
        rule.onNodeWithText("ISEE 2024").assertExists()
        // The list page left the sheet's body (one page shown at a time) without a second sheet.
        assertThat(live["isee list"] ?: 0).isEqualTo(0)
        assertThat(live["isee 2024"]).isEqualTo(1)

        rule.runOnIdle { navigator!!.back() }
        rule.waitForIdle()
        rule.onNodeWithText("isee list").assertExists()
        assertThat(live["isee 2024"] ?: 0).isEqualTo(0)
    }

    @Test
    fun `a stacked sheet and repeated dismissals never pop below the sheet`() {
        var navigator: AppNavigator? = null
        rule.setContent {
            navigator = rememberAppNavigator()
            Shell(navigator!!)
        }
        rule.runOnIdle {
            navigator!!.navigate(AppRoute.Profile)
            navigator!!.navigate(SheetRoute.Isee)
            navigator!!.navigate(SheetRoute.Appelli)
        }
        rule.waitForIdle()
        rule.onNodeWithText("isee list").assertExists()
        rule.onNodeWithText("appelli").assertExists()

        rule.runOnIdle {
            val iseeRoot = navigator!!.entries.first { it.route == SheetRoute.Isee }.id
            repeat(3) { navigator!!.pop(iseeRoot) }
        }
        rule.waitForIdle()
        rule.runOnIdle {
            assertThat(navigator!!.entries.map { it.route })
                .containsExactly(AppRoute.TabRoot, AppRoute.Profile).inOrder()
        }
        rule.onNodeWithText("profile page").assertExists()
    }

    @Test
    fun `an open sheet with an in-sheet page survives process death`() {
        val restoration = StateRestorationTester(rule)
        var navigator: AppNavigator? = null
        restoration.setContent {
            navigator = rememberAppNavigator()
            Shell(navigator!!)
        }
        rule.runOnIdle {
            navigator!!.navigate(SheetRoute.Isee)
            navigator!!.navigate(SheetRoute.IseeDetail(2023))
        }
        rule.waitForIdle()
        val idsBefore = rule.runOnIdle { navigator!!.entries.map { it.id } }

        restoration.emulateSavedInstanceStateRestore()
        rule.waitForIdle()

        rule.runOnIdle {
            assertThat(navigator!!.entries.map { it.id }).isEqualTo(idsBefore)
            assertThat(navigator!!.top.route).isEqualTo(SheetRoute.IseeDetail(2023))
        }
        rule.onNodeWithText("isee 2023").assertExists()
        assertThat(live["isee 2023"]).isEqualTo(1)
    }

    @Test
    fun `a sheet hidden under a full-screen page keeps its flow until it is really popped`() {
        var navigator: AppNavigator? = null
        rule.setContent {
            navigator = rememberAppNavigator()
            Shell(navigator!!)
        }
        rule.runOnIdle { navigator!!.navigate(SheetRoute.Appelli) }
        rule.waitForIdle()
        rule.runOnIdle { navigator!!.navigate(AppRoute.FileViewer("slip.pdf")) }
        rule.waitForIdle()
        rule.onNodeWithText("viewer slip.pdf").assertExists()
        // The sheet slid away and left composition, but it is still on the stack.
        assertThat(live["appelli"] ?: 0).isEqualTo(0)
        assertThat(popped["appelli"] ?: 0).isEqualTo(0)

        // Clearing the stack while the sheet is not composed still runs its pop action, once.
        rule.runOnIdle { navigator!!.popToRoot() }
        rule.waitForIdle()
        assertThat(popped["appelli"]).isEqualTo(1)
    }

    @Test
    fun `returning from a full-screen page brings the sheet back`() {
        var navigator: AppNavigator? = null
        rule.setContent {
            navigator = rememberAppNavigator()
            Shell(navigator!!)
        }
        rule.runOnIdle { navigator!!.navigate(SheetRoute.Appelli) }
        rule.waitForIdle()
        rule.runOnIdle { navigator!!.navigate(AppRoute.FileViewer("slip.pdf")) }
        rule.waitForIdle()
        rule.runOnIdle { navigator!!.back() }
        rule.waitForIdle()
        rule.onNodeWithText("appelli").assertExists()
        assertThat(live["appelli"]).isEqualTo(1)
        assertThat(popped["appelli"] ?: 0).isEqualTo(0)
    }
}
