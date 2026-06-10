package it.attendance100.mybicocca.data.mapper.transcript

import it.attendance100.mybicocca.data.mapper.common.parseEsse3Date
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PrerequisitesCheck
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Result
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptTest
import it.attendance100.mybicocca.domain.model.transcript.AttemptOutcome
import it.attendance100.mybicocca.domain.model.transcript.CourseAttempt
import it.attendance100.mybicocca.domain.model.transcript.PrerequisiteStatus

/**
 * Maps an Esse3 record-book test ("prova") to a domain course attempt, decoding its
 * final, written and partial outcome facets independently.
 */
fun Esse3TranscriptTest.toDomain(): CourseAttempt = CourseAttempt(
    id = activityRegulationId,
    callDate = callDate.parseEsse3Date(),
    sessionDescription = sessionDescription?.trim()?.takeIf { it.isNotEmpty() },
    statusDescription = regulationStatusDescription?.trim()?.takeIf { it.isNotEmpty() },
    finalOutcome = finalOutcome?.toDomain(),
    writtenOutcome = writingOutcome?.toDomain(),
    partialOutcome = partialOutcome?.toDomain(),
)

/**
 * Decodes one outcome facet. The numeric grade counts only under evaluation mode "V"
 * (voto); judgment-mode outcomes keep just the textual judgment. Empty facets (no
 * grade, no judgment, no date, not passed) resolve to null so the attempt timeline
 * renders no hollow rows — some attempts carry only a written or only a final part.
 */
private fun Esse3Result.toDomain(): AttemptOutcome? {
    val passed = supGraduationFlag == 1
    val isGrade = evaluationModeCode.value == "V"
    val resolvedGrade = grade?.takeIf { isGrade }
    val judgment = judgmentTypeDescription?.trim()?.takeIf { it.isNotEmpty() }
    val date = graduationDate.parseEsse3Date()
    if (!passed && resolvedGrade == null && judgment == null && date == null) return null
    return AttemptOutcome(
        passed = passed,
        grade = resolvedGrade,
        cumLaude = cumLaudeFlag == 1,
        judgment = judgment,
        date = date,
    )
}

/**
 * `esito == 1` means prerequisites are satisfied; [PrerequisiteStatus] documents the
 * full live-validated semantics of the check.
 */
fun Esse3PrerequisitesCheck.toDomain(): PrerequisiteStatus =
    if (outcome == 1) PrerequisiteStatus.Satisfied else PrerequisiteStatus.NotSatisfied
