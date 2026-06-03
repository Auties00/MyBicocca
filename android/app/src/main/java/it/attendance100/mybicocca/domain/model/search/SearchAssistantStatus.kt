package it.attendance100.mybicocca.domain.model.search

// Mirrors ML Kit's FeatureStatus without leaking the SDK enum into domain.
enum class SearchAssistantStatus {
    Unavailable,
    Downloadable,
    Downloading,
    Available,
}
