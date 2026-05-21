package it.attendance100.mybicocca.domain.model.account

import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId

data class AcademicIdentity(
    val recordUserId: String,
    val personId: Long,
    val fiscalCode: String?,
    val careers: List<Career>,
    val selectedCareerId: CareerId,
)
