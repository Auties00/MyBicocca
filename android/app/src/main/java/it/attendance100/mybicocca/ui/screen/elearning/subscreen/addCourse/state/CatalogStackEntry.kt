package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state

import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogNode

data class CatalogStackEntry(
    val node: CatalogNode,
    val areaTileId: String,
)
