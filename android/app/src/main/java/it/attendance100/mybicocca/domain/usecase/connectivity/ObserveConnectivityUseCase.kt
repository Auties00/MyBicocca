package it.attendance100.mybicocca.domain.usecase.connectivity

import it.attendance100.mybicocca.domain.repository.ConnectivityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams whether the device has validated internet access; feeds the app-wide offline banner
 * and the LocalIsOnline composition local that gates network-dependent actions.
 */
class ObserveConnectivityUseCase @Inject constructor(
    private val connectivityRepository: ConnectivityRepository,
) {
    operator fun invoke(): Flow<Boolean> = connectivityRepository.observe()
}
