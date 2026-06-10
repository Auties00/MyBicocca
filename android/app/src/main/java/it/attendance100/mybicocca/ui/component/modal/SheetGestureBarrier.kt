package it.attendance100.mybicocca.ui.component.modal

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Velocity

/**
 * Contains vertical gestures within a sheet page body so they never drag the sheet:
 * the content's scrollables keep working, their leftover scroll/fling is swallowed
 * before the sheet's nested-scroll connection can turn it into a dismissal, and plain
 * vertical drags on non-scrollable body areas are consumed in place. Applied below the
 * pinned header, it leaves the header (and drag handle) as the only swipe surface.
 */
fun Modifier.sheetBodyGestureBarrier(): Modifier = this
    .nestedScroll(SwallowLeftoverScrollConnection)
    .pointerInput(Unit) {
        detectVerticalDragGestures(
            onVerticalDrag = { change, _ -> change.consume() },
        )
    }

private object SwallowLeftoverScrollConnection : NestedScrollConnection {
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset = available

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = available
}
