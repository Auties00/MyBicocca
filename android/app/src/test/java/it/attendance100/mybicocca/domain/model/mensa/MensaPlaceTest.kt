package it.attendance100.mybicocca.domain.model.mensa

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class MensaPlaceTest {

    private val u12 = MensaPickupLocation(141, "Mensa U12", "Ristorazione base")
    private val u6 = MensaPickupLocation(146, "Mensa U6 -1 (solo pranzo)", "Ristorazione base")

    private fun service(places: List<MensaPickupLocation>) = MensaTakeawayService(
        id = 279, name = "Ristorazione base", menuId = 400, menuName = "Menù Bicocca", ageRangeId = 5,
        viewPrices = false, walletPayment = false, userRequestsEnabled = true, infoText = null,
        descriptionText = null, maxDaysAhead = 5, isFavorite = false, pickupLocations = places,
        scheduleByDate = emptyMap<LocalDate, List<MensaTakeawayDayMealType>>(),
    )

    @Test
    fun `only the canteens have a menu to choose`() {
        assertThat(MensaPlace.CANTEEN_U12.hasMenuChoice).isTrue()
        assertThat(MensaPlace.CANTEEN_U6.hasMenuChoice).isTrue()
        assertThat(MensaPlace.BAR_U3.hasMenuChoice).isFalse()
        assertThat(MensaPlace.BAR_U3.menuIds).containsExactly(MensaMenuIds.BAR_U3)
        assertThat(MensaPlace.BAR_U12.menuIds).containsExactly(MensaMenuIds.BAR_U12)
        assertThat(MensaPlace.BAR_U6.menuIds).containsExactly(MensaMenuIds.BAR_U6)
    }

    @Test
    fun `places are named after what and where they are`() {
        assertThat(MensaPlace.CANTEEN_U12.displayName).isEqualTo("Mensa U12")
        assertThat(MensaPlace.CANTEEN_U6.displayName).isEqualTo("Mensa U6")
        assertThat(MensaPlace.BAR_U3.displayName).isEqualTo("Bar U3")
    }

    @Test
    fun `the lunch-only places keep only the first meal`() {
        val meals = listOf("pranzo" to 1, "cena" to 2)
        assertThat(MensaPlace.CANTEEN_U6.mealsOf(meals, { it.second }).map { it.first }).containsExactly("pranzo")
        assertThat(MensaPlace.CANTEEN_U12.mealsOf(meals, { it.second }).map { it.first }).containsExactly("pranzo", "cena").inOrder()
        assertThat(MensaPlace.BAR_U6.mealsOf(meals, { it.second }).map { it.first }).containsExactly("pranzo")
    }

    @Test
    fun `a pickup place the operator marks as lunch only closes the day after lunch`() {
        assertThat(u6.lunchOnly).isTrue()
        assertThat(u12.lunchOnly).isFalse()
        val meals = listOf("pranzo" to 1, "cena" to 2)
        assertThat(MensaPlace.CANTEEN_U12.mealsOf(meals, { it.second }, u6).map { it.first }).containsExactly("pranzo")
    }

    @Test
    fun `the pickup place of a canteen is found by its building`() {
        val service = service(listOf(u12, u6))
        assertThat(MensaPlace.CANTEEN_U12.pickupLocation(service)).isEqualTo(u12)
        assertThat(MensaPlace.CANTEEN_U6.pickupLocation(service)).isEqualTo(u6)
        assertThat(MensaPlace.BAR_U3.pickupLocation(service)).isNull()
        assertThat(MensaPlace.CANTEEN_U12.pickupLocation(null)).isNull()
    }

    @Test
    fun `a time slot shows its time without the operator's line breaks and meal names`() {
        fun slot(name: String) = MensaTimeSlot(1, name, true, null, null, null)
        assertThat(slot("Pranzo\n11:45 - 14:30").timeLabel).isEqualTo("11:45 - 14:30")
        assertThat(slot("\n19:00 - 21:00").timeLabel).isEqualTo("19:00 - 21:00")
        assertThat(slot("\n12:00 - ").timeLabel).isEqualTo("12:00")
        assertThat(slot("\n12:30 - 13:00").timeLabel).isEqualTo("12:30 - 13:00")
    }
}
