package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.demo.DemoClock
import it.attendance100.mybicocca.demo.DemoMensa
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergen
import it.attendance100.mybicocca.domain.model.mensa.MensaCartPricing
import it.attendance100.mybicocca.domain.model.mensa.MensaDaySchedule
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.domain.model.mensa.MensaDishDetail
import it.attendance100.mybicocca.domain.model.mensa.MensaGdaItem
import it.attendance100.mybicocca.domain.model.mensa.MensaIngredient
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuSummary
import it.attendance100.mybicocca.domain.model.mensa.MensaNutritionRow
import it.attendance100.mybicocca.domain.model.mensa.MensaPickupLocation
import it.attendance100.mybicocca.domain.model.mensa.MensaReservation
import it.attendance100.mybicocca.domain.model.mensa.MensaSession
import it.attendance100.mybicocca.domain.model.mensa.MensaTakeawayDayMealType
import it.attendance100.mybicocca.domain.model.mensa.MensaTakeawayService
import it.attendance100.mybicocca.domain.model.mensa.MensaTimeSlot
import it.attendance100.mybicocca.domain.repository.MensaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** The invented canteen week of [DemoMensa], with a signed-in account and one booked order. */
@Singleton
class DemoMensaRepository @Inject constructor() : MensaRepository {

    private val today: LocalDate = DemoClock.now.toLocalDate()
    private val session = MutableStateFlow<MensaSession?>(DemoMensa.session)
    private val reservations = MutableStateFlow(listOf(bookedOrder()))

    override fun observeSession(): Flow<MensaSession?> = session.asStateFlow()

    override suspend fun isLoggedIn(): Boolean = session.value != null

    override suspend fun login(email: String, password: String): MensaSession =
        DemoMensa.session.copy(email = email.trim()).also { session.value = it }

    override suspend fun logout() {
        session.value = null
    }

    override suspend fun refreshAccountInfo(): MensaSession = session.value ?: DemoMensa.session

    override suspend fun updateProfile(name: String, phone: String?): MensaSession =
        (session.value ?: DemoMensa.session).copy(userName = name, phone = phone).also { session.value = it }

    override suspend fun changeEmail(newEmail: String): MensaSession =
        (session.value ?: DemoMensa.session).copy(email = newEmail).also { session.value = it }

    override suspend fun changePassword(oldPassword: String, newPassword: String) = Unit

    override suspend fun canDeleteAccount(): Boolean = true

    override suspend fun requestAccountDeletion(): String? = null

    override suspend fun getMenus(): List<MensaMenuSummary> = DemoMensa.menus

    override suspend fun getMenuSchedule(menuId: Int): Map<LocalDate, MensaDaySchedule> = DemoMensa.schedule(menuId)

    override suspend fun getAllUniversityDishes(): List<MensaDish> = DemoMensa.allDishes

    override suspend fun getAllergens(): List<MensaAllergen> = DemoMensa.allergens

    override suspend fun getDishDetail(dishId: Int): MensaDishDetail {
        val dish = DemoMensa.allDishes.first { it.id == dishId }
        return MensaDishDetail(
            id = dish.id,
            name = dish.name,
            description = null,
            imageUrl = null,
            netQuantityGrams = 320.0,
            ingredients = listOf(
                MensaIngredient(1, "Ingrediente principale", "180 g"),
                MensaIngredient(2, "Olio extravergine di oliva", "8 g"),
                MensaIngredient(3, "Sale", "2 g"),
            ),
            majorAllergens = dish.majorAllergens,
            traceAllergens = dish.traceAllergens,
            displayNutrition = true,
            nutritionSource = "Valori calcolati",
            nutritionRows = listOf(
                MensaNutritionRow("Energia", "kcal", 420.0, 1757.0, 131.0, 549.0, null),
                MensaNutritionRow("Proteine", "g", 14.0, null, 4.4, null, null),
                MensaNutritionRow("Carboidrati", "g", 68.0, null, 21.0, null, null),
                MensaNutritionRow("Grassi", "g", 9.0, null, 2.8, null, null),
            ),
            gdaItems = listOf(
                MensaGdaItem("Energia", "420 kcal", 21.0, null),
                MensaGdaItem("Grassi", "9 g", 13.0, null),
            ),
            dietaryBadges = dish.dietaryBadges,
        )
    }

    override suspend fun getTakeawayServices(): List<MensaTakeawayService> = listOf(
        MensaTakeawayService(
            id = 279,
            name = "Prenotazione ristorazione base",
            menuId = 400,
            menuName = "Menù Bicocca",
            ageRangeId = 5,
            viewPrices = false,
            walletPayment = false,
            userRequestsEnabled = true,
            infoText = null,
            descriptionText = null,
            maxDaysAhead = 5,
            isFavorite = true,
            pickupLocations = listOf(
                MensaPickupLocation(141, "Mensa U12", "Ristorazione base"),
                MensaPickupLocation(146, "Mensa U6 -1", "Ristorazione base"),
            ),
            scheduleByDate = DemoMensa.serviceDays().associateWith {
                listOf(
                    MensaTakeawayDayMealType(
                        id = 3,
                        name = "Pranzo",
                        timeSlots = listOf(
                            MensaTimeSlot(1116, "Pranzo\n11:45 - 14:30", true, 6, 60, 10.0),
                        ),
                    ),
                )
            },
        ),
    )

    override suspend fun toggleFavoriteService(serviceId: Int, isCurrentlyFavorite: Boolean) = Unit

    override suspend fun calculateCartPricing(
        serviceId: Int,
        ageRangeId: Int?,
        dishesQty: Map<Int, Int>,
    ): MensaCartPricing = MensaCartPricing("€", 0.0, 0.0, 0.0, 0.0, 0.0, null, null, null)

    override suspend fun submitTakeawayOrder(
        serviceId: Int,
        menuId: Int,
        mealTypeId: Int,
        turnId: Int,
        date: LocalDate,
        placeId: Int?,
        dishesQty: Map<Int, Int>,
        userNote: String?,
    ): Pair<Int?, String?> = 1202020 to "2026-GI111202020"

    override suspend fun getTakeawayReservations(): List<MensaReservation> = reservations.value

    override suspend fun cancelTakeawayReservation(reservationId: Int, motivation: String?) {
        reservations.value = reservations.value.filterNot { it.id == reservationId }
    }

    private fun bookedOrder(): MensaReservation {
        val date = DemoMensa.serviceDays().getOrElse(1) { today }
        return MensaReservation(
            id = 1201989,
            code = "2026-VE111201989",
            date = date,
            rawDate = date.toString(),
            createdAt = null,
            deliveryAddress = "",
            menuName = "Menù Bicocca",
            mealTypeName = "Pranzo",
            canteenName = "Prenotazione ristorazione base",
            shiftName = "Pranzo (11:45 - 14:30)",
            confirmed = true,
            deletable = true,
            active = true,
            provided = false,
            statusDescription = "confermata",
            statusColorHex = "#66bb6a",
            dishes = DemoMensa.allDishes.filter { it.id in listOf(1001, 1101, 1201, 1301) },
            barcodeValue = "P-1201989",
            cfpFinalValue = null,
            cfpUnit = null,
        )
    }
}
