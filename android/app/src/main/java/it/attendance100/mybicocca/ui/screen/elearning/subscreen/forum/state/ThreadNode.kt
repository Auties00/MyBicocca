package it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.state

import it.attendance100.mybicocca.domain.model.elearning.forum.Post

/**
 * One row of the flattened discussion tree, carrying everything the renderer needs to draw the
 * Reddit/Apollo-style connector rails and the collapse affordance without re-walking the tree.
 *
 * [parentRails] has one entry per ANCESTOR column (depth-1 of them): true means that ancestor
 * still has a sibling below it, so a vertical pass-through rail is drawn in that column.
 * [isLastChild] marks whether THIS node is the last of its siblings — the connector elbow stops
 * at the row's middle instead of continuing down. [descendantCount] drives the "N risposte" pill
 * when collapsed.
 */
data class ThreadNode(
    val post: Post,
    val depth: Int,
    val hasChildren: Boolean,
    val descendantCount: Int,
    val parentRails: List<Boolean>,
    val isLastChild: Boolean,
)

/**
 * Builds the depth-annotated, pre-ordered node list from a flat post list. Roots are posts with
 * no (resolvable) parent; children follow each parent in chronological order. The lineage
 * carried down the DFS records, for each level, whether that node was its parent's last child,
 * which is exactly what the rail renderer needs: an ancestor column keeps its pass-through rail
 * precisely where that ancestor was NOT its parent's last child.
 */
fun buildThread(posts: List<Post>): List<ThreadNode> {
    if (posts.isEmpty()) return emptyList()
    val ids = posts.mapTo(HashSet()) { it.id }
    val childrenByParent = LinkedHashMap<Int, MutableList<Post>>()
    val roots = ArrayList<Post>()
    for (post in posts) {
        val parent = post.parentId
        if (parent == null || parent !in ids) roots += post
        else childrenByParent.getOrPut(parent.value) { ArrayList() } += post
    }

    val descendantCache = HashMap<Int, Int>()
    fun descendants(post: Post): Int = descendantCache.getOrPut(post.id.value) {
        childrenByParent[post.id.value]?.sumOf { 1 + descendants(it) } ?: 0
    }

    val out = ArrayList<ThreadNode>(posts.size)
    fun walk(post: Post, lineage: List<Boolean>) {
        val depth = lineage.size
        val kids = childrenByParent[post.id.value].orEmpty()
        out += ThreadNode(
            post = post,
            depth = depth,
            hasChildren = kids.isNotEmpty(),
            descendantCount = descendants(post),
            parentRails = if (depth <= 1) emptyList() else lineage.dropLast(1).map { !it },
            isLastChild = lineage.lastOrNull() ?: true,
        )
        kids.forEachIndexed { index, child -> walk(child, lineage + (index == kids.lastIndex)) }
    }
    roots.forEach { walk(it, emptyList()) }
    return out
}

/**
 * Filters a pre-ordered thread to the rows currently visible given the set of collapsed post
 * ids: a collapsed post stays visible but all of its descendants are hidden.
 */
fun visibleThread(thread: List<ThreadNode>, collapsed: Set<Int>): List<ThreadNode> {
    if (collapsed.isEmpty()) return thread
    val out = ArrayList<ThreadNode>(thread.size)
    var hiddenBelowDepth = Int.MAX_VALUE
    for (node in thread) {
        if (node.depth > hiddenBelowDepth) continue
        out += node
        hiddenBelowDepth = if (node.post.id.value in collapsed) node.depth else Int.MAX_VALUE
    }
    return out
}
