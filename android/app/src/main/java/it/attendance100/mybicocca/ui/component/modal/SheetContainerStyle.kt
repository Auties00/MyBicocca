package it.attendance100.mybicocca.ui.component.modal

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

/**
 * Container look of a sheet, for the few sheets with their own visual identity (e.g. the account
 * switcher's large corners and custom handle). Unset values keep the app's standard sheet.
 *
 * @param dragToDismiss false for a sheet that a swipe down must not close (a long flow, where
 * scrolling up through a page too eagerly would drop it); back and the scrim still close it.
 * @param firmDrag a swipe down closes the sheet only when it is clearly meant to: a longer pull or
 * a quick fling; a short or slow one springs back.
 */
class SheetContainerStyle(
    val shape: Shape? = null,
    val dragToDismiss: Boolean = true,
    val firmDrag: Boolean = false,
    val dragHandle: (@Composable () -> Unit)? = null,
    val hideDragHandle: Boolean = false,
    val contentWindowInsets: (@Composable () -> WindowInsets)? = null,
    val scrimColor: Color? = null,
)
