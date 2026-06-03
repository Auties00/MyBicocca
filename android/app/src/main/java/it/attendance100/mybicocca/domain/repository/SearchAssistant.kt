package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.search.SearchAssistantStatus
import it.attendance100.mybicocca.domain.model.search.SearchSuggestion
import kotlinx.coroutines.flow.Flow

// On-device generative interpretation of search queries. The impl self-gates on device
// support; callers only see Unavailable and skip the AI card.
interface SearchAssistant {
    fun observeStatus(): Flow<SearchAssistantStatus>

    // Kicks the model download when Downloadable; true once the assistant is usable.
    suspend fun ensureReady(): Boolean

    // Null when unavailable, interrupted, or the output can't be parsed — never throws.
    suspend fun interpret(query: String): SearchSuggestion?
}
