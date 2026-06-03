package it.attendance100.mybicocca.domain.model.search

import java.time.Instant

data class SearchHistoryEntry(
    val query: String,
    val timestamp: Instant,
)
