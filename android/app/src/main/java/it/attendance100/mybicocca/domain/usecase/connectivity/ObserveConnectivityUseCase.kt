package it.attendance100.mybicocca.domain.usecase.connectivity

import it.attendance100.mybicocca.domain.repository.ConnectivityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveConnectivityUseCase @Inject constructor(
    private val connectivityRepository: ConnectivityRepository,
) {
    operator fun invoke(): Flow<Boolean> = connectivityRepository.observe()
}
