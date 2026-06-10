package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state

import androidx.compose.runtime.Immutable
import it.attendance100.mybicocca.core.search.QueryHighlighter
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogCourse
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogNode
import it.attendance100.mybicocca.domain.model.elearning.catalog.ElearningCatalog

/**
 * The highlighter walks the whole scoped subtree; past this many rows the list stops growing —
 * a narrower query beats a longer scroll.
 */
private const val MAX_ROWS = 160

/**
 * One row of the scoped search results: search is a pruned tree of the catalog subtree under
 * the CURRENT level (or the whole catalog at the root), not a flat global list. Courses and
 * categories that match the query are kept, along with the waypoint categories leading to
 * them; rails bookkeeping mirrors the forum thread tree so rows render with their ancestry
 * drawn.
 */
@Immutable
sealed class SearchRow(
    /**
     * List key, unique even when cross-listings repeat a node or course under several parents
     * (a running row index is baked in).
     */
    val key: String,
    /**
     * One flag per ancestor depth: true = that ancestor has more siblings after it, so its
     * vertical rail continues through this row. The row's own elbow is drawn from
     * [isLastChild].
     */
    val parentRails: List<Boolean>,
    val isLastChild: Boolean,
    /**
     * Area-root node this row lives under, for accent resolution at the root scope. When the
     * search is scoped inside a level, this and [areaName] echo the level itself and the UI
     * uses its accent.
     */
    val areaTileId: String,
    val areaName: String,
) {
    class Category(
        key: String,
        parentRails: List<Boolean>,
        isLastChild: Boolean,
        areaTileId: String,
        areaName: String,
        val name: String,
        val nameHighlights: List<IntRange>,
        /** Direct query hit vs a muted waypoint that only leads to hits below it. */
        val isMatch: Boolean,
        /**
         * Chain from the scope root down to this node (inclusive); tapping pushes the WHOLE
         * chain so back pops level-by-level.
         */
        val path: List<CatalogNode>,
    ) : SearchRow(key, parentRails, isLastChild, areaTileId, areaName)

    class Course(
        key: String,
        parentRails: List<Boolean>,
        isLastChild: Boolean,
        areaTileId: String,
        areaName: String,
        val course: CatalogCourse,
        val nameHighlights: List<IntRange>,
        val codeHighlights: List<IntRange>,
    ) : SearchRow(key, parentRails, isLastChild, areaTileId, areaName)
}

/**
 * Pure walk over the in-memory catalog, like [buildCatalogLevels] — no repository involved.
 * [QueryHighlighter]'s non-null result doubles as the match predicate, so the ranges that
 * drive the marker highlights are also what decides pruning. At the root scope every area with
 * hits renders as its own top-level subtree; inside a level only that level's descendants are
 * walked.
 */
fun buildSearchRows(
    catalog: ElearningCatalog,
    stack: List<CatalogStackEntry>,
    query: String,
): List<SearchRow> {
    val highlighter = QueryHighlighter(query)
    if (highlighter.isEmpty) return emptyList()

    val rows = ArrayList<SearchRow>(64)
    if (stack.isEmpty()) {
        val areas = ArrayList<PrunedCategory>()
        for (section in catalog.sections) {
            for (area in section.nodes) {
                pruneCategory(area, highlighter)?.let { areas += it }
            }
        }
        areas.forEachIndexed { index, area ->
            emitCategory(
                rows = rows,
                category = area,
                rails = emptyList(),
                isLast = index == areas.lastIndex,
                path = emptyList(),
                areaTileId = area.node.id,
                areaName = area.node.name,
            )
        }
    } else {
        val current = stack.last()
        val children = current.node.children.mapNotNull { pruneCategory(it, highlighter) }
        val courses = current.node.courses.mapNotNull { pruneCourse(it, highlighter) }
        emitChildren(
            rows = rows,
            children = children,
            courses = courses,
            rails = emptyList(),
            path = emptyList(),
            areaTileId = current.areaTileId,
            areaName = current.node.name,
        )
    }
    return rows
}

private class PrunedCategory(
    val node: CatalogNode,
    val nameRanges: List<IntRange>?,
    val children: List<PrunedCategory>,
    val courses: List<PrunedCourse>,
)

private class PrunedCourse(
    val course: CatalogCourse,
    val nameRanges: List<IntRange>?,
    val codeRanges: List<IntRange>?,
)

/**
 * A category survives when it matches directly or anything below it does. A directly-matching
 * category does NOT drag its whole subtree in — it renders as a tappable hit instead.
 */
private fun pruneCategory(node: CatalogNode, highlighter: QueryHighlighter): PrunedCategory? {
    val nameRanges = highlighter.rangesIn(node.name)
    val children = node.children.mapNotNull { pruneCategory(it, highlighter) }
    val courses = node.courses.mapNotNull { pruneCourse(it, highlighter) }
    if (nameRanges == null && children.isEmpty() && courses.isEmpty()) return null
    return PrunedCategory(node, nameRanges, children, courses)
}

private fun pruneCourse(course: CatalogCourse, highlighter: QueryHighlighter): PrunedCourse? {
    val nameRanges = highlighter.rangesIn(course.name)
    val codeRanges = highlighter.rangesIn(course.code)
    if (nameRanges == null && codeRanges == null) return null
    return PrunedCourse(course, nameRanges, codeRanges)
}

private fun emitCategory(
    rows: MutableList<SearchRow>,
    category: PrunedCategory,
    rails: List<Boolean>,
    isLast: Boolean,
    path: List<CatalogNode>,
    areaTileId: String,
    areaName: String,
) {
    if (rows.size >= MAX_ROWS) return
    val nodePath = path + category.node
    rows += SearchRow.Category(
        key = "c${rows.size}-${category.node.id}",
        parentRails = rails,
        isLastChild = isLast,
        areaTileId = areaTileId,
        areaName = areaName,
        name = category.node.name,
        nameHighlights = category.nameRanges.orEmpty(),
        isMatch = category.nameRanges != null,
        path = nodePath,
    )
    emitChildren(rows, category.children, category.courses, rails + !isLast, nodePath, areaTileId, areaName)
}

private fun emitChildren(
    rows: MutableList<SearchRow>,
    children: List<PrunedCategory>,
    courses: List<PrunedCourse>,
    rails: List<Boolean>,
    path: List<CatalogNode>,
    areaTileId: String,
    areaName: String,
) {
    val total = children.size + courses.size
    children.forEachIndexed { index, child ->
        emitCategory(rows, child, rails, index == total - 1, path, areaTileId, areaName)
    }
    courses.forEachIndexed { courseIndex, pruned ->
        if (rows.size >= MAX_ROWS) return
        val index = children.size + courseIndex
        rows += SearchRow.Course(
            key = "x${rows.size}-${pruned.course.id.value}",
            parentRails = rails,
            isLastChild = index == total - 1,
            areaTileId = areaTileId,
            areaName = areaName,
            course = pruned.course,
            nameHighlights = pruned.nameRanges.orEmpty(),
            codeHighlights = pruned.codeRanges.orEmpty(),
        )
    }
}
