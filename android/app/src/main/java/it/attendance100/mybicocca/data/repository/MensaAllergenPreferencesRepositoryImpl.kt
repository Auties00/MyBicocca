package it.attendance100.mybicocca.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenPreferences
import it.attendance100.mybicocca.domain.repository.MensaAllergenPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Persists the allergen selection in the shared `mybicocca_settings` DataStore, never synced. */
@Singleton
class MensaAllergenPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : MensaAllergenPreferencesRepository {

    override fun observe(): Flow<MensaAllergenPreferences> = dataStore.data.map { prefs ->
        MensaAllergenPreferences(
            allergenIds = prefs[ALLERGEN_IDS_KEY].orEmpty().mapNotNullTo(mutableSetOf()) { it.toIntOrNull() },
            hideMatchingDishes = prefs[HIDE_MATCHING_KEY] ?: false,
        )
    }

    override suspend fun setAllergenSelected(allergenId: Int, selected: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[ALLERGEN_IDS_KEY].orEmpty()
            val id = allergenId.toString()
            prefs[ALLERGEN_IDS_KEY] = if (selected) current + id else current - id
        }
    }

    override suspend fun setHideMatchingDishes(hide: Boolean) {
        dataStore.edit { prefs -> prefs[HIDE_MATCHING_KEY] = hide }
    }

    override suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(ALLERGEN_IDS_KEY)
            prefs.remove(HIDE_MATCHING_KEY)
        }
    }

    private companion object {
        val ALLERGEN_IDS_KEY = stringSetPreferencesKey("mensa_allergen_ids")
        val HIDE_MATCHING_KEY = booleanPreferencesKey("mensa_hide_matching_dishes")
    }
}
