package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.document.AcademicTitle
import it.attendance100.mybicocca.domain.model.document.BadgeBlobId
import it.attendance100.mybicocca.domain.model.document.StudentBadge

// Documents & card data is intentionally NOT cached locally — it changes through registry
// back-office actions and is read on demand from Esse3. Every call hits the network and
// throws on failure; the ViewModel translates to SyncStatus. Mirrors TaxRepository.
//
// Only the badge ("tessera") and titles sections are exposed: live probing showed the
// personal-attachment and career-attachment Esse3 endpoints reject the STUDENT profile
// (HTTP 403), so they are deliberately absent from this contract.
interface DocumentRepository {

    suspend fun getBadge(careerId: CareerId): StudentBadge?

    suspend fun getTitles(careerId: CareerId): List<AcademicTitle>

    // Front/rear card artwork bytes. Only call when StudentBadge exposes a non-null blobId
    // and the corresponding hasFrontImage / hasRearImage flag is set.
    suspend fun getBadgeImage(blobId: BadgeBlobId, rear: Boolean): ByteArray
}
