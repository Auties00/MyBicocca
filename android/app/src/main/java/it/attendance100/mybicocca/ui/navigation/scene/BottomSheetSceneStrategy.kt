package it.attendance100.mybicocca.ui.navigation.scene

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.component.modal.SheetPagerHeader
import it.attendance100.mybicocca.ui.component.modal.sheetBodyGestureBarrier
import it.attendance100.mybicocca.ui.component.modal.sheetPageTransform

// A multi-page modal whose pages are real back-stack entries. A run of consecutive top-of-stack
// entries that share the same `group` is rendered as ONE persistent bottom sheet: the top entry
// is visible, the rest form the in-sheet back stack. Pushing/popping entries morphs the visible
// page inside the same sheet (the sheet is never torn down between pages), reusing the app's
// existing PredictiveModalBottomSheet container, SheetPagerHeader and sheetPageTransform.
//
// Mark each page with metadata = sheetEntry("<group>") { depth -> SheetHeaderSpec(...) } in the
// entryProvider, then push the page's NavKey onto the app back stack to open / navigate it.

// Per-page pinned header, resolved in composition so the title/subtitle can read live ViewModel
// state (e.g. a "3 prenotazioni" counter). Return null to draw no header for that page — used by
// single-entry sheets that render their own internal morphing header.
data class SheetHeaderSpec(
    val title: String,
    val subtitle: CharSequence? = null,
    val onSubtitleClick: (() -> Unit)? = null,
)

class SheetEntryOptions(
    val group: String,
    val header: @Composable (depth: Int) -> SheetHeaderSpec? = { null },
)

const val SheetEntryMetadataKey: String = "it.attendance100.mybicocca.sheetEntry"

// Metadata builder for entry<Route>(metadata = sheetEntry("group") { ... }) { ... }.
fun sheetEntry(
    group: String,
    header: @Composable (depth: Int) -> SheetHeaderSpec? = { null },
): Map<String, Any> = mapOf(
    SheetEntryMetadataKey to SheetEntryOptions(group, header),
)

private fun NavEntry<*>.sheetOptions(): SheetEntryOptions? =
    metadata[SheetEntryMetadataKey] as? SheetEntryOptions

// Lets a single-entry sheet's content drive the container's swipe/scrim dismissal from its OWN
// internal state — a mid-wizard gesture-lock and a dismiss veto that morphs a dismissal into a
// confirm page ("uscire senza inviare?"). The scene owns the holder and reads it for its
// PredictiveModalBottomSheet; the entry writes into it (via LocalSheetDismissControl). The back
// GESTURE is still handled by the entry's own BackHandler, which wins while it's enabled.
@Stable
class SheetDismissControl(val dismiss: () -> Unit) {
    var gesturesEnabled: Boolean by mutableStateOf(true)
    var confirmDismiss: () -> Boolean by mutableStateOf({ true })
}

val LocalSheetDismissControl = staticCompositionLocalOf<SheetDismissControl?> { null }

class BottomSheetSceneStrategy<T : Any>(
    // Pops `count` entries off the app back stack: 1 for an in-sheet back, the run size for a dismiss.
    private val pop: (count: Int) -> Unit,
) : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val top = entries.lastOrNull() ?: return null
        val group = top.sheetOptions()?.group ?: return null
        val run = entries.takeLastWhile { it.sheetOptions()?.group == group }
        val prefixSize = entries.size - run.size
        return BottomSheetScene(
            key = run.first().contentKey,
            run = run,
            overlaidEntries = entries.subList(0, prefixSize).toList(),
            previousEntries = entries.subList(0, entries.size - 1).toList(),
            pop = pop,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private class BottomSheetScene<T : Any>(
    override val key: Any,
    private val run: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    override val previousEntries: List<NavEntry<T>>,
    private val pop: (count: Int) -> Unit,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = run

    override val content: @Composable () -> Unit = {
        val top = run.last()
        val depth = run.lastIndex
        val options = top.sheetOptions()
        // Single-entry sheets (wizards) close themselves via control.dismiss() once their own
        // exit-confirm is accepted; pop(1) removes the one sheet entry.
        val control = remember { SheetDismissControl(dismiss = { pop(1) }) }

        PredictiveModalBottomSheet(
            onDismiss = { pop(run.size) },
            gesturesEnabled = control.gesturesEnabled,
            confirmDismiss = { control.confirmDismiss() },
        ) { _, _ ->
            // In-sheet page back pops one entry. Disabled at the root so the gesture falls
            // through to the sheet's own seekable swipe-close (PredictiveModalBottomSheet).
            BackHandler(enabled = depth > 0) { pop(1) }

            var prevDepth by remember { mutableIntStateOf(depth) }
            val forward = depth >= prevDepth
            SideEffect { prevDepth = depth }

            Column {
                val header = options?.header
                val spec = if (header != null) header(depth) else null
                if (spec != null) {
                    SheetPagerHeader(
                        depth = depth,
                        title = spec.title,
                        subtitle = spec.subtitle,
                        onBack = if (depth > 0) ({ pop(1) }) else null,
                        onSubtitleClick = spec.onSubtitleClick,
                    )
                }
                CompositionLocalProvider(LocalSheetDismissControl provides control) {
                    AnimatedContent(
                        targetState = top,
                        transitionSpec = { sheetPageTransform(forward = forward) },
                        contentKey = { it.contentKey },
                        modifier = Modifier.sheetBodyGestureBarrier(),
                        label = "sheet_pages",
                    ) { entry -> entry.Content() }
                }
            }
        }
    }
}
