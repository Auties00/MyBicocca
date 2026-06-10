package it.attendance100.mybicocca.domain.model.account

import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId

/**
 * Esse3 (s3w.si.unimib.it) side of a saved account, captured at sign-in from the login session
 * and the careers endpoint and cached in Room.
 *
 * @property recordUserId Esse3 login user id of the session; unique per student and used to
 *   recognize an account that signs in again on the same device.
 * @property personId Esse3 person id; identifies the student in person-scoped endpoints such
 *   as the profile photo, academic titles, and tax records. 0 when Esse3 omits it.
 * @property fiscalCode Italian fiscal code, when Esse3 exposes it.
 * @property careers Every career Esse3 returns for the student, ended ones included.
 * @property selectedCareerId Career the student is working in; scopes the calendar, the
 *   transcript, and the other career-bound features.
 */
data class AcademicIdentity(
    val recordUserId: String,
    val personId: Long,
    val fiscalCode: String?,
    val careers: List<Career>,
    val selectedCareerId: CareerId,
)
