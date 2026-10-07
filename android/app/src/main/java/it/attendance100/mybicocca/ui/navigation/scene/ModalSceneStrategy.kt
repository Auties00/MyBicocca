package it.attendance100.mybicocca.ui.navigation.scene

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import it.attendance100.mybicocca.ui.component.modal.ModalSheetController
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.component.modal.SheetHeaderSpec
import it.attendance100.mybicocca.ui.component.modal.SheetPager
import it.attendance100.mybicocca.ui.component.modal.SheetPagerImpl
import androidx.compose.ui.Modifier
import it.attendance100.mybicocca.ui.navigation.AppNavigator
import it.attendance100.mybicocca.ui.navigation.Destination
import it.attendance100.mybicocca.ui.navigation.destination
import it.attendance100.mybicocca.ui.navigation.route.Route
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import kotlinx.coroutines.flow.first

/**
 * Lets a sheet page drive its sheet's swipe/scrim/back dismissal from its OWN state — a
 * mid-wizard gesture lock and a dismiss veto that morphs a dismissal into a confirm page ("uscire
 * senza inviare?"). Each page of a sheet gets its own control (via [LocalSheetDismissControl]) and
 * the sheet obeys the control of the page currently on top, so a page pushed above a wizard (e.g.
 * the file chooser) never inherits the wizard's stale lock.
 *
 * [dismiss] closes the whole sheet; it is idempotent.
 */
@Stable
class SheetDismissControl(val dismiss: () -> Unit) {
    var gesturesEnabled: Boolean by mutableStateOf(true)
    var confirmDismiss: () -> Boolean by mutableStateOf({ true })
}

/** Provided around every sheet page; null when not hosted inside a sheet. */
val LocalSheetDismissControl = staticCompositionLocalOf<SheetDismissControl?> { null }

/**
 * The shell's decorated back-stack entries (saveable state + ViewModel store), provided around the
 * NavDisplay so a sheet can render its pages from the live stack. See [ModalSceneStrategy].
 */
val LocalModalEntries = compositionLocalOf<List<NavEntry<NavKey>>> { emptyList() }

/**
 * Sheet-page metadata payload: the page's pinned header resolver, or null when the page draws
 * its header itself through a nested [SheetPager] ([sheetHeaderInPage]).
 */
class SheetPageOptions(val header: (@Composable (Route) -> SheetHeaderSpec)?)

private const val SheetPageMetadataKey: String = "it.attendance100.mybicocca.sheetPage"

/**
 * Metadata giving a sheet entry its pinned [SheetHeaderSpec] header (title and subtitle), e.g.
 * `entry<SheetRoute.IseeDetail>(metadata = sheetHeader<SheetRoute.IseeDetail> { key -> … })`.
 * Required on every sheet route: a page without it fails fast when shown. The resolver gets the
 * entry's own route, so a page can render its header even while it is the outgoing side of a
 * transition. Pages with their own internal pager (wizards) resolve their root page's header here
 * and nest a [SheetPager] for deeper pages.
 */
inline fun <reified R : Route> sheetHeader(
    noinline header: @Composable (R) -> SheetHeaderSpec,
): Map<String, Any> = sheetHeaderMetadata { route -> header(route as R) }

@PublishedApi
internal fun sheetHeaderMetadata(header: (@Composable (Route) -> SheetHeaderSpec)?): Map<String, Any> =
    mapOf(SheetPageMetadataKey to SheetPageOptions(header))

/**
 * Metadata for a sheet page that has its own page machine on a nested [SheetPager] (a wizard, a
 * list -> detail -> result flow): that pager draws the title and subtitle of each of its pages,
 * so the sheet adds no header of its own above it.
 */
fun sheetHeaderInPage(): Map<String, Any> = sheetHeaderMetadata(null)

private fun NavEntry<*>.sheetPageOptions(): SheetPageOptions? =
    metadata[SheetPageMetadataKey] as? SheetPageOptions

/**
 * Container look of a sheet, declared on its first page: for the few sheets with their own visual
 * identity (e.g. the account switcher's large corners and custom handle). Unset values keep the
 * app's standard sheet.
 */
class SheetContainerStyle(
    val shape: Shape? = null,
    val dragHandle: (@Composable () -> Unit)? = null,
    val hideDragHandle: Boolean = false,
    val contentWindowInsets: (@Composable () -> WindowInsets)? = null,
    val scrimColor: Color? = null,
)

private const val SheetStyleMetadataKey: String = "it.attendance100.mybicocca.sheetStyle"

/** Metadata giving a sheet (declared on its first page) a [SheetContainerStyle]. */
fun sheetStyle(style: SheetContainerStyle): Map<String, Any> = mapOf(SheetStyleMetadataKey to style)

private fun NavEntry<*>.sheetStyle(): SheetContainerStyle? =
    metadata[SheetStyleMetadataKey] as? SheetContainerStyle

/**
 * How the back stack splits into sheets. Pure functions over routes, shared by the scene strategy,
 * the navigator and tests.
 */
object ModalLayout {

    /**
     * Index of the first page of the sheet containing the sheet route at [index]: in-sheet pages
     * ([SheetRoute.joinsParentSheet]) extend the sheet beneath them, anything else starts one.
     */
    fun sheetStart(routes: List<Route>, index: Int): Int {
        var start = index
        while (start > 0 && (routes[start] as SheetRoute).joinsParentSheet && routes[start - 1] is SheetRoute) {
            start--
        }
        return start
    }

    /** The pages of the sheet whose first page is at [start] (it and the in-sheet pages above). */
    fun sheetRange(routes: List<Route>, start: Int): IntRange {
        var end = start
        while (end + 1 < routes.size && (routes[end + 1] as? SheetRoute)?.joinsParentSheet == true) end++
        return start..end
    }

    /**
     * Whether the sheet whose first page is [rootId] is on screen: on the stack with nothing but
     * modals above it (another sheet may be stacked on top; a full-screen page hides it).
     */
    fun isSheetShown(stack: List<Destination>, rootId: String): Boolean {
        val index = stack.indexOfFirst { it.id == rootId }
        if (index <= 0) return false
        for (i in index until stack.size) if (stack[i].route !is SheetRoute) return false
        return true
    }
}

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
 * finishes; the previous strategy produced a new, non-equal scene object on every recalculation
 * (every push inside the sheet, and every recomposition of the shell, since the strategies list
 * was rebuilt each time), so each one briefly composed a SECOND sheet rendering the same entries.
 * That is what crashed with "Key Isee was used multiple times" when the second sheet's window
 * attached first (issue #44, typically right after resuming, when the shell recomposes), and what
 * re-opened the sheet instead of morphing its height between pages (issue #14). Because NavDisplay
 * therefore holds on to the FIRST scene object of a sheet, the sheet reads its pages from the live
 * stack ([LocalModalEntries]) rather than from the scene object.
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

/** One page of a sheet as seen by its [SheetPager]: the entry and its position in the sheet. */
private class SheetPage(val entry: NavEntry<NavKey>, val depth: Int) {
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
            all.subList(range.first, range.last + 1).toList()
        }
    }
    // While the sheet slides out after being popped its entries are gone from the stack; keep
    // rendering the pages it last showed (their state holders outlive the pop until disposal).
    val lastPages = remember { arrayOf(live) }
    val pages = live.ifEmpty { lastPages[0] }
    SideEffect { if (live.isNotEmpty()) lastPages[0] = live }
    if (pages.isEmpty()) return

    val shown = ModalLayout.isSheetShown(navigator.entries, rootId)
    LaunchedEffect(shown) { if (shown) controller.show() }

    val controls = remember { HashMap<String, SheetDismissControl>() }
    fun controlFor(id: String): SheetDismissControl =
        controls.getOrPut(id) { SheetDismissControl(dismiss = { navigator.pop(rootId) }) }

    val refs = pages.mapIndexed { depth, entry -> SheetPage(entry, depth) }
    val top = refs.last()
    val topControl = controlFor(top.id)
    val latestTopControl by rememberUpdatedState(topControl)
    SideEffect { controls.keys.retainAll(refs.map { it.id }.toSet()) }

    val style = pages.first().sheetStyle()
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
            backTo = refs.getOrNull(refs.lastIndex - 1),
            onBack = { navigator.pop(top.id) },
            modifier = Modifier,
            header = { page ->
                val route = page.entry.destination.route
                val options = checkNotNull(page.entry.sheetPageOptions()) {
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

/** The standard drag handle, muted (but kept) while the sheet's gestures are locked. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetDragHandle(gesturesEnabled: Boolean) {
    if (gesturesEnabled) {
        BottomSheetDefaults.DragHandle()
    } else {
        BottomSheetDefaults.DragHandle(
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
        )
    }
}
