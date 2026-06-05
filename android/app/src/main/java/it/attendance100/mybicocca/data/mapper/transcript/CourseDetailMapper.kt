package it.attendance100.mybicocca.data.mapper.transcript

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PrerequisitesCheck
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Result
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptTest
import it.attendance100.mybicocca.domain.model.transcript.AttemptOutcome
import it.attendance100.mybicocca.domain.model.transcript.CourseAttempt
import it.attendance100.mybicocca.domain.model.transcript.PrerequisiteStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val ESSE3_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy")

// Esse3 dates arrive as "dd/MM/yyyy HH:mm:ss"; keep only the date part.
private fun parseEsse3Date(raw: String?): LocalDate? =
    raw?.substringBefore(' ')
        ?.takeIf { it.isNotBlank() }
        ?.let { runCatching { LocalDate.parse(it, ESSE3_DATE) }.getOrNull() }

fun Esse3TranscriptTest.toDomain(): CourseAttempt = CourseAttempt(
    id = activityRegulationId,
    callDate = parseEsse3Date(callDate),
    sessionDescription = sessionDescription?.trim()?.takeIf { it.isNotEmpty() },
    statusDescription = regulationStatusDescription?.trim()?.takeIf { it.isNotEmpty() },
    finalOutcome = finalOutcome?.toDomain(),
    writtenOutcome = writingOutcome?.toDomain(),
    partialOutcome = partialOutcome?.toDomain(),
)

private fun Esse3Result.toDomain(): AttemptOutcome? {
    val passed = supGraduationFlag == 1
    val isGrade = evaluationModeCode.value == "V"
    val resolvedGrade = grade?.takeIf { isGrade }
    val judgment = judgmentTypeDescription?.trim()?.takeIf { it.isNotEmpty() }
    val date = parseEsse3Date(graduationDate)
    // Drop empty facets (no grade, no judgment, no date, not passed) so the timeline
    // doesn't render hollow rows — some attempts carry only a written or only a final part.
    if (!passed && resolvedGrade == null && judgment == null && date == null) return null
    return AttemptOutcome(
        passed = passed,
        grade = resolvedGrade,
        cumLaude = cumLaudeFlag == 1,
        judgment = judgment,
        date = date,
    )
}

// esito == 1 means prerequisites are satisfied; see PrerequisiteStatus doc for the
// full live-validated semantics.
fun Esse3PrerequisitesCheck.toDomain(): PrerequisiteStatus =
    if (outcome == 1) PrerequisiteStatus.Satisfied else PrerequisiteStatus.NotSatisfied
