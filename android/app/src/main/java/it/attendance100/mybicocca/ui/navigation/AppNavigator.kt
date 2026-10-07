package it.attendance100.mybicocca.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import it.attendance100.mybicocca.ui.navigation.route.AppRoute
import it.attendance100.mybicocca.ui.navigation.route.Route
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.navigation.scene.ModalLayout
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * One back-stack slot: a [route] plus the [id] of this particular push. Routes are value types —
 * the ISEE sheet is always `SheetRoute.Isee`, a course is always `CourseDetail(42)` — so two pushes
 * of the same route are indistinguishable by the route alone. Navigation3 keys every per-entry
 * state holder (saveable state, ViewModel store, scene movable content) by the entry's contentKey,
 * and two live entries sharing one ends in `IllegalArgumentException: Key … was used multiple
 * times` (issue #44). The id makes every push unique, exactly like Navigation2's
 * NavBackStackEntry.id: it is the contentKey of the entry, the identity of the sheet a modal page
 * opens, and the handle [AppNavigator] pops by, so a stale pop can never remove the wrong entry.
 *
 * Serializable (the id included) so the whole stack is restored after process death with the same
 * identities the saved per-entry state was filed under.
 */
@Serializable
data class Destination(
    val route: Route,
    val id: String = UUID.randomUUID().toString(),
) : NavKey

/**
 * The single entry point for every navigation action of the signed-in shell: full-screen pages,
 * modal sheets and the pages inside a sheet all ride one Navigation3 back stack of [Destination]s,
 * mutated only through here.
 *
 * Every operation is idempotent and bounded by construction, which is what removes the race class
 * behind issue #22 (two pops landing in one frame emptying the stack and crashing NavDisplay):
 * - the root entry ([AppRoute.TabRoot], index 0) can never be removed;
 * - pops are addressed by entry [Destination.id] rather than by count or by "whatever is on top",
 *   so a duplicate dismiss callback, a double-tapped back button or a stale lambda captured by an
 *   exiting sheet becomes a no-op instead of removing an unrelated entry;
 * - pushes are single-top: pushing the route already on top (a double tap) is ignored.
 */
@Stable
class AppNavigator(private val backStack: NavBackStack<NavKey>) {

    /** The raw Navigation3 back stack (of [Destination]s), for building the entries to display. */
    val keys: List<NavKey>
        get() = backStack

    /** The live stack, bottom to top. Reading it in composition subscribes to changes. */
    val entries: List<Destination>
        get() = backStack.map { it as Destination }

    /** The top-most destination (a sheet page when a sheet is open). */
    val top: Destination
        get() = backStack.last() as Destination

    /**
     * The top-most full-screen page — the page a sheet floats over when one is open. This is what
     * drives the shell's chrome (title, back arrow), which stays put behind an open sheet.
     */
    val currentPage: Destination
        get() = entries.last { it.route is AppRoute }

    /** True when nothing but the tab root is on the stack. */
    val isAtRoot: Boolean
        get() = backStack.size <= 1

    /** Whether the entry with [id] is still on the stack. */
    operator fun contains(id: String): Boolean = backStack.any { (it as Destination).id == id }

    /**
     * Pushes [route]. Single-top: when the same route is already on top (a double tap, a deep link
     * re-delivered on resume) nothing happens and false is returned.
     */
    fun navigate(route: Route): Boolean {
        if (top.route == route) return false
        backStack.add(Destination(route))
        return true
    }

    /** Replaces the top entry with [route] (e.g. a chooser page handing over to its result). */
    fun replaceTop(route: Route) {
        if (isAtRoot) {
            navigate(route)
            return
        }
        backStack[backStack.lastIndex] = Destination(route)
    }

    /** Pops the top entry. Never pops the root; returns whether anything was popped. */
    fun back(): Boolean {
        if (isAtRoot) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }

    /**
     * Pops the entry [id] together with everything stacked above it — closing a sheet by its root
     * page's id, or leaving an in-sheet page by its own. A no-op when the entry is already gone (or
     * is the root), so it is safe to call from every dismissal path of the same sheet.
     */
    fun pop(id: String): Boolean {
        val index = backStack.indexOfFirst { (it as Destination).id == id }
        if (index <= 0) return false
        while (backStack.size > index) backStack.removeAt(backStack.lastIndex)
        return true
    }

    /**
     * Closes the whole sheet the entry [id] belongs to (its first page and every page above), e.g.
     * a picker page inside the event sheet that leaves to a full-screen page. When [id] is not a
     * sheet page it pops just that entry. No-op when it is gone.
     */
    fun dismissSheetOf(id: String): Boolean {
        val stack = entries
        val index = stack.indexOfFirst { it.id == id }
        if (index <= 0) return false
        if (stack[index].route !is SheetRoute) return pop(id)
        val start = ModalLayout.sheetStart(stack.map { it.route }, index)
        return pop(stack[start].id)
    }

    /** Pops everything above the entry [id], leaving it on top. No-op when it is gone. */
    fun popAbove(id: String): Boolean {
        val index = backStack.indexOfFirst { (it as Destination).id == id }
        if (index < 0 || index == backStack.lastIndex) return false
        while (backStack.size > index + 1) backStack.removeAt(backStack.lastIndex)
        return true
    }

    /** Pops back to the tab root. */
    fun popToRoot() {
        while (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    /** Removes every entry above the root whose route matches [predicate]. */
    fun removeAll(predicate: (Route) -> Boolean) {
        for (index in backStack.lastIndex downTo 1) {
            if (predicate((backStack[index] as Destination).route)) backStack.removeAt(index)
        }
    }

    companion object {
        /** Stable id of the root entry, so the restored root keeps its saved state. */
        const val ROOT_ID: String = "root"
    }
}

/** Remembers the shell's back stack (saveable across process death) and its navigator. */
@Composable
fun rememberAppNavigator(): AppNavigator {
    val backStack = rememberNavBackStack(Destination(AppRoute.TabRoot, AppNavigator.ROOT_ID))
    return remember(backStack) { AppNavigator(backStack) }
}

/**
 * The shell's navigator, for screens that open pages or sheets without callback plumbing. Null
 * outside MainShell (e.g. a page composed on its own in a UI test); see [requireAppNavigator].
 */
val LocalAppNavigator = staticCompositionLocalOf<AppNavigator?> { null }

/** The shell's navigator; fails fast when composed outside MainShell. */
@Composable
fun requireAppNavigator(): AppNavigator =
    LocalAppNavigator.current ?: error("No AppNavigator: only available inside MainShell")

/**
 * Per-entry "left the back stack" actions registered by [DisposableEffectOnPop]. Navigation3 calls
 * a decorator's onPop for every popped entry — when its content leaves composition, or right away
 * if it was not composed at the time — so the actions run exactly once per pop, including for a
 * sheet that was hidden under a full-screen page when the stack was cleared.
 */
class EntryPopActions internal constructor() {
    private val actions = HashMap<Any, MutableMap<Long, () -> Unit>>()

    internal fun register(contentKey: Any, slot: Long, action: () -> Unit) {
        actions.getOrPut(contentKey) { HashMap() }[slot] = action
    }

    internal fun onPop(contentKey: Any) {
        actions.remove(contentKey)?.values?.forEach { it() }
    }
}

private val LocalEntryPopActions = staticCompositionLocalOf<EntryPopActions?> { null }

/** The shell's entry decorator backing [DisposableEffectOnPop]. */
@Composable
fun rememberEntryPopActionsDecorator(): NavEntryDecorator<NavKey> {
    val actions = remember { EntryPopActions() }
    return remember(actions) {
        NavEntryDecorator(onPop = actions::onPop) { entry ->
            CompositionLocalProvider(LocalEntryPopActions provides actions) { entry.Content() }
        }
    }
}

/**
 * Runs [onPopped] once the calling entry is removed from the back stack — and NOT when its content
 * merely stops being shown while the entry stays on the stack (a sheet sliding away under a
 * full-screen page pushed from it, e.g. a PDF, comes back with its flow intact). Outside a shell
 * entry (a standalone UI test) it runs when the content is disposed.
 */
@Composable
fun DisposableEffectOnPop(onPopped: () -> Unit) {
    val actions = LocalEntryPopActions.current
    val self = LocalDestination.current
    val latest = rememberUpdatedState(onPopped)
    val slot = currentCompositeKeyHashCode
    DisposableEffect(actions, self, slot) {
        // Stays registered after disposal on purpose: the pop may come later.
        if (actions != null && self != null) actions.register(self.id, slot) { latest.value() }
        onDispose {
            if (actions == null || self == null) latest.value()
        }
    }
}

/**
 * Adapts a route-keyed entry provider (the `entryProvider { entry<Route> { … } }` DSL) to the
 * [Destination] back stack: the route's entry keeps its metadata and content, while the
 * destination's unique id becomes the contentKey every per-entry state holder is filed under. The
 * destination itself rides along in the metadata ([NavEntry.destination]) because NavEntry keeps
 * its key private and scenes need the route (e.g. to resolve a sheet page's header).
 */
fun destinationEntryProvider(
    routeProvider: (NavKey) -> NavEntry<NavKey>,
): (NavKey) -> NavEntry<NavKey> = { key ->
    val destination = key as Destination
    val routeEntry = routeProvider(destination.route)
    NavEntry(
        key = key,
        contentKey = destination.id,
        metadata = routeEntry.metadata + (DestinationMetadataKey to destination),
    ) {
        CompositionLocalProvider(LocalDestination provides destination) { routeEntry.Content() }
    }
}

/**
 * The [Destination] whose entry is being composed — lets an entry address itself (e.g. a detail
 * page popping only itself when its item disappears) without depending on what is on top.
 */
val LocalDestination = staticCompositionLocalOf<Destination?> { null }

private const val DestinationMetadataKey = "it.attendance100.mybicocca.destination"

/** The [Destination] behind an entry built by [destinationEntryProvider]. */
val NavEntry<*>.destination: Destination
    get() = metadata[DestinationMetadataKey] as? Destination
        ?: error("Entry $contentKey was not built by destinationEntryProvider")
