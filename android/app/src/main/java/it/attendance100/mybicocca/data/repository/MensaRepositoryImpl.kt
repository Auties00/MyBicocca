package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.local.credentials.CompassAccountStore
import it.attendance100.mybicocca.data.mapper.mensa.toDomain
import it.attendance100.mybicocca.data.remote.compass.api.CompassApi
import it.attendance100.mybicocca.data.remote.compass.dto.CompassBearerAuthAuth
import it.attendance100.mybicocca.data.remote.compass.dto.CompassFavoriteAction
import it.attendance100.mybicocca.data.remote.compass.exception.CompassException
import it.attendance100.mybicocca.di.ApplicationScope
import it.attendance100.mybicocca.domain.model.mensa.MensaCartPricing
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergen
import it.attendance100.mybicocca.domain.model.mensa.MensaDaySchedule
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.domain.model.mensa.MensaDishDetail
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuSummary
import it.attendance100.mybicocca.domain.model.mensa.MensaReservation
import it.attendance100.mybicocca.domain.model.mensa.MensaSession
import it.attendance100.mybicocca.domain.model.mensa.MensaTakeawayService
import it.attendance100.mybicocca.domain.repository.MensaInvalidCredentialsException
import it.attendance100.mybicocca.domain.repository.MensaNotLoggedInException
import it.attendance100.mybicocca.domain.repository.MensaRepository
import it.attendance100.mybicocca.domain.repository.MensaServerMessageException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generated-SDK-backed implementation of [MensaRepository] talking to `unibicocca.compasscloud.it`.
 *
 * Public canteen menus and dish nutrition/allergens work anonymously without an account. Wallet
 * balance, take-away ordering, and reservation history authenticate via the persistent RistoCloud
 * Bearer token stored encrypted in [CompassAccountStore].
 */
@Singleton
class MensaRepositoryImpl @Inject constructor(
    private val api: CompassApi,
    private val accountStore: CompassAccountStore,
    @ApplicationScope appScope: CoroutineScope,
) : MensaRepository {

    private val sessionFlow = MutableStateFlow<MensaSession?>(null)

    init {
        appScope.launch {
            val email = accountStore.email()
            val token = accountStore.authToken()
            if (!email.isNullOrBlank() && !token.isNullOrBlank()) {
                sessionFlow.value = MensaSession(
                    email = email,
                    userName = accountStore.userName(),
                    barcode = accountStore.barcode(),
                    walletBalance = null,
                )
            }
        }
    }

    override fun observeSession(): Flow<MensaSession?> = sessionFlow.asStateFlow()

    override suspend fun isLoggedIn(): Boolean =
        !accountStore.authToken().isNullOrBlank() && !accountStore.email().isNullOrBlank()

    override suspend fun login(email: String, password: String): MensaSession {
        val trimmedEmail = email.trim()
        val result = api.account.loginBasic(username = trimmedEmail, password = password)
        val token = result.token?.takeIf { it.isNotBlank() }
            ?: throw MensaInvalidCredentialsException()
        val auth = CompassBearerAuthAuth(token)
        val profile = api.account.getUserProfile(auth)
        val balance = runCatching { api.wallet.getWalletBalance(auth).saldo }.getOrNull()
            ?: profile.canteen?.wallet?.balance
        val displayName = profile.name?.trim()?.takeIf { it.isNotEmpty() }
        val barcode = profile.barcode?.trim()?.takeIf { it.isNotEmpty() }
            ?: profile.id?.let { "U-$it" }
        val resolvedEmail = profile.email?.trim()?.takeIf { it.isNotEmpty() } ?: trimmedEmail
        accountStore.setSession(
            email = resolvedEmail,
            authToken = token,
            userName = displayName,
            barcode = barcode,
        )
        return MensaSession(
            email = resolvedEmail,
            userName = displayName,
            barcode = barcode,
            walletBalance = balance,
        ).also { sessionFlow.value = it }
    }

    override suspend fun logout() {
        accountStore.clearSession()
        sessionFlow.value = null
    }

    override suspend fun refreshAccountInfo(): MensaSession = withBearerAuth { auth ->
        val storedEmail = accountStore.email().orEmpty()
        val profile = api.account.getUserProfile(auth)
        val balance = runCatching { api.wallet.getWalletBalance(auth).saldo }.getOrNull()
            ?: profile.canteen?.wallet?.balance
        val displayName = profile.name?.trim()?.takeIf { it.isNotEmpty() }
        val barcode = profile.barcode?.trim()?.takeIf { it.isNotEmpty() }
            ?: profile.id?.let { "U-$it" }
        val resolvedEmail = profile.email?.trim()?.takeIf { it.isNotEmpty() } ?: storedEmail
        accountStore.updateProfile(displayName, barcode)
        MensaSession(
            email = resolvedEmail,
            userName = displayName,
            barcode = barcode,
            walletBalance = balance,
            phone = profile.phoneNumber.toUsablePhone(),
        ).also { sessionFlow.value = it }
    }

    override suspend fun updateProfile(name: String, phone: String?): MensaSession = withBearerAuth { auth ->
        api.account.updateUserDetails(
            name = name.trim(),
            phoneNumber = phone?.trim()?.takeIf { it.isNotEmpty() },
            auth = auth,
        )
        refreshAccountInfo()
    }

    override suspend fun changeEmail(newEmail: String): MensaSession = withBearerAuth { auth ->
        val email = newEmail.trim()
        api.account.updateEmail(email = email, auth = auth)
        accountStore.updateEmail(email)
        refreshAccountInfo()
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String) {
        withBearerAuth { auth ->
            api.account.updatePassword(
                oldPassword = oldPassword,
                newPassword = newPassword,
                repeatPassword = newPassword,
                auth = auth,
            )
        }
    }

    override suspend fun canDeleteAccount(): Boolean = withBearerAuth { auth ->
        api.account.canDeleteAccount(auth).deletable
    }

    override suspend fun requestAccountDeletion(): String? = withBearerAuth { auth ->
        api.account.requestAccountDeletion(auth).message?.trim()?.takeIf { it.isNotEmpty() }
    }

    override suspend fun getMenus(): List<MensaMenuSummary> =
        api.menu.getMenusFrontend().menus
            .filter { (it.visible ?: true) && (it.active ?: true) }
            .sortedBy { it.order ?: Int.MAX_VALUE }
            .map { it.toDomain() }

    override suspend fun getMenuSchedule(menuId: Int): Map<LocalDate, MensaDaySchedule> =
        api.menu.getMenuDishes(menuId).dates.entries
            .mapNotNull { (key, day) -> day.toDomain(key)?.let { it.date to it } }
            .sortedBy { it.first }
            .toMap()

    override suspend fun getAllUniversityDishes(): List<MensaDish> = coroutineScope {
        getMenus()
            .map { menu -> async { getMenuSchedule(menu.id) } }
            .awaitAll()
            .flatMap { schedule -> schedule.values }
            .flatMap { day -> day.mealTypes }
            .flatMap { mealType -> mealType.courses }
            .flatMap { course -> course.dishes }
            .distinctBy { it.id }
    }

    override suspend fun getAllergens(): List<MensaAllergen> =
        api.contract.getRequiredAllergens().requiredAllergens
            .sortedBy { it.id }
            .map { it.toDomain() }

    override suspend fun getDishDetail(dishId: Int): MensaDishDetail =
        api.menu.getDishDetails(dishId).toDomain(dishId)

    override suspend fun getTakeawayServices(): List<MensaTakeawayService> = withBearerAuth { auth ->
        api.takeaway.getTakeawayServices(auth).services.map { it.toDomain() }
    }

    override suspend fun toggleFavoriteService(serviceId: Int, isCurrentlyFavorite: Boolean) {
        withBearerAuth { auth ->
            val action = if (isCurrentlyFavorite) CompassFavoriteAction.Remove else CompassFavoriteAction.Add
            val res = api.takeaway.editFavoriteService(serviceId, action, auth)
            if (res.status != null && !res.status.equals("success", ignoreCase = true)) {
                error(res.message ?: "Favorite update failed (${res.status})")
            }
        }
    }

    override suspend fun calculateCartPricing(
        serviceId: Int,
        ageRangeId: Int?,
        dishesQty: Map<Int, Int>,
    ): MensaCartPricing = withBearerAuth { auth ->
        val filtered = dishesQty.filterValues { it > 0 }
        val jsonMap = encodeDishQtyMap(filtered)
        val price = api.takeaway.calculateTemporaryCost(serviceId, jsonMap, auth)
        val cfp = ageRangeId?.let { id ->
            runCatching { api.takeaway.calculateCfpTotal(id, jsonMap, auth) }.getOrNull()
        }
        price.toDomain(cfp)
    }

    override suspend fun submitTakeawayOrder(
        serviceId: Int,
        menuId: Int,
        mealTypeId: Int,
        turnId: Int,
        date: LocalDate,
        placeId: Int?,
        dishesQty: Map<Int, Int>,
        userNote: String?,
    ): Pair<Int?, String?> = withBearerAuth { auth ->
        val filtered = dishesQty.filterValues { it > 0 }
        require(filtered.isNotEmpty()) { "At least one dish is required" }
        val result = api.takeaway.submitTakeawayOrder(
            turnId = turnId,
            reservationDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
            menuId = menuId,
            dishesQtyJson = encodeDishQtyMap(filtered),
            mealTypeId = mealTypeId,
            serviceId = serviceId,
            placeId = placeId,
            userNote = userNote?.trim()?.takeIf { it.isNotEmpty() },
            auth = auth,
        )
        if (!result.status.equals("success", ignoreCase = true) && result.reservationId == null) {
            error(result.messaggio ?: "Order submission failed (${result.status})")
        }
        result.reservationId to result.reservationCode
    }

    override suspend fun getTakeawayReservations(): List<MensaReservation> = withBearerAuth { auth ->
        api.takeaway.getTakeawayHistory(auth).reservations.map { it.toDomain() }
    }

    override suspend fun cancelTakeawayReservation(reservationId: Int, motivation: String?) {
        withBearerAuth { auth ->
            val res = api.takeaway.cancelTakeawayOrder(
                reservationId = reservationId,
                cancellationMotivation = motivation?.trim()?.takeIf { it.isNotEmpty() },
                auth = auth,
            )
            if (res.status != null && !res.status.equals("success", ignoreCase = true)) {
                error(res.message ?: "Cancellation failed (${res.status})")
            }
        }
    }

    private suspend inline fun <T> withBearerAuth(block: (CompassBearerAuthAuth) -> T): T {
        val token = accountStore.authToken()?.takeIf { it.isNotBlank() }
            ?: throw MensaNotLoggedInException()
        return try {
            block(CompassBearerAuthAuth(token))
        } catch (cause: CompassException) {
            val reason = cause.serverMessage?.trim()?.takeIf { it.isNotEmpty() }
            // The booking endpoint answers 401 with a message when a dish is no longer on the menu,
            // which is not about the session: only a bare 401 or 403 means it has expired.
            if ((cause.statusCode == 401 || cause.statusCode == 403) && reason == null) {
                logout()
                throw MensaNotLoggedInException()
            }
            throw if (reason != null) MensaServerMessageException(reason, cause) else cause
        }
    }

    private fun String?.toUsablePhone(): String? =
        this?.trim()?.takeIf { it.isNotEmpty() && !it.equals("Non disponibile", ignoreCase = true) }

    private fun encodeDishQtyMap(dishesQty: Map<Int, Int>): String = buildJsonObject {
        dishesQty.forEach { (id, qty) -> put(id.toString(), qty) }
    }.toString()
}
