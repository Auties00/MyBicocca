package it.attendance100.mybicocca.domain.usecase.transcript

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.transcript.PrerequisiteStatus
import it.attendance100.mybicocca.domain.repository.TranscriptRepository
import javax.inject.Inject

/**
 * Checks the propedeuticità of a single not-yet-passed libretto activity live on Esse3,
 * used to flag blocked exams in the profile screen. Resolves to Unknown on any failure
 * rather than throwing, so callers never surface a misleading warning.
 */
class GetPrerequisiteStatusUseCase @Inject constructor(
    private val repository: TranscriptRepository,
) {
    suspend operator fun invoke(careerId: CareerId, activityChoiceId: Long): PrerequisiteStatus =
        repository.getPrerequisiteStatus(careerId, activityChoiceId)
}
