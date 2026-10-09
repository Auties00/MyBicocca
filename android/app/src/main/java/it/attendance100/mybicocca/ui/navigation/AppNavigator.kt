package it.attendance100.mybicocca.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import it.attendance100.mybicocca.ui.navigation.route.AppRoute
import it.attendance100.mybicocca.ui.navigation.route.Route
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute

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
    internal val backStackKeys: List<NavKey>
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

    /**
     * Pushes [route]. Single-top: when the same route is already on top (a double tap, a deep link
     * re-delivered on resume) nothing happens and false is returned.
     */
    fun navigate(route: Route): Boolean {
        if (top.route == route) return false
        backStack.add(Destination(route))
        return true
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

    /** Pops back to the tab root. */
    fun popToRoot() {
        while (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
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

