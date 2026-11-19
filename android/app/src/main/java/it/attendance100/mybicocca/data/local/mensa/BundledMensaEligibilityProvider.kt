package it.attendance100.mybicocca.data.local.mensa

import it.attendance100.mybicocca.domain.model.mensa.MensaEligibility
import it.attendance100.mybicocca.domain.model.mensa.MensaMealRule
import it.attendance100.mybicocca.domain.model.mensa.MensaMealRuleId
import it.attendance100.mybicocca.domain.repository.MensaEligibilityProvider
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Combinations and prices copied from the take-away info text published by the canteen
 * operator, since the live text needs a signed-in account and is Italian only.
 */
@Singleton
class BundledMensaEligibilityProvider @Inject constructor() : MensaEligibilityProvider {

    override suspend fun getEligibility(): MensaEligibility = ELIGIBILITY

    private companion object {
        val ELIGIBILITY = MensaEligibility(
            lastVerified = LocalDate.of(2026, 11, 18),
            rules = listOf(
                MensaMealRule(MensaMealRuleId.FULL_MENU, 660),
                MensaMealRule(MensaMealRuleId.REDUCED_MENU, 550),
                MensaMealRule(MensaMealRuleId.SALAD_MENU, 660),
                MensaMealRule(MensaMealRuleId.SINGLE_DISH_MENU, 660),
                MensaMealRule(MensaMealRuleId.LUNCH_BOX_SANDWICH, 750),
                MensaMealRule(MensaMealRuleId.LUNCH_BOX_PIZZA, 800),
            ),
        )
    }
}
