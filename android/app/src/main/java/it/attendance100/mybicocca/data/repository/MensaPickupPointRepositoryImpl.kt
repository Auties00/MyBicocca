package it.attendance100.mybicocca.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import it.attendance100.mybicocca.domain.repository.MensaPickupPointRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Pickup points in the shared `mybicocca_settings` DataStore, as "reservationId|name", never synced. */
@Singleton
class MensaPickupPointRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : MensaPickupPointRepository {

    override fun observe(): Flow<Map<Int, String>> = dataStore.data.map { prefs ->
        prefs[KEY].orEmpty().mapNotNull(::decode).toMap()
    }

    override suspend fun remember(reservationId: Int, pickupPoint: String) {
        dataStore.edit { prefs ->
            val others = prefs[KEY].orEmpty().filterNot { decode(it)?.first == reservationId }
            // Bookings run on reservation ids that only grow, so the newest are the ones to keep.
            prefs[KEY] = (others + "$reservationId|$pickupPoint")
                .sortedByDescending { decode(it)?.first ?: 0 }
                .take(MAX_KEPT)
                .toSet()
        }
    }

    private fun decode(raw: String): Pair<Int, String>? {
        val id = raw.substringBefore('|').toIntOrNull() ?: return null
        return id to raw.substringAfter('|', "")
    }

    private companion object {
        val KEY = stringSetPreferencesKey("mensa_pickup_points")
        const val MAX_KEPT = 60
    }
}
