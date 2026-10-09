package it.attendance100.mybicocca.ui.screen.map

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.navigation.sheetHeader
import it.attendance100.mybicocca.ui.screen.map.subscreen.mapFilter.MapFilterPage
import it.attendance100.mybicocca.ui.screen.map.subscreen.mapFilter.mapFilterHeader

/** The map's category filter, editing the shell-scoped [mapViewModel]'s selection in place. */
fun EntryProviderScope<NavKey>.mapSheetEntries(mapViewModel: MapViewModel) {
    entry<SheetRoute.MapFilter>(
        metadata = sheetHeader<SheetRoute.MapFilter> {
            val categoryFilter by mapViewModel.categoryFilter.collectAsStateWithLifecycle()
            mapFilterHeader(selectedCount = categoryFilter.size)
        },
    ) {
        val categoryFilter by mapViewModel.categoryFilter.collectAsStateWithLifecycle()
        MapFilterPage(
            selected = categoryFilter,
            onToggle = mapViewModel::toggleCategory,
            onClear = mapViewModel::clearCategories,
        )
    }
}
