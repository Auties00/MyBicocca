package it.attendance100.mybicocca.ui.component.modal

import androidx.activity.BackEventCompat
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.core.os.ProvideHapticManager
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

/**
 * Frame-by-frame motion of [SheetPager]: header and sheet height must follow the page transition
 * continuously — through a programmatic push/pop and through a predictive-back seek finished by
 * release — and come to rest exactly where a settled page lays them out, without a jump on the
 * last frame (the regression where the title snapped sideways and the height snapped once the
 * transition settled).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp")
class SheetPagerMotionTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    /** Page objects rebuilt on every recomposition, as the modal scene rebuilds its pages. */
    private class Page(val depth: Int)

    private var depth by mutableIntStateOf(0)

    private data class Frame(val height: Dp, val titleX: Float?)

    private fun title(depth: Int) = if (depth == 0) "Root title of the sheet" else "Detail title $depth"

    private fun setContent(initialDepth: Int) {
        depth = initialDepth
        rule.setContent {
            ProvideHapticManager(enabled = false) {
                SheetPager(
                    page = Page(depth),
                    depth = { it.depth },
                    key = { it.depth },
                    backTo = if (depth > 0) Page(depth - 1) else null,
                    onBack = { depth-- },
                    modifier = Modifier.testTag("pager"),
                    header = { page ->
                        SheetHeaderSpec(title = title(page.depth), subtitle = "subtitle ${page.depth}")
                    },
                ) { page ->
                    Column(Modifier.fillMaxWidth()) {
                        Text("body ${page.depth}")
                        Column(Modifier.width(10.dp).height(if (page.depth == 0) 300.dp else 150.dp)) {}
                    }
                }
            }
        }
        rule.waitForIdle()
        rule.mainClock.autoAdvance = false
    }

    private fun frame(depth: Int): Frame {
        val pager = rule.onNodeWithTag("pager").getUnclippedBoundsInRoot()
        val titleX = rule.onAllNodesWithText(title(depth)).fetchSemanticsNodes()
            .firstOrNull()?.let { with(rule.density) { it.boundsInRoot.left.toDp().value } }
        return Frame(pager.bottom - pager.top, titleX)
    }

    private fun assertContinuous(frames: List<Frame>) {
        frames.zipWithNext().forEach { (a, b) ->
            assertThat(abs((b.height - a.height).value)).isAtMost(30f)
            if (a.titleX != null && b.titleX != null) {
                assertThat(abs(b.titleX - a.titleX)).isAtMost(12f)
            }
        }
    }

    @Test
    fun `push and pop move continuously and settle in place`() {
        setContent(initialDepth = 0)
        val resting = frame(0)

        depth = 1
        val forward = List(30) { rule.mainClock.advanceTimeByFrame(); frame(1) }
        assertContinuous(forward)
        assertThat(forward.last().titleX).isWithin(1f).of(64f)

        depth = 0
        val back = List(30) { rule.mainClock.advanceTimeByFrame(); frame(0) }
        assertContinuous(back)
        assertThat(back.last()).isEqualTo(resting)
    }

    @Test
    fun `predictive back seeks, commits and settles without a jump`() {
        setContent(initialDepth = 1)
        rule.mainClock.autoAdvance = true
        rule.runOnIdle { depth = 0 }
        rule.waitForIdle()
        val resting = frame(0)
        rule.runOnIdle { depth = 1 }
        rule.waitForIdle()
        rule.mainClock.autoAdvance = false

        val dispatcher = rule.activity.onBackPressedDispatcher
        val frames = mutableListOf<Frame>()
        rule.runOnUiThread {
            dispatcher.dispatchOnBackStarted(BackEventCompat(0f, 0f, 0f, BackEventCompat.EDGE_LEFT))
        }
        for (step in 1..10) {
            rule.runOnUiThread {
                dispatcher.dispatchOnBackProgressed(
                    BackEventCompat(step * 20f, 0f, step * 0.05f, BackEventCompat.EDGE_LEFT),
                )
            }
            rule.mainClock.advanceTimeByFrame()
            frames += frame(0)
        }
        rule.runOnUiThread { dispatcher.onBackPressed() }
        repeat(30) {
            rule.mainClock.advanceTimeByFrame()
            frames += frame(0)
        }

        assertThat(depth).isEqualTo(0)
        assertContinuous(frames)
        assertThat(frames.last()).isEqualTo(resting)
    }
}
