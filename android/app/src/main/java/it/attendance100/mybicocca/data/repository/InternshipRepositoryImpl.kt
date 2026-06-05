package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.mapper.internship.toDomain
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3InternshipApplicationStateCode
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.internship.InternshipApplication
import it.attendance100.mybicocca.domain.model.internship.InternshipApplicationDetail
import it.attendance100.mybicocca.domain.model.internship.InternshipApplicationId
import it.attendance100.mybicocca.domain.model.internship.InternshipAttachment
import it.attendance100.mybicocca.domain.repository.InternshipRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InternshipRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
) : InternshipRepository {

    // Esse3 returns only PRE/CON/AVV when no state filter is passed; closed, cancelled,
    // rejected and not-assigned applications each need an explicit per-state call.
    override suspend fun getApplications(careerId: CareerId): List<InternshipApplication> {
        requireCareer(careerId)
        val api = sessionManager.esse3()
        val studentId = careerId.value
        val headers = coroutineScope {
            val defaults = async {
                api.internships.getStudentInternshipApplicationHeaders(studentId = studentId)
            }
            val extras = NON_DEFAULT_STATES.map { state ->
                async {
                    api.internships.getStudentInternshipApplicationHeaders(
                        studentId = studentId,
                        internshipApplicationStateCode = state,
                    )
                }
            }
            defaults.await() + extras.awaitAll().flatten()
        }
        return withContext(Dispatchers.Default) {
            headers
                .mapNotNull { it.toDomain(careerId) }
                .distinctBy { it.id }
                .sortedWith(
                    compareByDescending<InternshipApplication> { it.academicYear ?: Int.MIN_VALUE }
                        .thenByDescending { it.id.value },
                )
        }
    }

    override suspend fun getApplicationDetail(
        careerId: CareerId,
        applicationId: InternshipApplicationId,
    ): InternshipApplicationDetail {
        requireCareer(careerId)
        // Verified live: the default response already carries every training-project field.
        // Do NOT pass fields=ALL here — on this endpoint `fields` is a projection whitelist
        // and the literal "ALL" matches nothing, returning an empty object.
        return sessionManager.esse3().internships
            .getStudentInternshipApplication(
                studentId = careerId.value,
                domicileInternshipId = applicationId.value,
            )
            .toDomain(careerId)
            ?: error("Esse3 returned an internship application without an id.")
    }

    override suspend fun getApplicationAttachments(
        careerId: CareerId,
        applicationId: InternshipApplicationId,
    ): List<InternshipAttachment> {
        requireCareer(careerId)
        return sessionManager.esse3().internships
            .getStudentInternshipApplicationAttachments(
                studentId = careerId.value,
                domicileInternshipId = applicationId.value,
            )
            .mapNotNull { it.toDomain() }
    }

    private fun requireCareer(careerId: CareerId): Career {
        val account = sessionManager.activeAccount.value
            ?: error("No active account; cannot resolve career for internships.")
        return account.academic.careers.firstOrNull { it.id == careerId }
            ?: error("Career ${careerId.value} not found on active account.")
    }

    private companion object {
        val NON_DEFAULT_STATES = listOf(
            Esse3InternshipApplicationStateCode.Closed,
            Esse3InternshipApplicationStateCode.Cancelled,
            Esse3InternshipApplicationStateCode.Rejected,
            Esse3InternshipApplicationStateCode.NotAssigned,
        )
    }
}
