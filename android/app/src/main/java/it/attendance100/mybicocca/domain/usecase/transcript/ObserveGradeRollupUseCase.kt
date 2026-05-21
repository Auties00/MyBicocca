package it.attendance100.mybicocca.domain.usecase.transcript

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.transcript.GradeRollup
import it.attendance100.mybicocca.domain.repository.TranscriptRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveGradeRollupUseCase @Inject constructor(
    private val repository: TranscriptRepository,
) {
    operator fun invoke(careerId: CareerId): Flow<Loadable<GradeRollup>> =
        repository.observeGradeRollup(careerId)
}
