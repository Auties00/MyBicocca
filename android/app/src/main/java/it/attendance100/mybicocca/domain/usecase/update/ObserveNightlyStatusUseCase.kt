package it.attendance100.mybicocca.domain.usecase.update

import it.attendance100.mybicocca.domain.model.update.UpdateStatus
import it.attendance100.mybicocca.domain.repository.UpdateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNightlyStatusUseCase @Inject constructor(
    private val repository: UpdateRepository,
) {
    operator fun invoke(): Flow<UpdateStatus> = repository.observeNightlyStatus()
}
