package it.attendance100.mybicocca.domain.model.internship

import java.time.Instant

// A locally-bookmarked internship opportunity. No Esse3 student backend exists for this,
// so it is stored only in Room (per account).
data class SavedOpportunity(
    val id: String,
    val title: String,
    val company: String?,
    val url: String?,
    val savedAt: Instant,
)
