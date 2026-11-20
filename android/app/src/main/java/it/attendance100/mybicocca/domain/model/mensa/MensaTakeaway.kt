package it.attendance100.mybicocca.domain.model.mensa

import java.time.LocalDate

/** Take-away ordering service configuration with pickup points and per-date meal turns. */
data class MensaTakeawayService(
    val id: Int,
    val name: String,
    val menuId: Int,
    val menuName: String,
    val ageRangeId: Int?,
    val viewPrices: Boolean,
    val walletPayment: Boolean,
    val userRequestsEnabled: Boolean,
    val infoText: String?,
    val descriptionText: String?,
    val maxDaysAhead: Int,
    val isFavorite: Boolean,
    val pickupLocations: List<MensaPickupLocation>,
    val scheduleByDate: Map<LocalDate, List<MensaTakeawayDayMealType>>,
)

/** Physical pickup counter or dining area selectable for take-away orders. */
data class MensaPickupLocation(
    val id: Int,
    val name: String,
    val groupName: String,
)

/** Meal type and its pickup time slots on a specific takeaway date. */
data class MensaTakeawayDayMealType(
    val id: Int,
    val name: String,
    val timeSlots: List<MensaTimeSlot>,
)

/** Single bookable pickup time slot with current occupancy statistics. */
data class MensaTimeSlot(
    val id: Int,
    val name: String,
    val bookable: Boolean,
    val reservationsCount: Int?,
    val affluencyCapacity: Int?,
    val percentage: Double?,
)

/** Server-calculated cart cost and carbon footprint summary. */
data class MensaCartPricing(
    val currencySymbol: String,
    val total: Double,
    val fixedCost: Double,
    val extraCost: Double,
    val combinationCost: Double,
    val reservationCost: Double,
    val cfpSum: String?,
    val cfpUnit: String?,
    val cfpEquivalentPhrase: String?,
)

/** Submitted take-away reservation from `/api/public/v1/reservations/takeaway/history`. */
data class MensaReservation(
    val id: Int,
    val code: String,
    val date: LocalDate?,
    val rawDate: String,
    val createdAt: String?,
    val deliveryAddress: String?,
    val menuName: String?,
    val mealTypeName: String?,
    val canteenName: String?,
    val shiftName: String?,
    val confirmed: Boolean,
    val deletable: Boolean,
    val active: Boolean,
    val provided: Boolean,
    val statusDescription: String,
    val statusColorHex: String?,
    val dishes: List<MensaDish>,
    val barcodeValue: String,
    val cfpFinalValue: String?,
    val cfpUnit: String?,
)

private val TimeRange = Regex("""\d{1,2}:\d{2}(\s*-\s*\d{1,2}:\d{2})?""")

/**
 * The slot's time as the operator wrote it, without the line breaks and meal names it adds
 * ("Pranzo\n11:45 - 14:30" is "11:45 - 14:30"; an open-ended "12:00 -" is "12:00").
 */
val MensaTimeSlot.timeLabel: String
    get() = TimeRange.find(name)?.value?.replace(Regex("\\s+"), " ") ?: name.replace("\n", " ").trim()
