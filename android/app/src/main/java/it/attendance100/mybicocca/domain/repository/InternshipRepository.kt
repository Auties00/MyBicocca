package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.internship.InternshipApplication
import it.attendance100.mybicocca.domain.model.internship.InternshipApplicationDetail
import it.attendance100.mybicocca.domain.model.internship.InternshipApplicationId
import it.attendance100.mybicocca.domain.model.internship.InternshipAttachment

// No Room cache, like TaxRepository: internship applications are low-volume and their
// state (confirmation, start, training-project acceptance) changes server-side, so a
// stale read is misleading. Methods throw on failure; ViewModels translate to SyncStatus.
interface InternshipRepository {
    suspend fun getApplications(careerId: CareerId): List<InternshipApplication>
    suspend fun getApplicationDetail(careerId: CareerId, applicationId: InternshipApplicationId): InternshipApplicationDetail
    suspend fun getApplicationAttachments(careerId: CareerId, applicationId: InternshipApplicationId): List<InternshipAttachment>
}
