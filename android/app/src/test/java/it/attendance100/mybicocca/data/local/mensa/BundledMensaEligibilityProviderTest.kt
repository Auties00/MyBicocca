package it.attendance100.mybicocca.data.local.mensa

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.domain.model.mensa.MensaMealRuleId
import kotlinx.coroutines.test.runTest
import org.junit.Test

class BundledMensaEligibilityProviderTest {

    @Test
    fun `lists every combination once with its price`() = runTest {
        val eligibility = BundledMensaEligibilityProvider().getEligibility()

        assertThat(eligibility.rules.map { it.id }).containsExactlyElementsIn(MensaMealRuleId.entries)
        assertThat(eligibility.rules.first { it.id == MensaMealRuleId.REDUCED_MENU }.priceCents).isEqualTo(550)
        assertThat(eligibility.rules.first { it.id == MensaMealRuleId.LUNCH_BOX_PIZZA }.priceCents).isEqualTo(800)
    }
}
