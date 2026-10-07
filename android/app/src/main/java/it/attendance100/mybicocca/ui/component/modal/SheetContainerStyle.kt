package it.attendance100.mybicocca.ui.component.modal

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

/**
 * Container look of a sheet, for the few sheets with their own visual identity (e.g. the account
 * switcher's large corners and custom handle). Unset values keep the app's standard sheet.
 */
class SheetContainerStyle(
    val shape: Shape? = null,
    val dragHandle: (@Composable () -> Unit)? = null,
    val hideDragHandle: Boolean = false,
    val contentWindowInsets: (@Composable () -> WindowInsets)? = null,
    val scrimColor: Color? = null,
)
