package it.attendance100.mybicocca.data.mapper.degreeaward

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSession
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionLocation
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExternalSubject
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SupervisorTeacher
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TeachingCommission
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TeachingDomainSummary
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ThesisAttachment
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ThesisConsultationMode
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ThesisSummary
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ThesisSupervisors
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ThesisTeachingDomainSummary
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ThesisTypes
import it.attendance100.mybicocca.domain.model.degreeaward.CommitteeMember
import it.attendance100.mybicocca.domain.model.degreeaward.CommitteeSession
import it.attendance100.mybicocca.domain.model.degreeaward.DiscussionMode
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationApplication
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationApplicationId
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationCall
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationCallId
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationResult
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationStage
import it.attendance100.mybicocca.domain.model.degreeaward.SupervisorCandidate
import it.attendance100.mybicocca.domain.model.degreeaward.Thesis
import it.attendance100.mybicocca.domain.model.degreeaward.ThesisAttachment
import it.attendance100.mybicocca.domain.model.degreeaward.ThesisId
import it.attendance100.mybicocca.domain.model.degreeaward.ThesisSupervisor
import it.attendance100.mybicocca.domain.model.degreeaward.ThesisType
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val Esse3DateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

// Esse3 emits DD/MM/YYYY, sometimes followed by a time component — keep the date part.
internal fun String?.toEsse3Date(): LocalDate? = this?.take(10)?.takeIf { it.isNotBlank() }?.let {
    runCatching { LocalDate.parse(it, Esse3DateFormat) }.getOrNull()
}

// Times arrive as HH:mm or HH:mm:ss, occasionally prefixed by a date.
internal fun String?.toEsse3Time(): LocalTime? {
    val raw = this?.trim()?.takeIf { it.isNotBlank() } ?: return null
    val timePart = raw.substringAfterLast(' ').take(8)
    return runCatching { LocalTime.parse(timePart.take(5)) }.getOrNull()
}

// The single existing CTIT application states. The repository derives the coarse stage
// from these plus whether a final grade is present.
internal fun graduationStageFor(summary: Esse3TeachingDomainSummary?): GraduationStage = when {
    summary == null -> GraduationStage.Open
    summary.finalGrade != null -> GraduationStage.Completed
    summary.state.equals("ANN", ignoreCase = true) -> GraduationStage.Cancelled
    summary.state.equals("CON", ignoreCase = true) ||
        summary.state.equals("CHI", ignoreCase = true) -> GraduationStage.Confirmed
    else -> GraduationStage.InProgress
}

internal fun graduationStateLabel(stateCode: String?): String = when (stateCode?.uppercase()) {
    "PRE" -> "In presentazione"
    "PRG" -> "In bozza"
    "CON" -> "Confermata"
    "CHI" -> "Chiusa"
    "ANN" -> "Annullata"
    "RIF" -> "Rifiutata"
    null -> "Domanda attiva"
    else -> stateCode
}

internal fun Esse3TeachingDomainSummary.toApplication(): GraduationApplication {
    val confirmed = state.equals("CON", ignoreCase = true) || state.equals("CHI", ignoreCase = true)
    return GraduationApplication(
        id = GraduationApplicationId(domicileCommitteeId ?: 0L),
        stateCode = state,
        stateLabel = graduationStateLabel(state),
        applicationDate = committeeApplicationDate.toEsse3Date(),
        callDescription = callCommitteeDescription,
        sessionDescription = committeeSessionDescription,
        sessionStartDate = committeeSessionStartDate.toEsse3Date(),
        sessionEndDate = committeeSessionEndDate.toEsse3Date(),
        committeeRegulationId = null,
        thesisId = thesisId?.let { ThesisId(it.toLong()) },
        isConfirmed = confirmed,
        isCancellable = !confirmed && !state.equals("ANN", ignoreCase = true) && finalGrade == null,
    )
}

internal fun Esse3TeachingDomainSummary.toResult(): GraduationResult? {
    if (finalGrade == null && committeeDate.toEsse3Date() == null) return null
    return GraduationResult(
        date = committeeDate.toEsse3Date(),
        finalGrade = finalGrade,
        cumLaude = (cumLaudeFlag ?: 0L) != 0L,
        mention = (mentionFlag ?: 0L) != 0L,
        commendation = (commendationFlag ?: 0L) != 0L,
        finalCredits = finalCredits,
        judgmentDescription = finalProJudgmentTypesDescription,
    )
}

// The committee session embedded in the application summary (when the secretariat has
// already scheduled the seduta).
internal fun Esse3TeachingDomainSummary.toSessionOrNull(): CommitteeSession? {
    val date = committeeSessionDate.toEsse3Date()
    if (date == null && classroomDescription == null && buildingDescription == null) return null
    return CommitteeSession(
        sessionId = null,
        date = date,
        time = committeeSessionSchedule.toEsse3Time(),
        classroomDescription = classroomDescription,
        buildingDescription = buildingDescription,
        departmentDescription = null,
        committeeMembers = emptyList(),
    )
}

// appelliCt is served by the shared exam-session shape; only the overlapping fields are
// reliably populated for graduation calls.
internal fun Esse3ExamSession.toGraduationCall(): GraduationCall? {
    val id = examCallId ?: return null
    return GraduationCall(
        id = GraduationCallId(id),
        description = callDescription
            ?: activityDescription
            ?: courseOfStudyDescription
            ?: "Appello di laurea",
        sessionDescription = stateDescription,
        academicYearId = academicYearCalendarId?.toLong(),
        courseOfStudyCode = courseOfStudyCode,
        courseOfStudyDescription = courseOfStudyDescription,
        callDate = callStartDate.toEsse3Date(),
        sessionStartDate = callStartDate.toEsse3Date(),
        sessionEndDate = null,
        note = notes,
        deadlines = emptyList(),
    )
}

internal fun Esse3ThesisTypes.toThesisType(): ThesisType? {
    val code = thesisTypeCode ?: return null
    return ThesisType(
        code = code,
        description = description ?: code,
        committeeRegulationId = committeeRegulationId?.toLong(),
    )
}

internal fun Esse3ThesisConsultationMode.toDiscussionMode(): DiscussionMode? {
    val code = thesisDiscussionModeCode ?: return null
    return DiscussionMode(
        code = code,
        description = description ?: code,
        embargoDays = embargoDays,
    )
}

internal fun Esse3SupervisorTeacher.toCandidate(): SupervisorCandidate = SupervisorCandidate(
    lecturerId = lecturerId,
    externalSubjectId = null,
    name = name.orEmpty(),
    surname = surname.orEmpty(),
    departmentDescription = departmentDescription,
    roleDescription = lecturerRoleDescription,
    isExternal = false,
)

internal fun Esse3ExternalSubject.toCandidate(): SupervisorCandidate = SupervisorCandidate(
    lecturerId = null,
    externalSubjectId = externalSubjectId,
    name = name.orEmpty(),
    surname = surname.orEmpty(),
    departmentDescription = department ?: didacticResponsibleStructure,
    roleDescription = externalSubjectTypeDescription,
    isExternal = true,
)

internal fun Esse3ThesisSummary.toThesis(): Thesis = Thesis(
    id = ThesisId(thesisId ?: 0L),
    titleItalian = thesisTitleItalian,
    titleEnglish = thesisTitleEnglish,
    abstractItalian = thesisAbstractItalian,
    abstractEnglish = thesisAbstractEnglish,
    typeDescription = thesisTypeDescription,
    statusLabel = thesisStatus,
    language = thesisLanguage,
    depositDate = null,
    discussionModeCode = thesisDiscussionModeCode,
    keywords = emptyList(),
    supervisors = supervisors.map { it.toSupervisor() },
    attachments = thesisAttachments.map { it.toAttachment() },
)

internal fun Esse3ThesisTeachingDomainSummary.toThesis(): Thesis = Thesis(
    id = ThesisId(thesisId ?: 0L),
    titleItalian = thesisTitleItalian,
    titleEnglish = thesisTitleEnglish,
    abstractItalian = thesisAbstractItalian,
    abstractEnglish = thesisAbstractEnglish,
    typeDescription = thesisTypeDescription,
    statusLabel = thesisStatus,
    language = thesisLanguage,
    depositDate = thesisDepositDate.toEsse3Date(),
    discussionModeCode = thesisDiscussionModeCode,
    keywords = emptyList(),
    supervisors = supervisors.map {
        ThesisSupervisor(
            relationTypeCode = it.relationTypeCode,
            relationTypeLabel = it.relationTypeDescription,
            lecturerId = it.lecturerId,
            externalSubjectId = it.externalSubjectId,
            displayName = null,
        )
    },
    attachments = thesisAttachments.map { it.toAttachment() },
)

private fun Esse3ThesisSupervisors.toSupervisor(): ThesisSupervisor = ThesisSupervisor(
    relationTypeCode = relationTypeCode,
    relationTypeLabel = relationTypeDescription,
    lecturerId = lecturerId,
    externalSubjectId = externalSubjectId,
    displayName = null,
)

private fun Esse3ThesisAttachment.toAttachment(): ThesisAttachment = ThesisAttachment(
    id = attachmentId,
    fileName = fileName,
    title = title,
    stateCode = thesisAttachmentStateCode,
    stateLabel = thesisAttachmentStateDescription ?: thesisAttachmentStateLabel(thesisAttachmentStateCode),
    isFinal = (definitionFlag ?: 0) != 0,
    antiplagiarismLink = antiplagiarismLink,
)

private fun thesisAttachmentStateLabel(code: String?): String? = when (code?.uppercase()) {
    "A" -> "Approvato"
    "R" -> "Rifiutato"
    "I" -> "Inserito"
    else -> null
}

internal fun Esse3ExamSessionLocation.toCommitteeSession(): CommitteeSession = CommitteeSession(
    sessionId = committeeSessionId,
    date = sessionDate.toEsse3Date(),
    time = sessionSchedule.toEsse3Time(),
    classroomDescription = classroomDescription,
    buildingDescription = null,
    departmentDescription = departmentDescription,
    committeeMembers = committeeMembers.map { it.toMember() },
)

private fun Esse3TeachingCommission.toMember(): CommitteeMember = CommitteeMember(
    title = title,
    name = name,
    surname = surname,
    roleDescription = roleDescription,
)
