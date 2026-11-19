package it.attendance100.mybicocca.domain.model.mensa

import java.time.LocalDate

/** High-level summary of a Bicocca canteen menu returned by `/api/public/v1/menu/get/frontend`. */
data class MensaMenuSummary(
    val id: Int,
    val name: String,
    val shortName: String,
    val description: String,
    val cateringTypeDescription: String,
    val activeDays: List<LocalDate>,
    val mealTypes: List<MensaMealType>,
)

/** Meal period within a menu (e.g. *Pranzo* or *Cena*). */
data class MensaMealType(
    val id: Int,
    val name: String,
    val shortName: String,
    val order: Int,
)

/** Full menu schedule for a single date. */
data class MensaDaySchedule(
    val menuId: Int,
    val date: LocalDate,
    val name: String,
    val active: Boolean,
    val mealTypes: List<MensaDayMealType>,
)

/** Courses available for a specific meal type on a scheduled day. */
data class MensaDayMealType(
    val id: Int,
    val name: String,
    val order: Int,
    val courses: List<MensaCourse>,
)

/** Course section (e.g. *Primi*, *Secondi*, *Contorni*) containing dishes. */
data class MensaCourse(
    val id: Int,
    val name: String,
    val code: String,
    val order: Int,
    val dishes: List<MensaDish>,
)
