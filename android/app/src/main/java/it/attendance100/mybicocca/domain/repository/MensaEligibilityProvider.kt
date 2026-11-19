package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.mensa.MensaEligibility

/**
 * Source of the canteen meal combinations and prices. Kept behind an interface so the data can
 * move from the bundled copy to a remote file without touching the screens.
 */
interface MensaEligibilityProvider {
    suspend fun getEligibility(): MensaEligibility
}
