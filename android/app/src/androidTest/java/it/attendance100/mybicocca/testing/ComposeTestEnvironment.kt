package it.attendance100.mybicocca.testing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import io.mockk.mockk
import it.attendance100.mybicocca.core.os.DeviceType
import it.attendance100.mybicocca.core.os.HapticManager
import it.attendance100.mybicocca.core.os.LocalDeviceType
import it.attendance100.mybicocca.core.os.LocalHapticManager
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.component.feedback.rememberAppSnackbarController
import it.attendance100.mybicocca.ui.theme.BicoccaTheme

/**
 * Instrumented-test twin of the unit-test `setBicoccaContent` (the androidTest source set cannot see
 * `src/test`, so the helper is duplicated here). Installs the three app-wide CompositionLocals that
 * `error()` when read without a provider — [LocalHapticManager], [LocalAppSnackbarController],
 * [LocalDeviceType] — then the production [BicoccaTheme], so a screen renders on a real device the
 * same way the shell mounts it.
 */
@Composable
fun BicoccaTestEnvironment(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalHapticManager provides remember { mockk<HapticManager>(relaxed = true) },
        LocalAppSnackbarController provides rememberAppSnackbarController(),
        LocalDeviceType provides DeviceType.Phone,
        it.attendance100.mybicocca.core.os.LocalIsTestEnvironment provides true,
    ) {
        BicoccaTheme(dark = false, content = content)
    }
}

/** Sets [content] wrapped in the full [BicoccaTestEnvironment]; the entry point for instrumented screen tests. */
fun ComposeContentTestRule.setBicoccaContent(content: @Composable () -> Unit) {
    setContent { BicoccaTestEnvironment(content) }
}
