package it.attendance100.mybicocca.domain.usecase.studyplan

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.StudyPath
import it.attendance100.mybicocca.domain.repository.StudyPlanRepository
import javax.inject.Inject

/**
 * Loads the student's path configuration (percorso / orientamento / profilo /
 * part-time) plus any selectable alternatives and the compilation-window state, live
 * from Esse3, for the study-plan sub-screen in the registry tab. Returns null when the
 * career has no standard plan; throws on network failure.
 */
class GetStudyPathUseCase @Inject constructor(
    private val repository: StudyPlanRepository,
) {
    suspend operator fun invoke(careerId: CareerId): StudyPath? =
        repository.getStudyPath(careerId)
}
