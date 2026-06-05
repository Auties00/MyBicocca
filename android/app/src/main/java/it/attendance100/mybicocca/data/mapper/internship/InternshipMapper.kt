package it.attendance100.mybicocca.data.mapper.internship

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3InternshipApplicationAttachments
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3InternshipApplicationDetail
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3InternshipApplicationHeader
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3InternshipApplicationStateCode
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.internship.InternshipApplication
import it.attendance100.mybicocca.domain.model.internship.InternshipApplicationDetail
import it.attendance100.mybicocca.domain.model.internship.InternshipApplicationId
import it.attendance100.mybicocca.domain.model.internship.InternshipApplicationState
import it.attendance100.mybicocca.domain.model.internship.InternshipAttachment
import it.attendance100.mybicocca.domain.model.internship.InternshipEvaluationWindow
import it.attendance100.mybicocca.domain.model.internship.InternshipTrainingProject
import it.attendance100.mybicocca.domain.model.internship.InternshipTutor
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal fun Esse3InternshipApplicationHeader.toDomain(careerId: CareerId): InternshipApplication? {
    val id = domicileInternshipId ?: return null
    return InternshipApplication(
        id = InternshipApplicationId(id),
        careerId = careerId,
        academicYear = academicYearId?.toInt(),
        academicYearLabel = academicYearDescription?.takeIf { it.isNotBlank() },
        typeDescription = internshipTypeDescription?.takeIf { it.isNotBlank() }
            ?: extendedDescription?.takeIf { it.isNotBlank() },
        state = internshipApplicationStateCode.toDomain(),
        stateDescription = internshipApplicationStateDescription?.takeIf { it.isNotBlank() },
        companyName = entityDescription?.takeIf { it.isNotBlank() },
        opportunityTitle = opportunityTitle?.takeIf { it.isNotBlank() },
        conventionDescription = conventionDescription?.takeIf { it.isNotBlank() },
        cfuRecognitionEnabled = cFURecognitionAuthorization == 1,
    )
}

internal fun Esse3InternshipApplicationDetail.toDomain(careerId: CareerId): InternshipApplicationDetail? {
    val id = domicileInternshipId ?: return null
    return InternshipApplicationDetail(
        id = InternshipApplicationId(id),
        careerId = careerId,
        startDate = internshipStartDate.parseDate(),
        endDate = internshipEndDate.parseDate(),
        expectedSchedule = expectedSchedule?.takeIf { it.isNotBlank() },
        periodDescription = periodTypeDescription?.takeIf { it.isNotBlank() },
        durationHours = durationHours,
        durationMonths = durationMonths,
        durationWeeks = durationWeeks,
        durationDays = durationDays,
        weeklyInternshipDays = internshipWeeklyDaysNumber,
        opportunityTitle = opportunityTitle?.takeIf { it.isNotBlank() },
        opportunityDescription = opportunityDescription?.takeIf { it.isNotBlank() },
        opportunitySite = opportunitySite?.takeIf { it.isNotBlank() },
        conventionDescription = conventionDescription?.takeIf { it.isNotBlank() },
        conventionStartDate = conventionStartDate.parseDate(),
        conventionEndDate = conventionEndDate.parseDate(),
        protocolNumber = protocolNumber?.takeIf { it.isNotBlank() },
        stageTypeDescription = stageTypesDescription?.takeIf { it.isNotBlank() },
        areaDescription = areaDescription?.takeIf { it.isNotBlank() },
        sectorDescription = areasSectorsDescription?.takeIf { it.isNotBlank() },
        workInsertionDescription = workInsertionDescription?.takeIf { it.isNotBlank() },
        facilitations = facilitations?.takeIf { it.isNotBlank() },
        academicTutor = tutorFrom(tutorLecturerName, tutorLecturerSurname),
        companyTutor = tutorFrom(tutorSubjectName, tutorSubjectSurname),
        cfuRecognitionEnabled = cFURecognitionAuthorization == 1,
        trainingProject = InternshipTrainingProject(
            stateDescription = pfDescription?.takeIf { it.isNotBlank() },
            acceptedByStudent = studentPfAcceptance == 1,
            studentAcceptanceDate = studentPfAcceptanceDate.parseDate(),
            acceptedByCompany = companyPfAcceptance == 1,
            companyAcceptanceDate = companyPfAcceptanceDate.parseDate(),
            liabilityWaiverAccepted = liabilityWaiverAcceptance == 1,
            liabilityWaiverDate = indemnityAcceptanceDate.parseDate(),
            // Esse3 keeps a student- and a tutor-proposed copy of each text; show whichever
            // side actually filled it in.
            objectives = firstText(trainingObjectiveDescription, trainingObjectiveDescriptionTutor),
            activities = firstText(activitiesCarriedOutDescription, activitiesCarriedOutDescriptionTutor),
            expectedSkills = firstText(pendingComponents, pendingComponentsTutor),
            acquiredSkills = firstText(acquiredSkillsDescription, acquiredSkillsDescriptionTutor),
            trainingContent = firstText(
                trainingContent,
                trainingContentTutor,
                generalTrainingContent,
                specificTrainingContent,
            ),
            learningVerificationMode = firstText(learningVerificationMode, learningVerificationModeTutor),
        ),
        finalEvaluation = InternshipEvaluationWindow(
            enabled = finalValidationAuthorization == 1,
            start = finalEvaluationStartDate.parseDate(),
            end = finalEvaluationEndDate.parseDate(),
        ),
        midtermEvaluation = InternshipEvaluationWindow(
            enabled = midtermValidationAuthorization == 1,
            start = mtEvaluationStartDate.parseDate(),
            end = mtEvaluationEndDate.parseDate(),
        ),
        finalReport = InternshipEvaluationWindow(
            enabled = finalRelationAuthorization == 1,
            start = finalRelationStartDate.parseDate(),
            end = finalRelationEndDate.parseDate(),
        ),
    )
}

internal fun Esse3InternshipApplicationAttachments.toDomain(): InternshipAttachment? {
    val id = attachmentId ?: return null
    return InternshipAttachment(
        id = id,
        title = title?.takeIf { it.isNotBlank() },
        description = description?.takeIf { it.isNotBlank() },
        fileName = fileName?.takeIf { it.isNotBlank() },
        sizeBytes = size,
        modifiedOn = modificationDate.parseDate(),
        typeCode = attachmentTypeCode?.takeIf { it.isNotBlank() },
        ownedByStudent = isOwner == 1,
    )
}

private fun Esse3InternshipApplicationStateCode?.toDomain(): InternshipApplicationState = when (this) {
    Esse3InternshipApplicationStateCode.PreEnrolled -> InternshipApplicationState.Submitted
    Esse3InternshipApplicationStateCode.Confirmed -> InternshipApplicationState.Confirmed
    Esse3InternshipApplicationStateCode.Started -> InternshipApplicationState.Started
    Esse3InternshipApplicationStateCode.Closed -> InternshipApplicationState.Closed
    Esse3InternshipApplicationStateCode.Cancelled -> InternshipApplicationState.Cancelled
    Esse3InternshipApplicationStateCode.Rejected -> InternshipApplicationState.Rejected
    Esse3InternshipApplicationStateCode.NotAssigned -> InternshipApplicationState.NotAssigned
    is Esse3InternshipApplicationStateCode.Unknown, null -> InternshipApplicationState.Unknown
}

private fun tutorFrom(name: String?, surname: String?): InternshipTutor? {
    val fullName = listOfNotNull(
        name?.trim()?.takeIf { it.isNotEmpty() },
        surname?.trim()?.takeIf { it.isNotEmpty() },
    ).joinToString(" ")
    return fullName.takeIf { it.isNotEmpty() }?.let { InternshipTutor(it) }
}

private fun firstText(vararg candidates: String?): String? =
    candidates.firstNotNullOfOrNull { it?.trim()?.takeIf { text -> text.isNotEmpty() } }

private val ESSE3_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun String?.parseDate(): LocalDate? {
    val raw = this?.trim()?.takeIf { it.isNotBlank() } ?: return null
    val datePart = raw.substringBefore(' ')
    return runCatching { LocalDate.parse(datePart, ESSE3_DATE) }.getOrNull()
}
