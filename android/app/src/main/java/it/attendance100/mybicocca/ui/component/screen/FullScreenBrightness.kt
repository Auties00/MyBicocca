package it.attendance100.mybicocca.ui.component.screen

import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/**
 * Turns the screen to full brightness while this is on screen, so a barcode scans at the till
 * whatever the room's light, as a wallet does with a ticket. The brightness the window had comes
 * back when it leaves.
 */
@Composable
fun FullScreenBrightness() {
    val window = LocalActivity.current?.window ?: return
    DisposableEffect(window) {
        val previous = window.attributes.screenBrightness
        window.attributes = window.attributes.apply {
            screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        }
        onDispose {
            window.attributes = window.attributes.apply { screenBrightness = previous }
        }
    }
}
