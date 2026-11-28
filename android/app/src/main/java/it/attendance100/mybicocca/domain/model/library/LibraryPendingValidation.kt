package it.attendance100.mybicocca.domain.model.library

import java.time.Instant

/**
 * An in-flight e-mail validation request persisted across process death while the student
 * switches to their mail app to open the Affluences link.
 */
data class LibraryPendingValidation(
    val email: String,
    val requestUuid: String,
    val createdAt: Instant,
)
