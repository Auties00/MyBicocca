package it.attendance100.mybicocca.domain.model.internship

import it.attendance100.mybicocca.domain.model.career.CareerId
import java.time.LocalDate

// Full training-project view of a domanda di tirocinio.
data class InternshipApplicationDetail(
    val id: InternshipApplicationId,
    val careerId: CareerId,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val expectedSchedule: String?,
    val periodDescription: String?,
    val durationHours: Long?,
    val durationMonths: Long?,
    val durationWeeks: Long?,
    val durationDays: Long?,
    val weeklyInternshipDays: Long?,
    val opportunityTitle: String?,
    val opportunityDescription: String?,
    val opportunitySite: String?,
    val conventionDescription: String?,
    val conventionStartDate: LocalDate?,
    val conventionEndDate: LocalDate?,
    val protocolNumber: String?,
    val stageTypeDescription: String?,
    val areaDescription: String?,
    val sectorDescription: String?,
    val workInsertionDescription: String?,
    val facilitations: String?,
    val academicTutor: InternshipTutor?,
    val companyTutor: InternshipTutor?,
    val cfuRecognitionEnabled: Boolean,
    val trainingProject: InternshipTrainingProject,
    val finalEvaluation: InternshipEvaluationWindow,
    val midtermEvaluation: InternshipEvaluationWindow,
    val finalReport: InternshipEvaluationWindow,
)

data class InternshipTutor(
    val fullName: String,
)

data class InternshipTrainingProject(
    val stateDescription: String?,
    val acceptedByStudent: Boolean,
    val studentAcceptanceDate: LocalDate?,
    val acceptedByCompany: Boolean,
    val companyAcceptanceDate: LocalDate?,
    val liabilityWaiverAccepted: Boolean,
    val liabilityWaiverDate: LocalDate?,
    val objectives: String?,
    val activities: String?,
    val expectedSkills: String?,
    val acquiredSkills: String?,
    val trainingContent: String?,
    val learningVerificationMode: String?,
)

// One of the compilation windows Esse3 opens around the end of the stage
// (valutazione finale, valutazione mid-term, relazione finale).
data class InternshipEvaluationWindow(
    val enabled: Boolean,
    val start: LocalDate?,
    val end: LocalDate?,
)
