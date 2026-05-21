package it.attendance100.mybicocca.domain.model.account

import java.time.Instant

data class Account(
    val id: AccountId,
    val username: String,
    val displayName: String,
    val academic: AcademicIdentity,
    val learning: LearningIdentity,
    val createdAt: Instant,
    val lastUsedAt: Instant,
    val lastSyncedAt: Instant,
)
