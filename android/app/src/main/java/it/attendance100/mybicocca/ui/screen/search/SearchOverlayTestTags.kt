package it.attendance100.mybicocca.ui.screen.search

import it.attendance100.mybicocca.domain.model.search.SearchResult

/**
 * Stable `testTag` identifiers for [SearchOverlay], referenced by both the overlay and its UI tests
 * so a user-visible copy change never breaks a test and a tag rename is a compile error rather than
 * a silently missed node. The overlay applies them with `Modifier.testTag`; tests anchor on the
 * constants instead of on Italian text.
 *
 * The search text field itself lives in the shell app bar (outside this overlay), so it has no tag
 * here; tests drive the query through the ViewModel instead. State markers cover the four mutually
 * exclusive branches — blank query with history, blank query with no history, a non-blank query
 * with no matches, and a non-blank query with ranked results — and item tags key history and result
 * rows by their stable identity.
 */
object SearchOverlayTestTags {
    const val ROOT = "search:root"
    const val HISTORY_LIST = "search:state:history"
    const val HISTORY_EMPTY = "search:state:historyEmpty"
    const val NO_RESULTS = "search:state:noResults"
    const val RESULTS_LIST = "search:state:results"
    const val TOP_HIT = "search:topHit"

    /** Tag for one recent-search row, keyed by the entry's query text. */
    fun historyItem(query: String) = "search:history:$query"

    /** Tag for one result row, keyed by the result's stable [SearchResult.key]. */
    fun resultItem(key: String) = "search:result:$key"
}
