package it.attendance100.mybicocca.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import it.attendance100.mybicocca.domain.model.mensa.MensaFavourite
import it.attendance100.mybicocca.domain.model.mensa.MensaFavouriteMatcher
import it.attendance100.mybicocca.domain.model.mensa.MensaFavouriteSettings
import it.attendance100.mybicocca.domain.repository.MensaFavouritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Favourite dishes in the shared `mybicocca_settings` DataStore, never synced. Each is stored as
 * "id|name" so the list reads back without a network call.
 */
@Singleton
class MensaFavouritesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : MensaFavouritesRepository {

    override fun observeFavourites(): Flow<List<MensaFavourite>> = dataStore.data.map { prefs ->
        prefs[FAVOURITES_KEY].orEmpty().mapNotNull(::decode).sortedBy { MensaFavouriteMatcher.normalize(it.name) }
    }

    override fun observeSettings(): Flow<MensaFavouriteSettings> = dataStore.data.map { prefs ->
        MensaFavouriteSettings(
            notify = prefs[NOTIFY_KEY] ?: false,
            lookaheadDays = prefs[LOOKAHEAD_KEY] ?: 0,
        )
    }

    override suspend fun setFavourite(favourite: MensaFavourite, isFavourite: Boolean) {
        dataStore.edit { prefs ->
            val others = prefs[FAVOURITES_KEY].orEmpty().filterNot { decode(it)?.dishId == favourite.dishId }.toSet()
            prefs[FAVOURITES_KEY] = if (isFavourite) others + encode(favourite) else others
        }
    }

    override suspend fun setNotify(notify: Boolean) {
        dataStore.edit { it[NOTIFY_KEY] = notify }
    }

    override suspend fun setLookaheadDays(days: Int) {
        dataStore.edit { it[LOOKAHEAD_KEY] = days.coerceIn(0, MensaFavouriteMatcher.MAX_LOOKAHEAD_DAYS) }
    }

    override suspend fun announcedKeys(): Set<String> = dataStore.data.first()[ANNOUNCED_KEY].orEmpty()

    override suspend fun setAnnouncedKeys(keys: Set<String>) {
        dataStore.edit { it[ANNOUNCED_KEY] = keys }
    }

    private fun encode(favourite: MensaFavourite) = "${favourite.dishId}|${favourite.name}"

    private fun decode(raw: String): MensaFavourite? {
        val id = raw.substringBefore('|').toIntOrNull() ?: return null
        return MensaFavourite(id, raw.substringAfter('|', ""))
    }

    private companion object {
        val FAVOURITES_KEY = stringSetPreferencesKey("mensa_favourite_dishes")
        val NOTIFY_KEY = booleanPreferencesKey("mensa_favourites_notify")
        val LOOKAHEAD_KEY = intPreferencesKey("mensa_favourites_lookahead_days")
        val ANNOUNCED_KEY = stringSetPreferencesKey("mensa_favourites_announced")
    }
}
