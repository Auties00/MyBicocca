package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergen
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenMatch
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenPreferences
import it.attendance100.mybicocca.domain.model.mensa.MensaCartPricing
import it.attendance100.mybicocca.domain.model.mensa.MensaCourse
import it.attendance100.mybicocca.domain.model.mensa.MensaDayMealType
import it.attendance100.mybicocca.domain.model.mensa.MensaDaySchedule
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.domain.model.mensa.MensaDishDetail
import it.attendance100.mybicocca.domain.model.mensa.MensaEligibility
import it.attendance100.mybicocca.domain.model.mensa.MensaMealRule
import it.attendance100.mybicocca.domain.model.mensa.MensaMealRuleId
import it.attendance100.mybicocca.domain.model.mensa.MensaMealType
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuSummary
import it.attendance100.mybicocca.domain.model.mensa.MensaPickupLocation
import it.attendance100.mybicocca.domain.model.mensa.MensaPlace
import it.attendance100.mybicocca.domain.model.mensa.MensaReservation
import it.attendance100.mybicocca.domain.model.mensa.MensaSession
import it.attendance100.mybicocca.domain.model.mensa.MensaTakeawayDayMealType
import it.attendance100.mybicocca.domain.model.mensa.MensaTakeawayService
import it.attendance100.mybicocca.domain.model.mensa.MensaTimeSlot
import it.attendance100.mybicocca.domain.model.mensa.allergenMatch
import it.attendance100.mybicocca.domain.repository.MensaAllergenPreferencesRepository
import it.attendance100.mybicocca.domain.repository.MensaEligibilityProvider
import it.attendance100.mybicocca.domain.repository.MensaServerMessageException
import it.attendance100.mybicocca.domain.repository.MensaRepository
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaDeletionCheck
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaDishDetailState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaOrderAvailability
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaSubPage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import it.attendance100.mybicocca.domain.model.mensa.MensaFavourite
import it.attendance100.mybicocca.domain.model.mensa.MensaFavouriteSettings
import it.attendance100.mybicocca.domain.repository.MensaFavouritesRepository
import it.attendance100.mybicocca.data.mensa.MensaFavouritesScheduler
import it.attendance100.mybicocca.domain.repository.MensaPickupPointRepository

@OptIn(ExperimentalCoroutinesApi::class)
class MensaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionFlow = MutableStateFlow<MensaSession?>(null)
    private val repository: MensaRepository = mockk(relaxed = true)
    private val preferencesFlow = MutableStateFlow(MensaAllergenPreferences())
    private val preferencesRepository: MensaAllergenPreferencesRepository = mockk(relaxed = true)
    private val eligibilityProvider: MensaEligibilityProvider = mockk()
    private val favouritesFlow = MutableStateFlow<List<MensaFavourite>>(emptyList())
    private val favouritesRepository: MensaFavouritesRepository = mockk(relaxed = true)
    private val favouritesScheduler: MensaFavouritesScheduler = mockk(relaxed = true)
    private val pickupPointRepository: MensaPickupPointRepository = mockk(relaxed = true)
    private val eligibility = MensaEligibility(LocalDate.of(2026, 11, 18), listOf(MensaMealRule(MensaMealRuleId.FULL_MENU, 660)))

    private val today: LocalDate = LocalDate.now(AppClock.clock)

    private val sampleDish = MensaDish(
        id = 101015,
        name = "Pasta al pomodoro",
        shortName = "Pasta pomodoro",
        courseId = 1,
        courseName = "Primi",
        mealTypeId = 3,
        mealTypeName = "Pranzo",
        imageUrl = null,
        thumbUrl = null,
        majorAllergens = emptyList(),
        traceAllergens = emptyList(),
        caloricLabel = "Leggero",
        caloricColorHex = "#4CAF50",
        dietaryBadges = emptyList(),
    )

    private val menus = listOf(
        MensaMenuSummary(
            id = 400,
            name = "Menù Bicocca",
            shortName = "U6",
            description = "Menù giornaliero",
            cateringTypeDescription = "Mensa",
            activeDays = listOf(today),
            mealTypes = listOf(MensaMealType(3, "Pranzo", "Pr", 1)),
        ),
        MensaMenuSummary(
            id = 701,
            name = "Ristorazione complementare U3",
            shortName = "U3",
            description = "Mensa U3",
            cateringTypeDescription = "Bar",
            activeDays = listOf(today),
            mealTypes = listOf(MensaMealType(3, "Pranzo", "Pr", 1)),
        ),
        MensaMenuSummary(
            id = 598,
            name = "Menu Bicocca Senza Glutine",
            shortName = "Senza Glutine",
            description = "",
            cateringTypeDescription = "Mensa",
            activeDays = listOf(today),
            mealTypes = listOf(MensaMealType(3, "Pranzo", "Pr", 1)),
        ),
    )

    private fun daySchedule(menuId: Int) = mapOf(
        today to MensaDaySchedule(
            menuId = menuId,
            date = today,
            name = "Menu $menuId",
            active = true,
            mealTypes = listOf(
                MensaDayMealType(
                    id = 3,
                    name = "Pranzo",
                    order = 1,
                    courses = listOf(
                        MensaCourse(1, "Primi", "PR", 1, listOf(sampleDish)),
                    ),
                ),
            ),
        ),
    )

    private val sampleService = MensaTakeawayService(
        id = 435,
        name = "Take-Away Bicocca",
        menuId = 400,
        menuName = "Menù Bicocca",
        ageRangeId = 5,
        viewPrices = true,
        walletPayment = true,
        userRequestsEnabled = true,
        infoText = null,
        descriptionText = null,
        maxDaysAhead = 7,
        isFavorite = false,
        pickupLocations = listOf(MensaPickupLocation(10, "Sportello Take-Away U6", "Ritiro")),
        scheduleByDate = mapOf(
            today to listOf(
                MensaTakeawayDayMealType(
                    id = 3,
                    name = "Pranzo",
                    timeSlots = listOf(
                        MensaTimeSlot(120, "12:30 - 13:00", bookable = true, reservationsCount = 2, affluencyCapacity = 20, percentage = 10.0),
                    ),
                ),
            ),
        ),
    )

    private val sampleReservation = MensaReservation(
        id = 1201989,
        code = "2026-LU111201989",
        date = today,
        rawDate = today.toString(),
        createdAt = null,
        deliveryAddress = "Sportello Take-Away U6",
        menuName = "Menù Bicocca",
        mealTypeName = "Pranzo",
        canteenName = "Take-Away Bicocca",
        shiftName = "12:30 - 13:00",
        confirmed = true,
        deletable = true,
        active = true,
        provided = false,
        statusDescription = "Confermata",
        statusColorHex = "#4CAF50",
        dishes = listOf(sampleDish),
        barcodeValue = "P-1201989",
        cfpFinalValue = null,
        cfpUnit = null,
    )

    private fun withDinner(menu: MensaMenuSummary) =
        menu.copy(mealTypes = menu.mealTypes + MensaMealType(5, "Cena", "Ce", 2))

    private fun withDinner(schedule: Map<LocalDate, MensaDaySchedule>) = schedule.mapValues { (_, day) ->
        day.copy(mealTypes = day.mealTypes + MensaDayMealType(5, "Cena", 2, day.mealTypes.first().courses))
    }

    private fun createViewModel(
        loggedIn: Boolean = false,
        dinner: Boolean = false,
        reservations: List<MensaReservation> = listOf(sampleReservation),
    ): MensaViewModel {
        every { repository.observeSession() } returns sessionFlow
        coEvery { repository.isLoggedIn() } returns loggedIn
        coEvery { repository.getMenus() } returns if (dinner) menus.map { if (it.id == 400) withDinner(it) else it } else menus
        coEvery { repository.getMenuSchedule(400) } returns if (dinner) withDinner(daySchedule(400)) else daySchedule(400)
        coEvery { repository.getMenuSchedule(701) } returns daySchedule(701)
        coEvery { repository.getMenuSchedule(598) } returns daySchedule(598)
        coEvery { repository.getTakeawayServices() } returns listOf(sampleService)
        coEvery { repository.getTakeawayReservations() } returns reservations
        every { preferencesRepository.observe() } returns preferencesFlow
        coEvery { eligibilityProvider.getEligibility() } returns eligibility
        every { favouritesRepository.observeFavourites() } returns favouritesFlow
        every { favouritesRepository.observeSettings() } returns MutableStateFlow(MensaFavouriteSettings())
        return MensaViewModel(repository, preferencesRepository, eligibilityProvider, favouritesRepository, favouritesScheduler, pickupPointRepository)
    }

    @Test
    fun `initial load populates menus and selects default schedule`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertThat((vm.menus.value as Loadable.Loaded).value).hasSize(3)
        assertThat(vm.selectedMenuId.value).isEqualTo(400)
        assertThat(vm.selectedDate.value).isEqualTo(today)
        assertThat(vm.selectedMealTypeId.value).isEqualTo(3)
        assertThat((vm.currentSchedule.value as Loadable.Loaded).value[today]?.menuId).isEqualTo(400)
    }

    @Test
    fun `selectMenu switches canteen menu for map quick action`() = runTest {
        val vm = createViewModel()
        vm.selectMenu(701)
        advanceUntilIdle()

        assertThat(vm.selectedMenuId.value).isEqualTo(701)
        assertThat((vm.currentSchedule.value as Loadable.Loaded).value[today]?.menuId).isEqualTo(701)
    }

    @Test
    fun `dish detail is fetched once per dish for the carousel`() = runTest {
        val detail = MensaDishDetail(
            id = 101015, name = "Pasta al pomodoro", description = "Piatto caldo", imageUrl = null,
            netQuantityGrams = 320.0, ingredients = emptyList(), majorAllergens = emptyList(),
            traceAllergens = emptyList(), displayNutrition = false, nutritionSource = null,
            nutritionRows = emptyList(), gdaItems = emptyList(), dietaryBadges = emptyList(),
        )
        coEvery { repository.getDishDetail(101015) } returns detail
        val vm = createViewModel()
        advanceUntilIdle()

        vm.openDishPager(listOf(sampleDish), 0)
        vm.requestDishDetail(101015)
        vm.requestDishDetail(101015)
        advanceUntilIdle()

        assertThat(vm.backStack.value.last()).isEqualTo(MensaSubPage.DishPager)
        assertThat(vm.pager.value?.dishes).containsExactly(sampleDish)
        assertThat(vm.dishDetails.value[101015]).isEqualTo(MensaDishDetailState.Loaded(detail))
        coVerify(exactly = 1) { repository.getDishDetail(101015) }

        vm.back()
        assertThat(vm.backStack.value.last()).isEqualTo(MensaSubPage.Home)
    }

    @Test
    fun `a failed dish detail can be retried`() = runTest {
        coEvery { repository.getDishDetail(101015) } throws IllegalStateException("offline")
        val vm = createViewModel()
        advanceUntilIdle()

        vm.requestDishDetail(101015)
        advanceUntilIdle()
        assertThat(vm.dishDetails.value[101015]).isEqualTo(MensaDishDetailState.Failed)

        vm.retryDishDetail(101015)
        advanceUntilIdle()
        coVerify(exactly = 2) { repository.getDishDetail(101015) }
    }

    @Test
    fun `the menu asks where first, then opens a bar on its day step`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.openMenu()
        assertThat(vm.backStack.value.last()).isEqualTo(MensaSubPage.PlacePicker)

        vm.pickPlace(MensaPlace.BAR_U3)
        assertThat(vm.selectedPlace.value).isEqualTo(MensaPlace.BAR_U3)
        assertThat(vm.selectedMenuId.value).isEqualTo(701)
        assertThat(vm.backStack.value)
            .containsExactly(MensaSubPage.Home, MensaSubPage.PlacePicker, MensaSubPage.MenuSetup).inOrder()

        vm.confirmMenuSetup()
        assertThat(vm.backStack.value).containsExactly(MensaSubPage.Home, MensaSubPage.Menu).inOrder()

        vm.back()
        vm.openMenu()
        assertThat(vm.backStack.value).containsExactly(MensaSubPage.Home, MensaSubPage.Menu).inOrder()
    }

    @Test
    fun `a canteen asks which menu after the place`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.openMenu()
        vm.pickPlace(MensaPlace.CANTEEN_U12)
        assertThat(vm.backStack.value.last()).isEqualTo(MensaSubPage.MenuPicker)

        vm.pickMenu(598)
        assertThat(vm.selectedMenuId.value).isEqualTo(598)
        assertThat(vm.backStack.value).containsExactly(
            MensaSubPage.Home, MensaSubPage.PlacePicker, MensaSubPage.MenuPicker, MensaSubPage.MenuSetup,
        ).inOrder()
    }

    @Test
    fun `back from each selection step returns to the step before it`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.openMenu()
        vm.pickPlace(MensaPlace.CANTEEN_U6)
        vm.pickMenu(598)

        vm.back()
        assertThat(vm.backStack.value.last()).isEqualTo(MensaSubPage.MenuPicker)
        vm.back()
        assertThat(vm.backStack.value.last()).isEqualTo(MensaSubPage.PlacePicker)
        vm.back()
        assertThat(vm.backStack.value).containsExactly(MensaSubPage.Home)
    }

    @Test
    fun `closing the sheet forgets the choices, so the next opening starts from the place`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.openMenu()
        vm.pickPlace(MensaPlace.BAR_U3)
        vm.confirmMenuSetup()
        vm.back()
        vm.openMenu()
        assertThat(vm.backStack.value.last()).isEqualTo(MensaSubPage.Menu)

        vm.resetNavigation()
        vm.openMenu()

        assertThat(vm.selectedPlace.value).isNull()
        assertThat(vm.backStack.value).containsExactly(MensaSubPage.Home, MensaSubPage.PlacePicker).inOrder()
    }

    @Test
    fun `changing the place from the day step comes back to it, asking the menu only when it no longer fits`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.openMenu()
        vm.pickPlace(MensaPlace.CANTEEN_U12)
        vm.pickMenu(598)

        // Same kind of place: the menu chosen stays.
        vm.openPlacePicker()
        vm.pickPlace(MensaPlace.CANTEEN_U6)
        assertThat(vm.selectedMenuId.value).isEqualTo(598)
        assertThat(vm.backStack.value.last()).isEqualTo(MensaSubPage.MenuSetup)

        // A bar has its own menu.
        vm.openPlacePicker()
        vm.pickPlace(MensaPlace.BAR_U3)
        assertThat(vm.selectedMenuId.value).isEqualTo(701)
        assertThat(vm.backStack.value.last()).isEqualTo(MensaSubPage.MenuSetup)

        // And back to a canteen, which asks again.
        vm.openPlacePicker()
        vm.pickPlace(MensaPlace.CANTEEN_U12)
        assertThat(vm.backStack.value.last()).isEqualTo(MensaSubPage.MenuPicker)
        vm.pickMenu(400)
        assertThat(vm.backStack.value.last()).isEqualTo(MensaSubPage.MenuSetup)
    }

    @Test
    fun `editing the day from the menu returns to the menu`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.openMenu()
        vm.pickPlace(MensaPlace.CANTEEN_U12)
        vm.pickMenu(400)
        vm.confirmMenuSetup()

        vm.openMenuSetup()
        vm.confirmMenuSetup()

        assertThat(vm.backStack.value).containsExactly(MensaSubPage.Home, MensaSubPage.Menu).inOrder()
    }

    @Test
    fun `changing the menu from the day step comes back to the day step`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.openMenu()
        vm.pickPlace(MensaPlace.CANTEEN_U12)
        vm.pickMenu(400)

        vm.openMenuPicker()
        vm.pickMenu(598)

        assertThat(vm.selectedMenuId.value).isEqualTo(598)
        assertThat(vm.backStack.value.last()).isEqualTo(MensaSubPage.MenuSetup)
    }

    @Test
    fun `a place picked on the campus map opens on the day step`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.preselectPlace(MensaPlace.BAR_U3)
        advanceUntilIdle()

        assertThat(vm.selectedMenuId.value).isEqualTo(701)
        assertThat(vm.backStack.value).containsExactly(MensaSubPage.Home, MensaSubPage.MenuSetup).inOrder()

        vm.confirmMenuSetup()
        assertThat(vm.backStack.value).containsExactly(MensaSubPage.Home, MensaSubPage.Menu).inOrder()
    }

    @Test
    fun `the canteen from the map becomes the gluten-free menu for someone avoiding gluten`() = runTest {
        preferencesFlow.value = MensaAllergenPreferences(allergenIds = setOf(37))
        val vm = createViewModel()
        advanceUntilIdle()

        vm.preselectPlace(MensaPlace.CANTEEN_U12)
        advanceUntilIdle()
        assertThat(vm.selectedMenuId.value).isEqualTo(598)

        // A bar has no gluten-free variant, so it stays what was asked for.
        vm.preselectPlace(MensaPlace.BAR_U3)
        advanceUntilIdle()
        assertThat(vm.selectedMenuId.value).isEqualTo(701)
    }

    @Test
    fun `the canteen from the map stays the canteen without a gluten selection`() = runTest {
        preferencesFlow.value = MensaAllergenPreferences(allergenIds = setOf(7))
        val vm = createViewModel()
        advanceUntilIdle()

        vm.preselectPlace(MensaPlace.CANTEEN_U6)
        advanceUntilIdle()
        assertThat(vm.selectedMenuId.value).isEqualTo(400)
    }

    @Test
    fun `a lunch-only place leaves dinner out and a canteen that serves it keeps it`() = runTest {
        val vm = createViewModel(dinner = true)
        advanceUntilIdle()
        vm.openMenu()
        vm.pickPlace(MensaPlace.CANTEEN_U12)
        vm.pickMenu(400)

        vm.selectMealType(5)
        assertThat(vm.selectedMealTypeId.value).isEqualTo(5)

        vm.openPlacePicker()
        vm.pickPlace(MensaPlace.CANTEEN_U6)
        assertThat(vm.selectedMealTypeId.value).isEqualTo(3)
    }

    @Test
    fun `the menu can be ordered from only when signed in and a booking is open for the day and meal`() = runTest {
        val vm = createViewModel(loggedIn = true, reservations = emptyList())
        advanceUntilIdle()
        vm.preselectPlace(MensaPlace.CANTEEN_U12)
        advanceUntilIdle()
        assertThat(vm.orderAvailability.value).isEqualTo(MensaOrderAvailability.SignedOut)

        sessionFlow.value = MensaSession("a@b.it", "A", "U-1", 0.0)
        advanceUntilIdle()
        assertThat(vm.orderAvailability.value).isEqualTo(MensaOrderAvailability.Available(sampleService))
        assertThat(vm.bookableDates.value).containsExactly(today)

        // The bar has no booking service in this data.
        vm.pickPlace(MensaPlace.BAR_U3)
        advanceUntilIdle()
        assertThat(vm.orderAvailability.value).isEqualTo(MensaOrderAvailability.Unavailable)
        assertThat(vm.bookableDates.value).isEmpty()
    }

    @Test
    fun `a day and meal already booked is not offered again`() = runTest {
        val vm = createViewModel(loggedIn = true)
        sessionFlow.value = MensaSession("a@b.it", "A", "U-1", 0.0)
        advanceUntilIdle()
        vm.preselectPlace(MensaPlace.CANTEEN_U12)
        advanceUntilIdle()

        assertThat(vm.orderAvailability.value).isEqualTo(MensaOrderAvailability.AlreadyBooked(sampleReservation))
    }

    @Test
    fun `a combination the canteen refuses is reported with its first paragraph`() = runTest {
        sessionFlow.value = MensaSession("a@b.it", "A", "U-1", 0.0)
        coEvery { repository.calculateCartPricing(any(), any(), any()) } throws MensaServerMessageException(
            "I seguenti extra non possono essere prenotati: PASTA (Primi Piatti).\n\nCOMBINAZIONI CONSENTITE ...",
            RuntimeException(),
        )
        val vm = createViewModel(loggedIn = true)
        advanceUntilIdle()

        vm.addDishToCart(sampleDish)
        advanceUntilIdle()

        assertThat(vm.cartPricingError.value).isEqualTo("I seguenti extra non possono essere prenotati: PASTA (Primi Piatti).")
        vm.clearCart()
        assertThat(vm.cartPricingError.value).isNull()
    }

    @Test
    fun `the canteen takes one dish per course and a new pick replaces the old one`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectMenu(400)
        advanceUntilIdle()
        assertThat(vm.onePerCourse.value).isTrue()

        val otherPrimo = sampleDish.copy(id = 101016, name = "Pasta al pesto")
        val secondo = sampleDish.copy(id = 101017, name = "Pollo", courseId = 2, courseName = "Secondi")
        vm.addDishToCart(sampleDish)
        vm.addDishToCart(sampleDish)
        vm.incrementCartDish(sampleDish.id)
        assertThat(vm.cartDishes.value[sampleDish.id]?.quantity).isEqualTo(1)

        vm.addDishToCart(secondo)
        vm.addDishToCart(otherPrimo)
        assertThat(vm.cartDishes.value.keys).containsExactly(otherPrimo.id, secondo.id)
        assertThat(vm.cartDishes.value.values.map { it.quantity }).containsExactly(1, 1)
    }

    @Test
    fun `a bar counts any number of the same dish`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectMenu(701)
        advanceUntilIdle()
        assertThat(vm.onePerCourse.value).isFalse()

        vm.addDishToCart(sampleDish)
        vm.addDishToCart(sampleDish)
        vm.incrementCartDish(sampleDish.id)
        assertThat(vm.cartDishes.value[sampleDish.id]?.quantity).isEqualTo(3)
    }

    @Test
    fun `the heart adds a dish to the favourites and takes it off again`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.toggleFavourite(sampleDish)
        advanceUntilIdle()
        coVerify { favouritesRepository.setFavourite(MensaFavourite(sampleDish.id, sampleDish.name), true) }

        favouritesFlow.value = listOf(MensaFavourite(sampleDish.id, sampleDish.name))
        advanceUntilIdle()
        vm.toggleFavourite(sampleDish)
        advanceUntilIdle()
        coVerify { favouritesRepository.setFavourite(MensaFavourite(sampleDish.id, sampleDish.name), false) }
    }

    @Test
    fun `turning the favourites reminder on plans the daily check and runs one now`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.setFavouriteNotify(true)
        advanceUntilIdle()

        coVerify { favouritesRepository.setNotify(true) }
        io.mockk.verify { favouritesScheduler.apply(true, any()) }
        io.mockk.verify { favouritesScheduler.checkNow() }
    }

    @Test
    fun `the cart belongs to the menu, so changing the menu empties it`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.addDishToCart(sampleDish)
        assertThat(vm.cartDishes.value).isNotEmpty()

        vm.selectMenu(701)
        advanceUntilIdle()

        assertThat(vm.cartDishes.value).isEmpty()
    }

    @Test
    fun `account deletion checks first and files the request`() = runTest {
        coEvery { repository.canDeleteAccount() } returns true
        coEvery { repository.requestAccountDeletion() } returns "Richiesta inviata"
        val vm = createViewModel(loggedIn = true)
        sessionFlow.value = MensaSession("a@b.it", "A", "U-1", 0.0)
        advanceUntilIdle()
        vm.openAccount()
        vm.openAccountDelete()
        advanceUntilIdle()
        assertThat(vm.deletionCheck.value).isEqualTo(MensaDeletionCheck.Allowed)

        vm.events.test {
            vm.requestAccountDeletion()
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(MensaEvent.AccountDeletionRequested("Richiesta inviata"))
        }
        assertThat(vm.backStack.value).containsExactly(MensaSubPage.Home, MensaSubPage.Account).inOrder()
    }

    @Test
    fun `an operator refusal is shown as the reason`() = runTest {
        coEvery { repository.canDeleteAccount() } throws MensaServerMessageException("Hai ordini attivi")
        val vm = createViewModel(loggedIn = true)
        advanceUntilIdle()

        vm.openAccountDelete()
        advanceUntilIdle()

        assertThat(vm.deletionCheck.value).isEqualTo(MensaDeletionCheck.NotAllowed("Hai ordini attivi"))
    }

    @Test
    fun `cart mutations trigger temporary cost calculation when signed in`() = runTest {
        sessionFlow.value = MensaSession("student@campus.unimib.it", "Mario Rossi", "U-1", 5.0)
        coEvery { repository.calculateCartPricing(435, 5, mapOf(101015 to 1)) } returns MensaCartPricing(
            currencySymbol = "€",
            total = 3.5,
            fixedCost = 0.0,
            extraCost = 0.0,
            combinationCost = 3.5,
            reservationCost = 0.0,
            cfpSum = "0.42",
            cfpUnit = "KgCO2e",
            cfpEquivalentPhrase = null,
        )
        val vm = createViewModel(loggedIn = true)
        advanceUntilIdle()

        vm.addDishToCart(sampleDish)
        advanceUntilIdle()

        assertThat(vm.cartDishes.value[101015]?.quantity).isEqualTo(1)
        assertThat((vm.cartPricing.value as Loadable.Loaded).value.total).isEqualTo(3.5)

        // The canteen takes one of each, so there is no second portion to add.
        vm.incrementCartDish(101015)
        assertThat(vm.cartDishes.value[101015]?.quantity).isEqualTo(1)

        vm.decrementCartDish(101015)
        assertThat(vm.cartDishes.value).isEmpty()
    }

    @Test
    fun `submitTakeawayOrder clears cart and navigates to reservations`() = runTest {
        sessionFlow.value = MensaSession("student@campus.unimib.it", "Mario Rossi", "U-1", 5.0)
        coEvery { repository.calculateCartPricing(any(), any(), any()) } returns MensaCartPricing(
            "€", 3.5, 0.0, 0.0, 3.5, 0.0, null, null, null,
        )
        coEvery {
            repository.submitTakeawayOrder(435, 400, 3, 120, today, 10, mapOf(101015 to 1), "")
        } returns (1201989 to "2026-LU111201989")

        val vm = createViewModel(loggedIn = true)
        advanceUntilIdle()
        vm.addDishToCart(sampleDish)
        vm.openTakeawayOrder()
        advanceUntilIdle()

        vm.events.test {
            vm.submitTakeawayOrder()
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(MensaEvent.OrderSubmitted("2026-LU111201989"))
        }
        assertThat(vm.cartDishes.value).isEmpty()
        assertThat(vm.backStack.value.last()).isEqualTo(MensaSubPage.Reservations)
    }

    @Test
    fun `cancelReservation calls repository and emits OrderCancelled`() = runTest {
        sessionFlow.value = MensaSession("student@campus.unimib.it", "Mario Rossi", "U-1", 5.0)
        val vm = createViewModel(loggedIn = true)
        advanceUntilIdle()

        vm.events.test {
            vm.cancelReservation(sampleReservation)
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(MensaEvent.OrderCancelled)
        }
        coVerify { repository.cancelTakeawayReservation(1201989, null) }
    }

    @Test
    fun `confirming the day step opens the menu above the hub`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.preselectPlace(MensaPlace.CANTEEN_U12)

        vm.confirmMenuSetup()

        assertThat(vm.backStack.value).containsExactly(MensaSubPage.Home, MensaSubPage.Menu).inOrder()
    }

    @Test
    fun `browseDishes from the order page opens the menu, or returns to it when underneath`() = runTest {
        val vm = createViewModel(loggedIn = true)
        sessionFlow.value = MensaSession("a@b.it", "A", null, 0.0)
        advanceUntilIdle()

        vm.openTakeawayOrder()
        vm.browseDishes()
        assertThat(vm.backStack.value.last()).isEqualTo(MensaSubPage.Menu)

        vm.resetNavigation()
        vm.preselectPlace(MensaPlace.CANTEEN_U12)
        vm.confirmMenuSetup()
        vm.openTakeawayOrder()
        vm.browseDishes()
        assertThat(vm.backStack.value).containsExactly(MensaSubPage.Home, MensaSubPage.Menu).inOrder()
    }

    @Test
    fun `booking while signed out asks for login and then continues to the order page`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        val signedIn = MensaSession("a@b.it", "A", "U-1", 0.0)
        coEvery { repository.login("a@b.it", "pw") } coAnswers {
            sessionFlow.value = signedIn
            signedIn
        }

        vm.openTakeawayOrder()
        assertThat(vm.backStack.value.last()).isEqualTo(MensaSubPage.Login)

        vm.login("a@b.it", "pw")
        advanceUntilIdle()

        assertThat(vm.backStack.value.last()).isEqualTo(MensaSubPage.TakeawayOrder)
    }

    @Test
    fun `the order recap needs no account, and continuing signs in before the pickup page`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        val signedIn = MensaSession("a@b.it", "A", "U-1", 0.0)
        coEvery { repository.login("a@b.it", "pw") } coAnswers {
            sessionFlow.value = signedIn
            signedIn
        }
        vm.preselectPlace(MensaPlace.CANTEEN_U12)
        vm.confirmMenuSetup()
        vm.addDishToCart(sampleDish)

        vm.openOrderSummary()
        assertThat(vm.backStack.value)
            .containsExactly(MensaSubPage.Home, MensaSubPage.Menu, MensaSubPage.OrderSummary).inOrder()

        vm.openTakeawayOrder()
        assertThat(vm.backStack.value.last()).isEqualTo(MensaSubPage.Login)
        vm.login("a@b.it", "pw")
        advanceUntilIdle()

        assertThat(vm.backStack.value).containsExactly(
            MensaSubPage.Home, MensaSubPage.Menu, MensaSubPage.OrderSummary, MensaSubPage.TakeawayOrder,
        ).inOrder()
        assertThat(vm.cartDishes.value[101015]?.quantity).isEqualTo(1)
    }

    @Test
    fun `browseDishes from the recap goes back to the menu underneath`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.preselectPlace(MensaPlace.CANTEEN_U12)
        vm.confirmMenuSetup()
        vm.openOrderSummary()

        vm.browseDishes()

        assertThat(vm.backStack.value).containsExactly(MensaSubPage.Home, MensaSubPage.Menu).inOrder()
    }

    @Test
    fun `setCartQuantity sets an exact amount, caps it and removes at zero`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.setCartQuantity(sampleDish, 7)
        assertThat(vm.cartDishes.value[101015]?.quantity).isEqualTo(7)

        vm.setCartQuantity(sampleDish, 500)
        assertThat(vm.cartDishes.value[101015]?.quantity).isEqualTo(MENSA_MAX_CART_QUANTITY)

        vm.incrementCartDish(101015)
        assertThat(vm.cartDishes.value[101015]?.quantity).isEqualTo(MENSA_MAX_CART_QUANTITY)

        vm.setCartQuantity(sampleDish, 0)
        assertThat(vm.cartDishes.value).isEmpty()
    }

    @Test
    fun `the carousel remembers which menu row each dish was on`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.openDishPager(listOf(sampleDish, sampleDish.copy(id = 2)), 1, listOf(3, 5))
        assertThat(vm.pager.value?.menuRows).containsExactly(3, 5).inOrder()

        // Rows that do not line up with the dishes are dropped rather than trusted.
        vm.openDishPager(listOf(sampleDish), 0, listOf(3, 5))
        assertThat(vm.pager.value?.menuRows).isNull()
    }

    @Test
    fun `openFoodCatalog loads every dish and the meal combinations`() = runTest {
        coEvery { repository.getAllUniversityDishes() } returns listOf(sampleDish)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.openFoodCatalog()
        advanceUntilIdle()

        assertThat(vm.backStack.value.last()).isEqualTo(MensaSubPage.FoodCatalog)
        assertThat((vm.catalog.value as Loadable.Loaded).value).containsExactly(sampleDish)
        assertThat(vm.eligibility.value).isEqualTo(eligibility)
    }

    @Test
    fun `allergen choices are stored through the repository and flag matching dishes`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.setAllergenSelected(7, true)
        advanceUntilIdle()
        coVerify { preferencesRepository.setAllergenSelected(7, true) }

        val gluten = MensaAllergen(7, "Glutine", "Glutine", null)
        val withGluten = sampleDish.copy(majorAllergens = listOf(gluten))
        val withTraces = sampleDish.copy(traceAllergens = listOf(gluten))
        val preferences = MensaAllergenPreferences(allergenIds = setOf(7))

        assertThat(withGluten.allergenMatch(preferences)).isEqualTo(MensaAllergenMatch.CONTAINS)
        assertThat(withTraces.allergenMatch(preferences)).isEqualTo(MensaAllergenMatch.TRACE)
        assertThat(sampleDish.allergenMatch(preferences)).isEqualTo(MensaAllergenMatch.NONE)
        assertThat(withGluten.allergenMatch(MensaAllergenPreferences())).isEqualTo(MensaAllergenMatch.NONE)
    }
}
