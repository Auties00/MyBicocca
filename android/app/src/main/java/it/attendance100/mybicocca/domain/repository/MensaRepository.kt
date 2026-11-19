package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.mensa.MensaCartPricing
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergen
import it.attendance100.mybicocca.domain.model.mensa.MensaDaySchedule
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.domain.model.mensa.MensaDishDetail
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuSummary
import it.attendance100.mybicocca.domain.model.mensa.MensaReservation
import it.attendance100.mybicocca.domain.model.mensa.MensaSession
import it.attendance100.mybicocca.domain.model.mensa.MensaTakeawayService
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Raised when a mensa operation requires a signed-in RistoCloud session. */
class MensaNotLoggedInException : IllegalStateException("Not logged in to Fresh & Natural")

/** Raised when RistoCloud refuses an operation and explains why in [serverMessage] (Italian). */
class MensaServerMessageException(
    val serverMessage: String,
    cause: Throwable? = null,
) : IllegalStateException(serverMessage, cause)

/** Raised when RistoCloud rejects the user's email/password credentials. */
class MensaInvalidCredentialsException(
    message: String? = null,
) : IllegalStateException(message ?: "Invalid RistoCloud credentials")

/**
 * Canteen menus, dish nutrition/allergens, RistoCloud account/wallet, and take-away ordering for
 * Bicocca dining facilities backed by `unibicocca.compasscloud.it`.
 */
interface MensaRepository {

    /** Emits the persisted/hydrated RistoCloud session, or `null` when signed out. */
    fun observeSession(): Flow<MensaSession?>

    /** Whether a persisted RistoCloud token is currently available. */
    suspend fun isLoggedIn(): Boolean

    /** Signs in via email & password, fetches profile & wallet balance, and persists the session. */
    suspend fun login(email: String, password: String): MensaSession

    /** Clears the persisted RistoCloud session. */
    suspend fun logout()

    /** Refreshes the signed-in user's profile and wallet balance. */
    suspend fun refreshAccountInfo(): MensaSession

    /** Updates the display name and phone number on the account profile. */
    suspend fun updateProfile(name: String, phone: String?): MensaSession

    /** Changes the account e-mail address. */
    suspend fun changeEmail(newEmail: String): MensaSession

    /** Changes the account password; [newPassword] is also sent as its own confirmation. */
    suspend fun changePassword(oldPassword: String, newPassword: String)

    /** Whether the signed-in account can be deleted right now (the operator runs checks first). */
    suspend fun canDeleteAccount(): Boolean

    /**
     * Files a request to delete the account. The operator completes it by hand and sends a
     * confirmation mail, so the session stays valid; returns the operator's confirmation text.
     */
    suspend fun requestAccountDeletion(): String?

    /** Lists the active Bicocca canteen menus and their active days. */
    suspend fun getMenus(): List<MensaMenuSummary>

    /** Loads the scheduled daily dishes (grouped by meal type and course) for [menuId]. */
    suspend fun getMenuSchedule(menuId: Int): Map<LocalDate, MensaDaySchedule>

    /** Loads every dish scheduled on any active menu, deduplicated by dish id. */
    suspend fun getAllUniversityDishes(): List<MensaDish>

    /** Lists the allergens the canteen operator declares on its dishes. */
    suspend fun getAllergens(): List<MensaAllergen>

    /** Loads recipe ingredients, EU allergens, nutrition table, and GDA percentages for [dishId]. */
    suspend fun getDishDetail(dishId: Int): MensaDishDetail

    /** Lists bookable take-away services, pickup locations, and turns per date. */
    suspend fun getTakeawayServices(): List<MensaTakeawayService>

    /** Toggles favorite state for [serviceId]. */
    suspend fun toggleFavoriteService(serviceId: Int, isCurrentlyFavorite: Boolean)

    /** Calculates live cart pricing and carbon footprint for [dishesQty]. */
    suspend fun calculateCartPricing(
        serviceId: Int,
        ageRangeId: Int?,
        dishesQty: Map<Int, Int>,
    ): MensaCartPricing

    /** Submits a take-away order and returns `(reservationId, reservationCode)`. */
    suspend fun submitTakeawayOrder(
        serviceId: Int,
        menuId: Int,
        mealTypeId: Int,
        turnId: Int,
        date: LocalDate,
        placeId: Int?,
        dishesQty: Map<Int, Int>,
        userNote: String?,
    ): Pair<Int?, String?>

    /** Lists active and historical take-away orders. */
    suspend fun getTakeawayReservations(): List<MensaReservation>

    /** Cancels an active deletable take-away order by [reservationId]. */
    suspend fun cancelTakeawayReservation(reservationId: Int, motivation: String? = null)
}
