package it.attendance100.mybicocca.domain.model.search

import java.time.Instant

/**
 * A recent search of one account, persisted by the search history store and shown in the
 * search overlay before the user types.
 *
 * @property query The submitted query text.
 * @property timestamp When the query was last submitted.
 * @property pickedKey Stable key ([SearchResult.key]) of the result the user opened from
 *   this query, when the search ended in a tap. Powers the adaptive "same query, same
 *   result" boost.
 */
data class SearchHistoryEntry(
    val query: String,
    val timestamp: Instant,
    val pickedKey: String? = null,
)
