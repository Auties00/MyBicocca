package it.attendance100.mybicocca.ui.component.modal

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.DragScope
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity

/**
 * Keeps a sheet where it is while its content is dragged or scrolled, so only the drag handle and
 * the header move it. Otherwise a scroll that runs past the content's top, a fling, or a drag on
 * content that does not scroll pulls the whole sheet down and closes it, losing what was on it.
 * Scrolling and swiping inside the content work as before: they see every touch first.
 */
fun Modifier.holdSheetFromContent(): Modifier = this
    .nestedScroll(SwallowLeftoverScroll)
    .draggable(state = AbsorbDrag, orientation = Orientation.Vertical)

/** Absorbs vertical drags that no child claimed, waiting for touch slop so child scrollables can start. */
private object AbsorbDrag : DraggableState {
    private val scope = object : DragScope {
        override fun dragBy(pixels: Float) = Unit
    }

    override suspend fun drag(dragPriority: MutatePriority, block: suspend DragScope.() -> Unit) {
        block(scope)
    }

    override fun dispatchRawDelta(delta: Float) = Unit
}

/** Keeps what the content's own scrolling leaves over, so the sheet behind it never gets it. */
private object SwallowLeftoverScroll : NestedScrollConnection {
    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset = available

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = available
}
