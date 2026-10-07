package it.attendance100.mybicocca.ui.navigation.scene

import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import it.attendance100.mybicocca.ui.component.modal.LocalSheetDismissControl
import it.attendance100.mybicocca.ui.component.modal.ModalSheetController
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.component.modal.SheetDismissControl
import it.attendance100.mybicocca.ui.component.modal.SheetDragHandle
import it.attendance100.mybicocca.ui.component.modal.SheetPageSlide
import it.attendance100.mybicocca.ui.component.modal.SheetPager
import it.attendance100.mybicocca.ui.component.modal.SheetPagerImpl
import it.attendance100.mybicocca.ui.component.modal.rememberLastNonNull
import it.attendance100.mybicocca.ui.navigation.AppNavigator
import it.attendance100.mybicocca.ui.navigation.Destination
import it.attendance100.mybicocca.ui.navigation.ModalLayout
import it.attendance100.mybicocca.ui.navigation.destination
import it.attendance100.mybicocca.ui.navigation.sheetContainerStyle
import it.attendance100.mybicocca.ui.navigation.sheetPageOptions
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import kotlinx.coroutines.flow.first

/**
 * The shell's decorated back-stack entries (saveable state + ViewModel store), provided around the
 * NavDisplay so a sheet can render its pages from the live stack. See [ModalSceneStrategy].
 */
val LocalModalEntries = compositionLocalOf<List<NavEntry<NavKey>>> { emptyList() }

/**
 * Renders [SheetRoute] entries as modal bottom sheets over the page beneath — every modal of the
 * shell, one implementation.
 *
 * The top run of sheet entries is split by [ModalLayout]: each sheet becomes one [OverlayScene]
 * (sheets stacked above sheets become stacked overlays), its first page is the sheet's root and
 * the in-sheet pages above it are navigated with [SheetPager] inside the same, persistent sheet.
 *
 * Scene identity is the sheet itself — the unique [Destination.id] of its first page — and NOT the
 * pages it currently shows. NavDisplay keeps an overlay scene composed for as long as an EQUAL
 * scene is calculated, and composes a non-equal one next to it until the old one's [onRemove]
 * finishes; a scene per page would briefly compose a second sheet rendering the same entries
 * ("Key … was used multiple times", issue #44) and re-open the sheet instead of morphing between
 * pages (issue #14). Because NavDisplay holds on to the FIRST scene object of a sheet, the sheet
 * reads its pages from the live stack ([LocalModalEntries]) rather than from the scene object.
 *
 * Closing is animated: when the sheet leaves the stack by navigation (a "Fatto" button, a deep
 * link, the tab bar) [onRemove] slides it out before NavDisplay drops it. If the same sheet comes
 * back while it is hiding (its root is still the same entry, e.g. returning from a full-screen
 * file viewer opened from it), it slides back in instead.
 */
class ModalSceneStrategy(private val navigator: AppNavigator) : SceneStrategy<NavKey> {

    override fun SceneStrategyScope<NavKey>.calculateScene(entries: List<NavEntry<NavKey>>): Scene<NavKey>? {
        val top = entries.lastOrNull() ?: return null
        if (top.destination.route !is SheetRoute) return null
        val routes = entries.map { it.destination.route }
        val start = ModalLayout.sheetStart(routes, entries.lastIndex)
        // A sheet always floats over a page: the root of the stack is never a sheet.
        if (start == 0) return null
        return ModalSheetScene(
            rootId = entries[start].destination.id,
            navigator = navigator,
            entries = entries.subList(start, entries.size).toList(),
            overlaidEntries = entries.subList(0, start).toList(),
            previousEntries = entries.subList(0, entries.lastIndex).toList(),
        )
    }
}

private class ModalSheetScene(
    private val rootId: String,
    private val navigator: AppNavigator,
    override val entries: List<NavEntry<NavKey>>,
    override val overlaidEntries: List<NavEntry<NavKey>>,
    override val previousEntries: List<NavEntry<NavKey>>,
) : OverlayScene<NavKey> {

    override val key: Any = rootId

    private val controller = ModalSheetController()

    override val content: @Composable () -> Unit = {
        ModalSheetHost(rootId = rootId, navigator = navigator, controller = controller)
    }

    override suspend fun onRemove() {
        while (true) {
            snapshotFlow { ModalLayout.isSheetShown(navigator.entries, rootId) }.first { !it }
            controller.hide()
            if (!ModalLayout.isSheetShown(navigator.entries, rootId)) return
        }
    }

    override fun equals(other: Any?): Boolean =
        this === other || (other is ModalSheetScene && other.rootId == rootId)

    override fun hashCode(): Int = rootId.hashCode()

    override fun toString(): String = "ModalSheetScene(root=$rootId)"
}

/** One page of a sheet as seen by its pager: the entry and its position in the sheet. */
private data class HostedPage(val entry: NavEntry<NavKey>, val depth: Int) {
    val id: String get() = entry.destination.id
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModalSheetHost(
    rootId: String,
    navigator: AppNavigator,
    controller: ModalSheetController,
) {
    val all = LocalModalEntries.current
    val live = remember(all, rootId) {
        val start = all.indexOfFirst { it.destination.id == rootId }
        if (start < 0) {
            emptyList()
        } else {
            val range = ModalLayout.sheetRange(all.map { it.destination.route }, start)
            all.subList(range.first, range.last + 1).mapIndexed { depth, entry -> HostedPage(entry, depth) }
        }
    }
    // While the sheet slides out after being popped its entries are gone from the stack; keep
    // rendering the pages it last showed (their state holders outlive the pop until disposal).
    val pages = rememberLastNonNull(live.ifEmpty { null }) ?: return

    LaunchedEffect(navigator, rootId) {
        snapshotFlow { ModalLayout.isSheetShown(navigator.entries, rootId) }
            .collect { shown -> if (shown) controller.show() }
    }

    // One dismiss control per page, kept while the page is in the sheet.
    val controls = remember { mutableMapOf<String, SheetDismissControl>() }
    fun controlFor(id: String): SheetDismissControl =
        controls.getOrPut(id) { SheetDismissControl(dismiss = { navigator.pop(rootId) }) }
    SideEffect { controls.keys.retainAll(pages.mapTo(HashSet()) { it.id }) }

    val top = pages.last()
    val topControl = controlFor(top.id)
    val latestTopControl by rememberUpdatedState(topControl)

    val style = pages.first().entry.sheetContainerStyle
    val gesturesEnabled = topControl.gesturesEnabled
    PredictiveModalBottomSheet(
        onDismiss = { navigator.pop(rootId) },
        gesturesEnabled = gesturesEnabled,
        confirmDismiss = { latestTopControl.confirmDismiss() },
        controller = controller,
        shape = style?.shape ?: BottomSheetDefaults.ExpandedShape,
        scrimColor = style?.scrimColor ?: BottomSheetDefaults.ScrimColor,
        contentWindowInsets = style?.contentWindowInsets ?: { BottomSheetDefaults.modalWindowInsets },
        dragHandle = when {
            style?.hideDragHandle == true -> null
            style?.dragHandle != null -> style.dragHandle
            else -> { { SheetDragHandle(gesturesEnabled) } }
        },
    ) {
        SheetPagerImpl(
            page = top,
            depth = { it.depth },
            key = { it.id },
            slide = SheetPageSlide.Horizontal,
            backTo = pages.getOrNull(pages.lastIndex - 1),
            onBack = { navigator.pop(top.id) },
            modifier = Modifier,
            header = { page ->
                val route = page.entry.destination.route
                val options = checkNotNull(page.entry.sheetPageOptions) {
                    "Sheet route $route declares neither sheetHeader nor sheetHeaderInPage: " +
                        "every modal page needs a title and subtitle"
                }
                options.header?.invoke(route)
            },
        ) { page ->
            CompositionLocalProvider(LocalSheetDismissControl provides controlFor(page.id)) {
                page.entry.Content()
            }
        }
    }
}
