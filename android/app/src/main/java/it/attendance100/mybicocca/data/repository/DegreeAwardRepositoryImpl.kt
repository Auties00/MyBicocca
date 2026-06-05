package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.mapper.degreeaward.graduationStageFor
import it.attendance100.mybicocca.data.mapper.degreeaward.toApplication
import it.attendance100.mybicocca.data.mapper.degreeaward.toCandidate
import it.attendance100.mybicocca.data.mapper.degreeaward.toCommitteeSession
import it.attendance100.mybicocca.data.mapper.degreeaward.toDiscussionMode
import it.attendance100.mybicocca.data.mapper.degreeaward.toGraduationCall
import it.attendance100.mybicocca.data.mapper.degreeaward.toResult
import it.attendance100.mybicocca.data.mapper.degreeaward.toSessionOrNull
import it.attendance100.mybicocca.data.mapper.degreeaward.toThesis
import it.attendance100.mybicocca.data.mapper.degreeaward.toThesisType
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ApplicationDataInsert
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SupervisorsInsert
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TeachingDomainInsert
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TeachingDomainSummary
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ThesisDataInsert
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ThesisIntoTeachingDomainInsert
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.degreeaward.CommitteeSession
import it.attendance100.mybicocca.domain.model.degreeaward.DiscussionMode
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationApplicationId
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationCallId
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationHub
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationStage
import it.attendance100.mybicocca.domain.model.degreeaward.SupervisorAssignment
import it.attendance100.mybicocca.domain.model.degreeaward.SupervisorCandidate
import it.attendance100.mybicocca.domain.model.degreeaward.Thesis
import it.attendance100.mybicocca.domain.model.degreeaward.ThesisDraft
import it.attendance100.mybicocca.domain.model.degreeaward.ThesisId
import it.attendance100.mybicocca.domain.repository.DegreeAwardRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DegreeAwardRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
) : DegreeAwardRepository {

    override suspend fun getHub(careerId: CareerId): GraduationHub = coroutineScope {
        val api = sessionManager.esse3().degreeAward
        val studentId = careerId.value

        // The application is the spine; the thesis types and the open calls are auxiliary.
        // Each auxiliary fetch degrades to empty so a window-closed 403 / timeout on one of
        // them doesn't blank the whole hub.
        val applicationsDeferred = async { api.getCommitteeApplicationByStudentId(studentId) }
        val typesDeferred = async {
            runCatching { api.getThesisType() }.getOrDefault(emptyList())
        }
        val callsDeferred = async {
            runCatching { api.getExamCalls() }.getOrDefault(emptyList())
        }

        val applications = applicationsDeferred.await()
        val summary: Esse3TeachingDomainSummary? = applications
            .filterNot { it.state.equals("ANN", ignoreCase = true) }
            .maxByOrNull { it.domicileCommitteeId ?: Long.MIN_VALUE }
            ?: applications.firstOrNull()

        val thesisTypes = typesDeferred.await().mapNotNull { it.toThesisType() }
        val openCalls = callsDeferred.await().mapNotNull { it.toGraduationCall() }

        // When an application exists, pull its full thesis detail (supervisors/attachments
        // live there, not on the summary).
        val thesis: Thesis? = summary?.let { loadThesis(it) }

        val application = summary?.toApplication()
        val stage = when {
            summary == null && openCalls.isEmpty() -> GraduationStage.NotOpen
            summary == null -> GraduationStage.Open
            else -> graduationStageFor(summary)
        }

        GraduationHub(
            stage = stage,
            application = application,
            thesis = thesis,
            session = summary?.toSessionOrNull(),
            result = summary?.toResult(),
            openCalls = openCalls,
            thesisTypes = thesisTypes,
        )
    }

    private suspend fun loadThesis(summary: Esse3TeachingDomainSummary): Thesis? {
        val domId = summary.domicileCommitteeId ?: return null
        val api = sessionManager.esse3().degreeAward
        return runCatching { api.getTheses(domId).toThesis() }.getOrNull()
    }

    override suspend fun getThesis(thesisId: ThesisId): Thesis =
        sessionManager.esse3().degreeAward.getThesisByThesisId(thesisId.value).toThesis()

    override suspend fun searchSupervisors(
        surname: String,
        includeExternal: Boolean,
    ): List<SupervisorCandidate> = coroutineScope {
        val api = sessionManager.esse3().degreeAward
        val lecturersDeferred = async {
            runCatching { api.getThesisRelatedDocuments(surname = surname, limit = 25) }.getOrDefault(emptyList())
        }
        val externalsDeferred = async {
            if (!includeExternal) emptyList()
            else runCatching { api.getExternalSubject(surname = surname, limit = 25) }.getOrDefault(emptyList())
        }
        val lecturers = lecturersDeferred.await().map { it.toCandidate() }
        val externals = externalsDeferred.await().map { it.toCandidate() }
        (lecturers + externals).filter { it.displayName.isNotBlank() }
    }

    override suspend fun getDiscussionModes(): List<DiscussionMode> =
        sessionManager.esse3().degreeAward.getThesisDiscussionMode(authorizationFlag = 1)
            .mapNotNull { it.toDiscussionMode() }

    override suspend fun getCommitteeSession(callId: GraduationCallId): CommitteeSession? {
        val sessions = sessionManager.esse3().degreeAward.getCommitteeCallSession()
        return sessions.firstOrNull { it.callCommitteeId == callId.value }?.toCommitteeSession()
            ?: sessions.firstOrNull()?.toCommitteeSession()
    }

    // --- IRREVERSIBLE mutations. Each posts/puts to the live CTIT process. ---

    override suspend fun submitApplication(careerId: CareerId, callId: GraduationCallId) {
        val body = Esse3TeachingDomainInsert(
            application = Esse3ApplicationDataInsert(
                studentId = careerId.value.toInt(),
                callCommitteeId = callId.value,
            ),
        )
        sessionManager.esse3().degreeAward.postCommitteeApplication(body)
    }

    override suspend fun cancelApplication(careerId: CareerId, applicationId: GraduationApplicationId) {
        sessionManager.esse3().degreeAward.putCancelCommitteeApplication(
            domicileTitleDeliveryId = applicationId.value,
            studentId = careerId.value,
            deadlinesCheck = true,
        )
    }

    override suspend fun submitThesis(
        careerId: CareerId,
        applicationId: GraduationApplicationId,
        committeeRegulationId: Long?,
        draft: ThesisDraft,
    ) {
        val keywords = draft.keywords.map { it.trim() }.filter { it.isNotEmpty() }.take(5)
        val body = Esse3ThesisIntoTeachingDomainInsert(
            domicileCommitteeId = applicationId.value,
            thesis = Esse3ThesisDataInsert(
                studentId = careerId.value.toInt(),
                committeeRegulationId = committeeRegulationId,
                thesisTypeCode = draft.thesisTypeCode,
                thesisTitleItalian = draft.titleItalian.trim(),
                thesisTitleEnglish = draft.titleEnglish.trim().ifEmpty { null },
                thesisAbstractItalian = draft.abstractItalian.trim().ifEmpty { null },
                thesisAbstractEnglish = draft.abstractEnglish.trim().ifEmpty { null },
                thesisLanguage = draft.languageId,
                almaKeyword1 = keywords.getOrNull(0),
                almaKeyword2 = keywords.getOrNull(1),
                almaKeyword3 = keywords.getOrNull(2),
                almaKeyword4 = keywords.getOrNull(3),
                almaKeyword5 = keywords.getOrNull(4),
            ),
        )
        sessionManager.esse3().degreeAward.postThesisIntoCommitteeApplication(body)
    }

    override suspend fun assignSupervisors(thesisId: ThesisId, assignments: List<SupervisorAssignment>) {
        val body = assignments.map {
            Esse3SupervisorsInsert(
                relationTypeCode = it.relationTypeCode,
                lecturerId = it.lecturerId,
                externalSubjectId = it.externalSubjectId,
            )
        }
        sessionManager.esse3().degreeAward.putThesisRelation(thesisId.value, body)
    }

    override suspend fun setDiscussionMode(thesisId: ThesisId, discussionModeCode: String) {
        sessionManager.esse3().degreeAward.putThesisDiscussionMode(thesisId.value, discussionModeCode)
    }
}
