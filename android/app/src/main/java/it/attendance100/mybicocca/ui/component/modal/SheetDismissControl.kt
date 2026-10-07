package it.attendance100.mybicocca.ui.component.modal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Lets a sheet page drive its sheet's swipe/scrim/back dismissal from its OWN state — a
 * mid-wizard gesture lock and a dismiss veto that morphs a dismissal into a confirm page ("uscire
 * senza inviare?"). Each page of a sheet gets its own control (via [LocalSheetDismissControl]) and
 * the sheet obeys the control of the page currently on top, so a page pushed above a wizard (e.g.
 * the file chooser) never inherits the wizard's stale lock.
 *
 * [dismiss] closes the whole sheet; it is idempotent.
 */
@Stable
class SheetDismissControl(val dismiss: () -> Unit) {
    var gesturesEnabled: Boolean by mutableStateOf(true)
    var confirmDismiss: () -> Boolean by mutableStateOf({ true })
}

/** Provided around every sheet page; null when not hosted inside a sheet. */
val LocalSheetDismissControl = staticCompositionLocalOf<SheetDismissControl?> { null }

/**
 * Keeps the calling page's sheet open while [locked] (e.g. mid-submission): no drag, scrim or back
 * dismissal, and the drag handle is muted. A no-op outside a sheet.
 */
@Composable
fun LockSheetWhile(locked: Boolean) {
    val control = LocalSheetDismissControl.current ?: return
    SideEffect {
        control.gesturesEnabled = !locked
        control.confirmDismiss = { !locked }
    }
}
