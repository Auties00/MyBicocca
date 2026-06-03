package it.attendance100.mybicocca.domain.model.search

// On-device AI interpretation of a free-form query, rendered as a card on submit.
data class SearchSuggestion(
    val intent: String,
    val target: SearchDestination?,
    val explanation: String?,
)
