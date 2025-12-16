package it.attendance100.mybicocca.util

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

@Composable
fun rememberPageNestedScrollConnection(
    state: PagerState,
    orientation: Orientation = Orientation.Horizontal
): NestedScrollConnection {
    return remember(state, orientation) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return if (available.x > 0 && state.currentPage == 0 && kotlin.math.abs(state.currentPageOffsetFraction) < 0.01f) {
                    // We are at the start and scrolling right, we consume the scroll so the Pager doesn't get it
                    available
                } else {
                    Offset.Zero
                }
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                return if (available.x > 0 && state.currentPage == 0 && kotlin.math.abs(state.currentPageOffsetFraction) < 0.01f) {
                    available
                } else {
                    Velocity.Zero
                }
            }
        }
    }
}
