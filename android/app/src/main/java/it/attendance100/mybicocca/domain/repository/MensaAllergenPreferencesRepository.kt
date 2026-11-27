package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenPreferences
import kotlinx.coroutines.flow.Flow

/** Device-local store for the allergens the user wants flagged in the canteen menus. */
interface MensaAllergenPreferencesRepository {
    fun observe(): Flow<MensaAllergenPreferences>
    suspend fun setAllergenSelected(allergenId: Int, selected: Boolean)
    suspend fun setHideMatchingDishes(hide: Boolean)
    suspend fun clear()
}
