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
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaSubPage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class MensaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionFlow = MutableStateFlow<MensaSession?>(null)
    private val repository: MensaRepository = mockk(relaxed = true)
    private val preferencesFlow = MutableStateFlow(MensaAllergenPreferences())
    private val preferencesRepository: MensaAllergenPreferencesRepository = mockk(relaxed = true)
    private val eligibilityProvider: MensaEligibilityProvider = mockk()
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

    private fun createViewModel(loggedIn: Boolean = false): MensaViewModel {
        every { repository.observeSession() } returns sessionFlow
        coEvery { repository.isLoggedIn() } returns loggedIn
        coEvery { repository.getMenus() } returns menus
        coEvery { repository.getMenuSchedule(400) } returns daySchedule(400)
        coEvery { repository.getMenuSchedule(701) } returns daySchedule(701)
        coEvery { repository.getTakeawayServices() } returns listOf(sampleService)
        coEvery { repository.getTakeawayReservations() } returns listOf(sampleReservation)
        every { preferencesRepository.observe() } returns preferencesFlow
        coEvery { eligibilityProvider.getEligibility() } returns eligibility
        return MensaViewModel(repository, preferencesRepository, eligibilityProvider)
    }

    @Test
    fun `initial load populates menus and selects default schedule`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertThat((vm.menus.value as Loadable.Loaded).value).hasSize(2)
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
    fun `the menu asks for canteen day and meal first, then opens directly`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.openMenu()
        assertThat(vm.backStack.value.last()).isEqualTo(MensaSubPage.MenuSetup)

        vm.confirmMenuSetup()
        assertThat(vm.backStack.value).containsExactly(MensaSubPage.Home, MensaSubPage.Menu).inOrder()

        vm.back()
        vm.openMenu()
        assertThat(vm.backStack.value).containsExactly(MensaSubPage.Home, MensaSubPage.Menu).inOrder()
    }

    @Test
    fun `editing the context from the menu returns to the menu`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.preselectMenu(400)

        vm.openMenu()
        vm.openMenuSetup()
        vm.confirmMenuSetup()

        assertThat(vm.backStack.value).containsExactly(MensaSubPage.Home, MensaSubPage.Menu).inOrder()
    }

    @Test
    fun `a canteen picked on the campus map skips the setup step`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.preselectMenu(701)
        advanceUntilIdle()
        vm.openMenu()

        assertThat(vm.selectedMenuId.value).isEqualTo(701)
        assertThat(vm.backStack.value.last()).isEqualTo(MensaSubPage.Menu)
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

        vm.incrementCartDish(101015)
        assertThat(vm.cartDishes.value[101015]?.quantity).isEqualTo(2)

        vm.decrementCartDish(101015)
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
    fun `openMenu pushes the menu above the hub`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.preselectMenu(400)

        vm.openMenu()

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
        vm.preselectMenu(400)
        vm.openMenu()
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
