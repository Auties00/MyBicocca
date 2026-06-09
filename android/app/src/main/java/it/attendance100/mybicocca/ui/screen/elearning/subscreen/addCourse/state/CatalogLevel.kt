package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogCourse
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogNode
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogSection
import it.attendance100.mybicocca.domain.model.elearning.catalog.ElearningCatalog

// One browsable "page" of the catalog. Each level is fully self-contained (it carries the
// data needed to render it) so a SeekableTransitionState can keep an outgoing page composed
// while the stack underneath it has already been popped — equality/identity is by [key]
// (depth + node id) so live enrolment-status updates never look like a navigation event.
@Immutable
sealed class CatalogLevel(
    val key: String,
    val depth: Int,
    val title: String,
) {
    class Root(val sections: List<CatalogSection>) :
        CatalogLevel(key = "root", depth = 0, title = "Aggiungi corso")

    class Inside(
        depth: Int,
        val node: CatalogNode,
        val areaTileId: String,
        val accent: Color,
        // Names of the ancestors above this level, for the header breadcrumb.
        val ancestors: List<String>,
    ) : CatalogLevel(key = "n:${node.id}@$depth", depth = depth, title = node.name) {
        val children: List<CatalogNode> get() = node.children
        val courses: List<CatalogCourse> get() = node.courses
    }

    override fun equals(other: Any?): Boolean = other is CatalogLevel && other.key == key
    override fun hashCode(): Int = key.hashCode()
}

// Flattens the navigation stack into the full chain of pages (root first, current last).
// The chain is what lets the back gesture scrub from the current page to its parent.
fun buildCatalogLevels(
    catalog: ElearningCatalog,
    stack: List<CatalogStackEntry>,
): List<CatalogLevel> {
    val levels = ArrayList<CatalogLevel>(stack.size + 1)
    levels += CatalogLevel.Root(catalog.sections)
    stack.forEachIndexed { index, entry ->
        levels += CatalogLevel.Inside(
            depth = index + 1,
            node = entry.node,
            areaTileId = entry.areaTileId,
            accent = entry.accent,
            ancestors = stack.take(index).map { it.node.name },
        )
    }
    return levels
}
