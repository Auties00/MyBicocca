package it.attendance100.mybicocca.domain.usecase.update

import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.repository.UpdateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams the "a newer version was just discovered" events the daily background check emits, so
 * the signed-in shell can raise the "new version available" snackbar once per newly-found build.
 */
class ObserveUpdateEventsUseCase @Inject constructor(
    private val repository: UpdateRepository,
) {
    operator fun invoke(): Flow<AppRelease> = repository.newUpdateEvents
}
