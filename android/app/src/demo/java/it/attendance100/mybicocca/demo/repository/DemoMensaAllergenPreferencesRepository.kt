package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenPreferences
import it.attendance100.mybicocca.domain.repository.MensaAllergenPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/** The demo student avoids gluten and milk, so the menus show flagged dishes. Kept in memory only. */
@Singleton
class DemoMensaAllergenPreferencesRepository @Inject constructor() : MensaAllergenPreferencesRepository {

    private val state = MutableStateFlow(MensaAllergenPreferences(allergenIds = setOf(1, 7)))

    override fun observe(): Flow<MensaAllergenPreferences> = state.asStateFlow()

    override suspend fun setAllergenSelected(allergenId: Int, selected: Boolean) {
        state.update { it.copy(allergenIds = if (selected) it.allergenIds + allergenId else it.allergenIds - allergenId) }
    }

    override suspend fun setHideMatchingDishes(hide: Boolean) {
        state.update { it.copy(hideMatchingDishes = hide) }
    }

    override suspend fun clear() {
        state.value = MensaAllergenPreferences()
    }
}
