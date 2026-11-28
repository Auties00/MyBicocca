package it.attendance100.mybicocca.ui.component.modal

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp")
class SheetContentHoldTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `slow drag scrolls child verticalScroll without moving outer sheet`() {
        var outerDragged = 0f
        var outerNestedScrolled = 0f
        lateinit var scrollState: androidx.compose.foundation.ScrollState

        rule.setContent {
            scrollState = rememberScrollState()
            val outerConnection = object : NestedScrollConnection {
                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    outerNestedScrolled += available.y
                    return Offset.Zero
                }

                override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                    outerNestedScrolled += available.y
                    return Velocity.Zero
                }
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .nestedScroll(outerConnection)
                    .draggable(rememberDraggableState { outerDragged += it }, Orientation.Vertical),
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .holdSheetFromContent(),
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .verticalScroll(scrollState)
                            .testTag("list"),
                    ) {
                        repeat(30) { Box(Modifier.fillMaxWidth().height(40.dp)) { Text("Row $it") } }
                    }
                }
            }
        }
        rule.waitForIdle()

        rule.onNodeWithTag("list").performTouchInput {
            down(center)
            // Small sub-touchSlop steps, matching a normal human drag on a high-refresh screen.
            repeat(40) { step ->
                advanceEventTime(16L)
                moveTo(Offset(center.x, center.y - (step + 1) * 5f))
            }
            up()
        }
        rule.waitForIdle()

        assertThat(scrollState.value).isGreaterThan(100)
        assertThat(outerDragged).isEqualTo(0f)
        assertThat(outerNestedScrolled).isEqualTo(0f)
    }

    @Test
    fun `drag on non-scrollable content does not move outer sheet`() {
        var outerDragged = 0f
        var clicked = 0

        rule.setContent {
            Box(
                Modifier
                    .fillMaxSize()
                    .draggable(rememberDraggableState { outerDragged += it }, Orientation.Vertical),
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .holdSheetFromContent()
                        .clickable { clicked++ }
                        .testTag("static"),
                )
            }
        }
        rule.waitForIdle()

        rule.onNodeWithTag("static").performClick()
        rule.waitForIdle()
        assertThat(clicked).isEqualTo(1)

        rule.onNodeWithTag("static").performTouchInput {
            down(center)
            repeat(30) { step ->
                advanceEventTime(16L)
                moveTo(Offset(center.x, center.y + (step + 1) * 8f))
            }
            up()
        }
        rule.waitForIdle()

        assertThat(outerDragged).isEqualTo(0f)
    }

    @Test
    fun `horizontal pager inside holdSheetFromContent still swipes smoothly`() {
        var outerDragged = 0f
        lateinit var pagerState: PagerState

        rule.setContent {
            pagerState = rememberPagerState { 2 }
            Box(
                Modifier
                    .fillMaxSize()
                    .draggable(rememberDraggableState { outerDragged += it }, Orientation.Vertical),
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .holdSheetFromContent(),
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize().testTag("pager"),
                    ) { page ->
                        Box(Modifier.fillMaxSize()) { Text("Page $page") }
                    }
                }
            }
        }
        rule.waitForIdle()

        rule.onNodeWithTag("pager").performTouchInput {
            down(Offset(right - 40f, centerY))
            repeat(40) { step ->
                advanceEventTime(16L)
                moveTo(Offset(right - 40f - (step + 1) * 15f, centerY))
            }
            up()
        }
        rule.waitForIdle()

        assertThat(pagerState.currentPage).isEqualTo(1)
        assertThat(outerDragged).isEqualTo(0f)
    }
}
