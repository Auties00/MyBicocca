package it.attendance100.mybicocca.domain.usecase.transcript

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.TranscriptRepository
import javax.inject.Inject

class RefreshTranscriptUseCase @Inject constructor(
    private val repository: TranscriptRepository,
) {
    suspend operator fun invoke(careerId: CareerId, force: Boolean = false) =
        repository.refresh(careerId, force)
}
