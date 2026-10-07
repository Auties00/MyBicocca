package it.attendance100.mybicocca.ui.navigation

import it.attendance100.mybicocca.ui.navigation.route.Route
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute

/**
 * How the back stack splits into sheets. Pure functions over routes, shared by the modal scene
 * strategy, [AppNavigator] and tests.
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
