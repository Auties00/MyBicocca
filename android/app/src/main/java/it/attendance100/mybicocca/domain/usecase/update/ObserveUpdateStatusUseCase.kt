package it.attendance100.mybicocca.domain.usecase.update

import it.attendance100.mybicocca.domain.model.update.UpdateStatus
import it.attendance100.mybicocca.domain.repository.UpdateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams the persisted update status that drives the "Check for Updates" tile in the About modal. */
class ObserveUpdateStatusUseCase @Inject constructor(
    private val repository: UpdateRepository,
) {
    operator fun invoke(): Flow<UpdateStatus> = repository.observeStatus()
}
