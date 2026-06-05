package it.attendance100.mybicocca.domain.model.degreeaward

import java.time.LocalDate
import java.time.LocalTime

@JvmInline
value class GraduationApplicationId(val value: Long)

@JvmInline
value class GraduationCallId(val value: Long)

@JvmInline
value class ThesisId(val value: Long)

// The student's position in the graduation process, derived from whether an application
// exists and what state it carries. Drives which steps the hub unlocks.
enum class GraduationStage {
    // No application yet and no open call the student can apply to.
    NotOpen,

    // No application yet, but at least one graduation call is open to apply to.
    Open,

    // An application exists and is in progress (thesis / supervisors / attachments).
    InProgress,

    // The application is confirmed by the secretariat; awaiting the session.
    Confirmed,

    // The discussion happened and a final grade is recorded.
    Completed,

    // The application was cancelled.
    Cancelled,
}

// The full graduation picture for the active career, assembled by the repository from the
// application summary (when present), the thesis detail, and the list of open calls. Every
// nested piece is nullable because the student can be at any point in the timeline.
data class GraduationHub(
    val stage: GraduationStage,
    val application: GraduationApplication?,
    val thesis: Thesis?,
    val session: CommitteeSession?,
    val result: GraduationResult?,
    val openCalls: List<GraduationCall>,
    val thesisTypes: List<ThesisType>,
)

data class GraduationApplication(
    val id: GraduationApplicationId,
    val stateCode: String?,
    val stateLabel: String,
    val applicationDate: LocalDate?,
    val callDescription: String?,
    val sessionDescription: String?,
    val sessionStartDate: LocalDate?,
    val sessionEndDate: LocalDate?,
    val committeeRegulationId: Long?,
    val thesisId: ThesisId?,
    // True once the secretariat has confirmed; mutations are no longer offered.
    val isConfirmed: Boolean,
    val isCancellable: Boolean,
)

data class GraduationCall(
    val id: GraduationCallId,
    val description: String,
    val sessionDescription: String?,
    val academicYearId: Long?,
    val courseOfStudyCode: String?,
    val courseOfStudyDescription: String?,
    val callDate: LocalDate?,
    val sessionStartDate: LocalDate?,
    val sessionEndDate: LocalDate?,
    val note: String?,
    val deadlines: List<GraduationDeadline>,
)

data class GraduationDeadline(
    val typeLabel: String,
    val start: LocalDate?,
    val end: LocalDate?,
)

data class Thesis(
    val id: ThesisId,
    val titleItalian: String?,
    val titleEnglish: String?,
    val abstractItalian: String?,
    val abstractEnglish: String?,
    val typeDescription: String?,
    val statusLabel: String?,
    val language: String?,
    val depositDate: LocalDate?,
    val discussionModeCode: String?,
    val keywords: List<String>,
    val supervisors: List<ThesisSupervisor>,
    val attachments: List<ThesisAttachment>,
)

data class ThesisType(
    val code: String,
    val description: String,
    val committeeRegulationId: Long?,
)

// A supervisor already assigned to the thesis.
data class ThesisSupervisor(
    val relationTypeCode: String?,
    val relationTypeLabel: String?,
    val lecturerId: Long?,
    val externalSubjectId: Long?,
    val displayName: String?,
)

// A candidate returned by the lecturer / external-subject search, to be assigned.
data class SupervisorCandidate(
    val lecturerId: Long?,
    val externalSubjectId: Long?,
    val name: String,
    val surname: String,
    val departmentDescription: String?,
    val roleDescription: String?,
    val isExternal: Boolean,
) {
    val displayName: String get() = listOf(surname, name).filter { it.isNotBlank() }.joinToString(" ")
}

data class ThesisAttachment(
    val id: Long?,
    val fileName: String?,
    val title: String?,
    val stateCode: String?,
    val stateLabel: String?,
    val isFinal: Boolean,
    val antiplagiarismLink: String?,
)

data class DiscussionMode(
    val code: String,
    val description: String,
    val embargoDays: Int?,
)

data class CommitteeSession(
    val sessionId: Long?,
    val date: LocalDate?,
    val time: LocalTime?,
    val classroomDescription: String?,
    val buildingDescription: String?,
    val departmentDescription: String?,
    val committeeMembers: List<CommitteeMember>,
)

data class CommitteeMember(
    val title: String?,
    val name: String?,
    val surname: String?,
    val roleDescription: String?,
) {
    val displayName: String
        get() = listOfNotNull(title, name, surname).filter { it.isNotBlank() }.joinToString(" ")
}

data class GraduationResult(
    val date: LocalDate?,
    val finalGrade: Long?,
    val cumLaude: Boolean,
    val mention: Boolean,
    val commendation: Boolean,
    val finalCredits: Double?,
    val judgmentDescription: String?,
)

// Draft the student fills in the thesis step before posting.
data class ThesisDraft(
    val titleItalian: String,
    val titleEnglish: String,
    val abstractItalian: String,
    val abstractEnglish: String,
    val thesisTypeCode: String,
    val languageId: Long?,
    val keywords: List<String>,
)

// Draft the student fills in the supervisors step before assigning.
data class SupervisorAssignment(
    val relationTypeCode: String,
    val lecturerId: Long?,
    val externalSubjectId: Long?,
)
