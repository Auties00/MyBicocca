package it.attendance100.mybicocca.ui.component.modal

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.Velocity

/**
 * Keeps a sheet where it is while its content is dragged or scrolled, so only the drag handle and
 * the header move it. Otherwise a scroll that runs past the content's top, a fling, or a drag on
 * content that does not scroll pulls the whole sheet down and closes it, losing what was on it.
 * Scrolling and swiping inside the content work as before: they see every touch first.
 */
fun Modifier.holdSheetFromContent(): Modifier = this
    .nestedScroll(SwallowLeftoverScroll)
    .pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            do {
                val event = awaitPointerEvent()
                // The content has had these moves already; taking them leaves none for the sheet.
                event.changes.forEach { if (it.positionChange() != Offset.Zero) it.consume() }
            } while (event.changes.any { it.pressed })
        }
    }

/** Keeps what the content's own scrolling leaves over, so the sheet behind it never gets it. */
private object SwallowLeftoverScroll : NestedScrollConnection {
    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset = available

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = available
}
