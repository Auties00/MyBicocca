package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state

import androidx.compose.ui.graphics.Color
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogNode

/**
 * One pushed step of the catalog browse stack: the opened node plus the area-root identity and
 * accent it inherited, so deeper levels keep their colouring without re-resolving the area they
 * were entered from.
 */
data class CatalogStackEntry(
    val node: CatalogNode,
    val areaTileId: String,
    val accent: Color,
)
