package it.attendance100.mybicocca.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Where each take-away booking is picked up. Compass does not say so in a booking, so the app keeps
 * the pickup point chosen when the booking was made, on this device only; bookings made elsewhere
 * (the official app, another phone) have none.
 */
interface MensaPickupPointRepository {
    /** Pickup point names by reservation id. */
    fun observe(): Flow<Map<Int, String>>

    suspend fun remember(reservationId: Int, pickupPoint: String)
}
