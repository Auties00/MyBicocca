package it.attendance100.mybicocca.domain.model.account

import java.time.Instant

/**
 * A university account saved on the device, as listed in the account switcher and shown in the
 * profile. Built at sign-in by merging the Esse3 session (name, careers) with the Moodle site
 * info, then persisted in Room; multiple accounts can coexist and one is active at a time.
 *
 * @property id App-generated identifier, stable across re-sign-ins of the same student.
 * @property username Login identity in full e-mail form (a typed short username is completed
 *   with the campus domain at sign-in).
 * @property displayName Student's name composed from the Esse3 first and last name; falls back
 *   to the Esse3 user id when both are blank.
 * @property academic Esse3 side of the identity, including the career list and the selection.
 * @property learning Moodle side of the identity.
 * @property createdAt When the account was first saved on this device.
 * @property lastUsedAt Last activation (sign-in, account switch, or career selection); orders
 *   the account list.
 * @property lastSyncedAt Last successful career refresh from Esse3.
 */
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
