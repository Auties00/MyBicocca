package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.document.AcademicTitle
import it.attendance100.mybicocca.domain.model.document.BadgeBlobId
import it.attendance100.mybicocca.domain.model.document.StudentBadge

/**
 * Student documents from Esse3: the university card ("tessera") and academic titles.
 *
 * Documents & card data is live-first — it changes through registry back-office actions and
 * is read on demand from Esse3 while connectivity exists; every call throws on failure and
 * the ViewModel translates to SyncStatus. Each read (badge, badge images, titles) keeps an
 * offline snapshot of its last success purely for display when the device has no network.
 * The same policy applies to taxes.
 *
 * Only the badge and titles sections are exposed: the personal-attachment and
 * career-attachment Esse3 endpoints reject the STUDENT profile (HTTP 403), so they are
 * deliberately absent from this contract.
 */
interface DocumentRepository {

    suspend fun getBadge(careerId: CareerId): StudentBadge?

    suspend fun getTitles(careerId: CareerId): List<AcademicTitle>

    /**
     * Front/rear card artwork bytes. Only call when [StudentBadge] exposes a non-null blobId
     * and the corresponding hasFrontImage / hasRearImage flag is set.
     */
    suspend fun getBadgeImage(blobId: BadgeBlobId, rear: Boolean): ByteArray
}
