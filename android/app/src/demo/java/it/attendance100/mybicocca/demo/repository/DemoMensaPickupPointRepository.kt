package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.domain.repository.MensaPickupPointRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/** In-memory pickup points: the demo student's booking was made in the app, for Mensa U12. */
@Singleton
class DemoMensaPickupPointRepository @Inject constructor() : MensaPickupPointRepository {
    private val points = MutableStateFlow(mapOf(1201989 to "Mensa U12"))

    override fun observe(): Flow<Map<Int, String>> = points.asStateFlow()

    override suspend fun remember(reservationId: Int, pickupPoint: String) {
        points.update { it + (reservationId to pickupPoint) }
    }
}
