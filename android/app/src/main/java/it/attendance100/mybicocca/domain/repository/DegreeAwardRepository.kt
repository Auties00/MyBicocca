package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.degreeaward.CommitteeSession
import it.attendance100.mybicocca.domain.model.degreeaward.DiscussionMode
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationApplicationId
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationCallId
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationHub
import it.attendance100.mybicocca.domain.model.degreeaward.SupervisorAssignment
import it.attendance100.mybicocca.domain.model.degreeaward.SupervisorCandidate
import it.attendance100.mybicocca.domain.model.degreeaward.Thesis
import it.attendance100.mybicocca.domain.model.degreeaward.ThesisDraft
import it.attendance100.mybicocca.domain.model.degreeaward.ThesisId

// Graduation ("Conseguimento titolo") data is intentionally NOT cached locally — the
// application moves through irreversible secretariat-driven states and a stale read would
// mislead the student into a duplicate submission. Every getter hits Esse3 and THROWS on
// failure; the ViewModel translates to SyncStatus. Mirrors TaxRepository / ExamRepository.
//
// Every mutating method here drives a real, irreversible secretariat process. They are
// gated in the UI behind the corresponding open-window / application-state checks.
interface DegreeAwardRepository {

    // Assembles the whole graduation picture: the student's application (if any), the
    // linked thesis detail, the committee session, the final result, and the open calls.
    suspend fun getHub(careerId: CareerId): GraduationHub

    suspend fun getThesis(thesisId: ThesisId): Thesis

    suspend fun searchSupervisors(surname: String, includeExternal: Boolean): List<SupervisorCandidate>

    suspend fun getDiscussionModes(): List<DiscussionMode>

    suspend fun getCommitteeSession(callId: GraduationCallId): CommitteeSession?

    // --- IRREVERSIBLE mutations below. Never call during read-only probing. ---

    // Submits the graduation application for the chosen open call.
    suspend fun submitApplication(careerId: CareerId, callId: GraduationCallId)

    // Cancels an in-progress application.
    suspend fun cancelApplication(careerId: CareerId, applicationId: GraduationApplicationId)

    // Attaches the thesis metadata (title/abstract/type/keywords) to the application.
    suspend fun submitThesis(
        careerId: CareerId,
        applicationId: GraduationApplicationId,
        committeeRegulationId: Long?,
        draft: ThesisDraft,
    )

    // Assigns the relatore + correlatori to the thesis.
    suspend fun assignSupervisors(thesisId: ThesisId, assignments: List<SupervisorAssignment>)

    // Sets the public / embargo consultation mode for the deposited thesis.
    suspend fun setDiscussionMode(thesisId: ThesisId, discussionModeCode: String)
}
