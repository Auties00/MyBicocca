package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergen
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenPreferences
import it.attendance100.mybicocca.domain.model.mensa.MensaCartPricing
import it.attendance100.mybicocca.domain.model.mensa.MensaDaySchedule
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.domain.model.mensa.MensaEligibility
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuSummary
import it.attendance100.mybicocca.domain.model.mensa.MensaReservation
import it.attendance100.mybicocca.domain.model.mensa.MensaSession
import it.attendance100.mybicocca.domain.model.mensa.MensaTakeawayService
import it.attendance100.mybicocca.domain.repository.MensaAllergenPreferencesRepository
import it.attendance100.mybicocca.domain.repository.MensaEligibilityProvider
import it.attendance100.mybicocca.domain.repository.MensaNotLoggedInException
import it.attendance100.mybicocca.domain.repository.MensaRepository
import it.attendance100.mybicocca.domain.repository.MensaServerMessageException
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaAccountField
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaDeletionCheck
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaDishDetailState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaDishPagerRequest
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaSubPage
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Single owner of the Mensa modal sheet (`unibicocca.compasscloud.it`): daily/weekly canteen menu
 * browsing, dish recipe/allergen/nutrition detail, RistoCloud account/wallet session, and the
 * take-away order + reservations management flows.
 */
@HiltViewModel
class MensaViewModel @Inject constructor(
    private val repository: MensaRepository,
    private val allergenPreferencesRepository: MensaAllergenPreferencesRepository,
    private val eligibilityProvider: MensaEligibilityProvider,
) : ViewModel() {

    private val _backStack = MutableStateFlow<List<MensaSubPage>>(listOf(MensaSubPage.Home))
    val backStack: StateFlow<List<MensaSubPage>> = _backStack.asStateFlow()

    val session: StateFlow<MensaSession?> = repository.observeSession()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val allergenPreferences: StateFlow<MensaAllergenPreferences> = allergenPreferencesRepository.observe()
        .stateIn(viewModelScope, SharingStarted.Eagerly, MensaAllergenPreferences())

    private val _allergens = MutableStateFlow<Loadable<List<MensaAllergen>>>(Loadable.NotYetLoaded)
    val allergens: StateFlow<Loadable<List<MensaAllergen>>> = _allergens.asStateFlow()

    private val _allergensStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val allergensStatus: StateFlow<SyncStatus> = _allergensStatus.asStateFlow()

    private val _catalog = MutableStateFlow<Loadable<List<MensaDish>>>(Loadable.NotYetLoaded)
    val catalog: StateFlow<Loadable<List<MensaDish>>> = _catalog.asStateFlow()

    private val _catalogStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val catalogStatus: StateFlow<SyncStatus> = _catalogStatus.asStateFlow()

    private val _eligibility = MutableStateFlow<MensaEligibility?>(null)
    val eligibility: StateFlow<MensaEligibility?> = _eligibility.asStateFlow()

    private val _accountBusy = MutableStateFlow(false)
    val accountBusy: StateFlow<Boolean> = _accountBusy.asStateFlow()

    private val _menus = MutableStateFlow<Loadable<List<MensaMenuSummary>>>(Loadable.NotYetLoaded)
    val menus: StateFlow<Loadable<List<MensaMenuSummary>>> = _menus.asStateFlow()

    private val _menusStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val menusStatus: StateFlow<SyncStatus> = _menusStatus.asStateFlow()

    private val _selectedMenuId = MutableStateFlow<Int?>(null)
    val selectedMenuId: StateFlow<Int?> = _selectedMenuId.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val _selectedMealTypeId = MutableStateFlow<Int?>(null)
    val selectedMealTypeId: StateFlow<Int?> = _selectedMealTypeId.asStateFlow()

    private val scheduleCache = mutableMapOf<Int, Map<LocalDate, MensaDaySchedule>>()
    private val _currentSchedule = MutableStateFlow<Loadable<Map<LocalDate, MensaDaySchedule>>>(Loadable.NotYetLoaded)
    val currentSchedule: StateFlow<Loadable<Map<LocalDate, MensaDaySchedule>>> = _currentSchedule.asStateFlow()

    private val _scheduleStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val scheduleStatus: StateFlow<SyncStatus> = _scheduleStatus.asStateFlow()

    private val _pager = MutableStateFlow<MensaDishPagerRequest?>(null)
    val pager: StateFlow<MensaDishPagerRequest?> = _pager.asStateFlow()

    private val _dishDetails = MutableStateFlow<Map<Int, MensaDishDetailState>>(emptyMap())
    val dishDetails: StateFlow<Map<Int, MensaDishDetailState>> = _dishDetails.asStateFlow()

    private val _menuContextConfirmed = MutableStateFlow(false)

    private val _deletionCheck = MutableStateFlow<MensaDeletionCheck>(MensaDeletionCheck.Checking)
    val deletionCheck: StateFlow<MensaDeletionCheck> = _deletionCheck.asStateFlow()

    private val _loginSubmitting = MutableStateFlow(false)
    val loginSubmitting: StateFlow<Boolean> = _loginSubmitting.asStateFlow()

    private val _takeawayServices = MutableStateFlow<Loadable<List<MensaTakeawayService>>>(Loadable.NotYetLoaded)
    val takeawayServices: StateFlow<Loadable<List<MensaTakeawayService>>> = _takeawayServices.asStateFlow()

    private val _takeawayStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val takeawayStatus: StateFlow<SyncStatus> = _takeawayStatus.asStateFlow()

    private val _selectedServiceId = MutableStateFlow<Int?>(null)
    val selectedServiceId: StateFlow<Int?> = _selectedServiceId.asStateFlow()

    private val _selectedTakeawayDate = MutableStateFlow<LocalDate?>(null)
    val selectedTakeawayDate: StateFlow<LocalDate?> = _selectedTakeawayDate.asStateFlow()

    private val _selectedTakeawayMealTypeId = MutableStateFlow<Int?>(null)
    val selectedTakeawayMealTypeId: StateFlow<Int?> = _selectedTakeawayMealTypeId.asStateFlow()

    private val _selectedPickupLocationId = MutableStateFlow<Int?>(null)
    val selectedPickupLocationId: StateFlow<Int?> = _selectedPickupLocationId.asStateFlow()

    private val _selectedTimeSlotId = MutableStateFlow<Int?>(null)
    val selectedTimeSlotId: StateFlow<Int?> = _selectedTimeSlotId.asStateFlow()

    private val _orderNote = MutableStateFlow("")
    val orderNote: StateFlow<String> = _orderNote.asStateFlow()

    private val _cartDishes = MutableStateFlow<Map<Int, MensaCartEntry>>(emptyMap())
    val cartDishes: StateFlow<Map<Int, MensaCartEntry>> = _cartDishes.asStateFlow()

    private val _cartPricing = MutableStateFlow<Loadable<MensaCartPricing>>(Loadable.NotYetLoaded)
    val cartPricing: StateFlow<Loadable<MensaCartPricing>> = _cartPricing.asStateFlow()

    private val _submittingOrder = MutableStateFlow(false)
    val submittingOrder: StateFlow<Boolean> = _submittingOrder.asStateFlow()

    private val _reservations = MutableStateFlow<Loadable<List<MensaReservation>>>(Loadable.NotYetLoaded)
    val reservations: StateFlow<Loadable<List<MensaReservation>>> = _reservations.asStateFlow()

    private val _reservationsStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val reservationsStatus: StateFlow<SyncStatus> = _reservationsStatus.asStateFlow()

    private val _cancellingId = MutableStateFlow<Int?>(null)
    val cancellingId: StateFlow<Int?> = _cancellingId.asStateFlow()

    private val _events = Channel<MensaEvent>(Channel.BUFFERED)
    val events: Flow<MensaEvent> = _events.receiveAsFlow()

    private var scheduleJob: Job? = null
    private var pricingJob: Job? = null
    private var pendingMenuIdFocus: Int? = null
    private var pendingAfterLogin: MensaSubPage? = null

    init {
        viewModelScope.launch { fetchMenus() }
        viewModelScope.launch {
            if (repository.isLoggedIn()) {
                runCatching { repository.refreshAccountInfo() }
                runCatching { fetchTakeawayServicesInternal() }
                runCatching { fetchReservationsInternal() }
            }
        }
    }

    private fun push(page: MensaSubPage) {
        _backStack.value = _backStack.value + page
    }

    fun back() {
        if (busy()) return
        if (_backStack.value.size > 1) _backStack.value = _backStack.value.dropLast(1)
    }

    private fun busy() = _submittingOrder.value || _loginSubmitting.value || _accountBusy.value

    fun resetNavigation() {
        pendingAfterLogin = null
        _backStack.value = listOf(MensaSubPage.Home)
        _pager.value = null
    }

    fun refreshMenus() {
        viewModelScope.launch { fetchMenus() }
    }

    private suspend fun fetchMenus() {
        _menusStatus.value = SyncStatus.Refreshing
        runCatching { repository.getMenus() }.fold(
            onSuccess = { list ->
                _menus.value = Loadable.Loaded(list)
                _menusStatus.value = SyncStatus.Idle
                val requested = pendingMenuIdFocus?.takeIf { id -> list.any { it.id == id } }
                val targetMenuId = requested ?: _selectedMenuId.value?.takeIf { id -> list.any { it.id == id } }
                    ?: list.firstOrNull()?.id
                pendingMenuIdFocus = null
                if (targetMenuId != null) selectMenu(targetMenuId)
            },
            onFailure = { _menusStatus.value = SyncStatus.Failed(it) },
        )
    }

    /** Pre-selects a menu before (or while) opening the Mensa sheet, e.g. from the Campus Map. */
    fun selectMenu(menuId: Int) {
        pendingMenuIdFocus = menuId
        val list = _menus.value.valueOrNull()
        if (list != null && list.none { it.id == menuId }) return
        if (_selectedMenuId.value == menuId && _currentSchedule.value is Loadable.Loaded) return
        _selectedMenuId.value = menuId
        val menuSummary = list?.firstOrNull { it.id == menuId }
        val today = LocalDate.now(AppClock.clock)
        val cached = scheduleCache[menuId]
        if (cached != null) {
            applyLoadedSchedule(cached, today)
        } else {
            val defaultDate = menuSummary?.activeDays?.let { pickBestDate(it, today) }
            _selectedDate.value = defaultDate
            _selectedMealTypeId.value = defaultMeal(menuSummary?.mealTypes.orEmpty().map { it.id })
            loadSchedule(menuId, today)
        }
    }

    private fun loadSchedule(menuId: Int, today: LocalDate) {
        scheduleJob?.cancel()
        _currentSchedule.value = Loadable.NotYetLoaded
        _scheduleStatus.value = SyncStatus.Refreshing
        scheduleJob = viewModelScope.launch {
            runCatching { repository.getMenuSchedule(menuId) }.fold(
                onSuccess = { schedule ->
                    scheduleCache[menuId] = schedule
                    if (_selectedMenuId.value == menuId) {
                        applyLoadedSchedule(schedule, today)
                        _scheduleStatus.value = SyncStatus.Idle
                    }
                },
                onFailure = {
                    if (_selectedMenuId.value == menuId) {
                        _scheduleStatus.value = SyncStatus.Failed(it)
                    }
                },
            )
        }
    }

    fun retrySchedule() {
        val menuId = _selectedMenuId.value ?: return
        scheduleCache.remove(menuId)
        loadSchedule(menuId, LocalDate.now(AppClock.clock))
    }

    private fun applyLoadedSchedule(schedule: Map<LocalDate, MensaDaySchedule>, today: LocalDate) {
        _currentSchedule.value = Loadable.Loaded(schedule)
        val availableDates = schedule.keys.sorted()
        val pickedDate = _selectedDate.value?.takeIf { it in schedule }
            ?: pickBestDate(availableDates, today)
        _selectedDate.value = pickedDate
        val day = pickedDate?.let { schedule[it] }
        val availableMeals = day?.mealTypes.orEmpty()
        if (_selectedMealTypeId.value == null || availableMeals.none { it.id == _selectedMealTypeId.value }) {
            _selectedMealTypeId.value = defaultMeal(availableMeals.map { it.id })
        }
    }

    /** Lunch until mid-afternoon, dinner after it, when the canteen serves both. */
    private fun defaultMeal(mealIds: List<Int>): Int? =
        if (mealIds.size > 1 && java.time.LocalTime.now(AppClock.clock).hour >= 15) mealIds.last() else mealIds.firstOrNull()

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        val schedule = _currentSchedule.value.valueOrNull().orEmpty()
        val meals = schedule[date]?.mealTypes.orEmpty()
        if (_selectedMealTypeId.value == null || meals.none { it.id == _selectedMealTypeId.value }) {
            _selectedMealTypeId.value = meals.firstOrNull()?.id
        }
    }

    fun selectMealType(mealTypeId: Int) {
        _selectedMealTypeId.value = mealTypeId
    }

    /** Opens the menu, asking for canteen, day and meal first unless they were already chosen. */
    fun openMenu() {
        push(if (_menuContextConfirmed.value) MensaSubPage.Menu else MensaSubPage.MenuSetup)
    }

    fun openMenuSetup() {
        push(MensaSubPage.MenuSetup)
    }

    /** Leaves the setup step for the menu; the setup page is replaced, so back goes to where it began. */
    fun confirmMenuSetup() {
        _menuContextConfirmed.value = true
        val stack = _backStack.value
        _backStack.value = when {
            stack.getOrNull(stack.lastIndex - 1) == MensaSubPage.Menu -> stack.dropLast(1)
            stack.lastOrNull() == MensaSubPage.MenuSetup -> stack.dropLast(1) + MensaSubPage.Menu
            else -> stack + MensaSubPage.Menu
        }
    }

    /** Pre-selects a canteen from the Campus Map; the menu then opens without the setup step. */
    fun preselectMenu(menuId: Int) {
        selectMenu(menuId)
        _menuContextConfirmed.value = true
    }

    /** Opens the Menu so dishes can be added to the order; goes back when it is already underneath. */
    fun browseDishes() {
        val stack = _backStack.value
        if (stack.getOrNull(stack.lastIndex - 1) == MensaSubPage.Menu) back() else push(MensaSubPage.Menu)
    }

    fun openFoodCatalog() {
        push(MensaSubPage.FoodCatalog)
        if (_catalog.value !is Loadable.Loaded) loadCatalog()
        if (_eligibility.value == null) {
            viewModelScope.launch { _eligibility.value = eligibilityProvider.getEligibility() }
        }
    }

    fun retryCatalog() {
        loadCatalog()
    }

    private fun loadCatalog() {
        _catalogStatus.value = SyncStatus.Refreshing
        viewModelScope.launch {
            runCatching { repository.getAllUniversityDishes() }.fold(
                onSuccess = { dishes ->
                    _catalog.value = Loadable.Loaded(dishes)
                    _catalogStatus.value = SyncStatus.Idle
                },
                onFailure = { _catalogStatus.value = SyncStatus.Failed(it) },
            )
        }
    }

    fun openAllergens() {
        push(MensaSubPage.Allergens)
        if (_allergens.value !is Loadable.Loaded) loadAllergens()
    }

    fun retryAllergens() {
        loadAllergens()
    }

    private fun loadAllergens() {
        _allergensStatus.value = SyncStatus.Refreshing
        viewModelScope.launch {
            runCatching { repository.getAllergens() }.fold(
                onSuccess = { list ->
                    _allergens.value = Loadable.Loaded(list)
                    _allergensStatus.value = SyncStatus.Idle
                },
                onFailure = { _allergensStatus.value = SyncStatus.Failed(it) },
            )
        }
    }

    fun setAllergenSelected(allergenId: Int, selected: Boolean) {
        viewModelScope.launch { allergenPreferencesRepository.setAllergenSelected(allergenId, selected) }
    }

    fun setHideMatchingDishes(hide: Boolean) {
        viewModelScope.launch { allergenPreferencesRepository.setHideMatchingDishes(hide) }
    }

    fun clearAllergens() {
        viewModelScope.launch { allergenPreferencesRepository.clear() }
    }

    fun openAccount() {
        if (session.value == null) {
            openLogin()
            return
        }
        push(MensaSubPage.Account)
        viewModelScope.launch { runCatching { repository.refreshAccountInfo() } }
    }

    fun openAccountDelete() {
        push(MensaSubPage.AccountDelete)
        checkAccountDeletion()
    }

    fun checkAccountDeletion() {
        _deletionCheck.value = MensaDeletionCheck.Checking
        viewModelScope.launch {
            _deletionCheck.value = runCatching { repository.canDeleteAccount() }.fold(
                onSuccess = { if (it) MensaDeletionCheck.Allowed else MensaDeletionCheck.NotAllowed(null) },
                onFailure = {
                    if (it is MensaServerMessageException) MensaDeletionCheck.NotAllowed(it.serverMessage) else MensaDeletionCheck.Failed
                },
            )
        }
    }

    /** Files the deletion request; the operator finishes it by hand, so the session stays signed in. */
    fun requestAccountDeletion() {
        if (_accountBusy.value) return
        _accountBusy.value = true
        viewModelScope.launch {
            runCatching { repository.requestAccountDeletion() }.fold(
                onSuccess = { message ->
                    _backStack.value = listOf(MensaSubPage.Home, MensaSubPage.Account)
                    _events.send(MensaEvent.AccountDeletionRequested(message))
                },
                onFailure = { _events.send(MensaEvent.AccountDeletionFailed(it)) },
            )
            _accountBusy.value = false
        }
    }

    fun openAccountEdit(field: MensaAccountField) {
        push(MensaSubPage.AccountEdit(field))
    }

    fun updateProfile(name: String, phone: String) = runAccountUpdate(MensaAccountField.PROFILE) {
        repository.updateProfile(name, phone)
    }

    fun changeEmail(newEmail: String) = runAccountUpdate(MensaAccountField.EMAIL) {
        repository.changeEmail(newEmail)
    }

    fun changePassword(oldPassword: String, newPassword: String) = runAccountUpdate(MensaAccountField.PASSWORD) {
        repository.changePassword(oldPassword, newPassword)
    }

    private fun runAccountUpdate(field: MensaAccountField, block: suspend () -> Unit) {
        if (_accountBusy.value) return
        _accountBusy.value = true
        viewModelScope.launch {
            runCatching { block() }.fold(
                onSuccess = {
                    if (_backStack.value.lastOrNull() is MensaSubPage.AccountEdit) {
                        _backStack.value = _backStack.value.dropLast(1)
                    }
                    _events.send(MensaEvent.AccountUpdated(field))
                },
                onFailure = { _events.send(MensaEvent.AccountUpdateFailed(it)) },
            )
            _accountBusy.value = false
        }
    }

    fun openDishPager(dishes: List<MensaDish>, startIndex: Int) {
        if (dishes.isEmpty()) return
        val index = startIndex.coerceIn(dishes.indices)
        _pager.value = MensaDishPagerRequest(dishes, index)
        push(MensaSubPage.DishPager)
    }

    /** Loads the full detail of a dish the carousel is on or next to, once. */
    fun requestDishDetail(dishId: Int) {
        val state = _dishDetails.value[dishId]
        if (state is MensaDishDetailState.Loading || state is MensaDishDetailState.Loaded) return
        _dishDetails.update { it + (dishId to MensaDishDetailState.Loading) }
        viewModelScope.launch {
            val result = runCatching { repository.getDishDetail(dishId) }.fold(
                onSuccess = { MensaDishDetailState.Loaded(it) },
                onFailure = { MensaDishDetailState.Failed },
            )
            _dishDetails.update { it + (dishId to result) }
        }
    }

    fun retryDishDetail(dishId: Int) {
        _dishDetails.update { it - dishId }
        requestDishDetail(dishId)
    }

    fun openLogin() {
        push(MensaSubPage.Login)
    }

    private fun openLoginThen(next: MensaSubPage) {
        pendingAfterLogin = next
        openLogin()
    }

    fun login(email: String, password: String) {
        if (_loginSubmitting.value || email.isBlank() || password.isBlank()) return
        _loginSubmitting.value = true
        viewModelScope.launch {
            runCatching { repository.login(email, password) }.fold(
                onSuccess = {
                    if (_backStack.value.lastOrNull() == MensaSubPage.Login) {
                        _backStack.value = _backStack.value.dropLast(1)
                    }
                    _events.send(MensaEvent.LoginSucceeded)
                    runCatching { fetchTakeawayServicesInternal() }
                    runCatching { fetchReservationsInternal() }
                    when (pendingAfterLogin) {
                        MensaSubPage.TakeawayOrder -> enterTakeawayOrder()
                        MensaSubPage.Reservations -> enterReservations()
                        else -> Unit
                    }
                    pendingAfterLogin = null
                },
                onFailure = { _events.send(MensaEvent.LoginFailed(it)) },
            )
            _loginSubmitting.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            runCatching { repository.logout() }
            _takeawayServices.value = Loadable.NotYetLoaded
            _reservations.value = Loadable.NotYetLoaded
            _cartDishes.value = emptyMap()
            _cartPricing.value = Loadable.NotYetLoaded
            _backStack.value = listOf(MensaSubPage.Home)
        }
    }

    fun addDishToCart(dish: MensaDish) {
        val current = _cartDishes.value.toMutableMap()
        val existing = current[dish.id]
        val nextQty = (existing?.quantity ?: 0) + 1
        current[dish.id] = MensaCartEntry(dish = dish, quantity = nextQty)
        _cartDishes.value = current
        recalculateCartPricing()
    }

    fun incrementCartDish(dishId: Int) {
        val existing = _cartDishes.value[dishId] ?: return
        val current = _cartDishes.value.toMutableMap()
        current[dishId] = existing.copy(quantity = existing.quantity + 1)
        _cartDishes.value = current
        recalculateCartPricing()
    }

    fun decrementCartDish(dishId: Int) {
        val existing = _cartDishes.value[dishId] ?: return
        val current = _cartDishes.value.toMutableMap()
        if (existing.quantity <= 1) {
            current.remove(dishId)
        } else {
            current[dishId] = existing.copy(quantity = existing.quantity - 1)
        }
        _cartDishes.value = current
        recalculateCartPricing()
    }

    fun clearCart() {
        _cartDishes.value = emptyMap()
        _cartPricing.value = Loadable.NotYetLoaded
    }

    fun openTakeawayOrder() {
        if (session.value == null) {
            openLoginThen(MensaSubPage.TakeawayOrder)
            return
        }
        enterTakeawayOrder()
    }

    private fun enterTakeawayOrder() {
        push(MensaSubPage.TakeawayOrder)
        if (_takeawayServices.value !is Loadable.Loaded) {
            refreshTakeawayServices()
        } else {
            syncServiceForSelectedMenu()
            recalculateCartPricing()
        }
    }

    fun refreshTakeawayServices() {
        viewModelScope.launch { fetchTakeawayServicesInternal() }
    }

    private suspend fun fetchTakeawayServicesInternal() {
        _takeawayStatus.value = SyncStatus.Refreshing
        runCatching { repository.getTakeawayServices() }.fold(
            onSuccess = { services ->
                _takeawayServices.value = Loadable.Loaded(services)
                _takeawayStatus.value = SyncStatus.Idle
                if (_selectedServiceId.value == null || services.none { it.id == _selectedServiceId.value }) {
                    syncServiceForSelectedMenu()
                }
                recalculateCartPricing()
            },
            onFailure = { cause ->
                _takeawayStatus.value = if (cause is MensaNotLoggedInException) SyncStatus.Idle else SyncStatus.Failed(cause)
            },
        )
    }

    private fun syncServiceForSelectedMenu() {
        val services = _takeawayServices.value.valueOrNull().orEmpty()
        if (services.isEmpty()) return
        val menuId = _selectedMenuId.value
        val match = services.firstOrNull { it.menuId == menuId } ?: services.firstOrNull { it.isFavorite } ?: services.first()
        selectTakeawayService(match.id)
    }

    fun selectTakeawayService(serviceId: Int) {
        val services = _takeawayServices.value.valueOrNull().orEmpty()
        val service = services.firstOrNull { it.id == serviceId } ?: return
        _selectedServiceId.value = serviceId
        _selectedPickupLocationId.value = service.pickupLocations.firstOrNull()?.id
        val today = LocalDate.now(AppClock.clock)
        val dates = service.scheduleByDate.keys.sorted()
        val defaultDate = pickBestDate(dates, today)
        selectTakeawayDate(defaultDate)
        recalculateCartPricing()
    }

    fun selectTakeawayDate(date: LocalDate?) {
        _selectedTakeawayDate.value = date
        val service = currentTakeawayService()
        val dayMeals = date?.let { service?.scheduleByDate?.get(it) }.orEmpty()
        val defaultMeal = dayMeals.firstOrNull()
        _selectedTakeawayMealTypeId.value = defaultMeal?.id
        _selectedTimeSlotId.value = defaultMeal?.timeSlots?.firstOrNull { it.bookable }?.id
            ?: defaultMeal?.timeSlots?.firstOrNull()?.id
    }

    fun selectTakeawayMealType(mealTypeId: Int) {
        _selectedTakeawayMealTypeId.value = mealTypeId
        val service = currentTakeawayService()
        val date = _selectedTakeawayDate.value
        val slots = date?.let { service?.scheduleByDate?.get(it) }
            ?.firstOrNull { it.id == mealTypeId }
            ?.timeSlots
            .orEmpty()
        _selectedTimeSlotId.value = slots.firstOrNull { it.bookable }?.id ?: slots.firstOrNull()?.id
    }

    fun selectPickupLocation(locationId: Int) {
        _selectedPickupLocationId.value = locationId
    }

    fun selectTimeSlot(slotId: Int) {
        _selectedTimeSlotId.value = slotId
    }

    fun setOrderNote(note: String) {
        _orderNote.value = note
    }

    fun toggleFavoriteService(service: MensaTakeawayService) {
        viewModelScope.launch {
            runCatching { repository.toggleFavoriteService(service.id, service.isFavorite) }.fold(
                onSuccess = { fetchTakeawayServicesInternal() },
                onFailure = { _events.send(MensaEvent.SyncFailed(it)) },
            )
        }
    }

    private fun recalculateCartPricing() {
        val service = currentTakeawayService() ?: return
        val qtyMap = _cartDishes.value.mapValues { it.value.quantity }.filterValues { it > 0 }
        if (qtyMap.isEmpty()) {
            _cartPricing.value = Loadable.NotYetLoaded
            return
        }
        if (session.value == null) return
        pricingJob?.cancel()
        pricingJob = viewModelScope.launch {
            runCatching {
                repository.calculateCartPricing(
                    serviceId = service.id,
                    ageRangeId = service.ageRangeId,
                    dishesQty = qtyMap,
                )
            }.onSuccess { pricing ->
                _cartPricing.value = Loadable.Loaded(pricing)
            }
        }
    }

    fun submitTakeawayOrder() {
        if (_submittingOrder.value) return
        val service = currentTakeawayService() ?: return
        val date = _selectedTakeawayDate.value ?: return
        val mealTypeId = _selectedTakeawayMealTypeId.value ?: return
        val turnId = _selectedTimeSlotId.value ?: return
        val qtyMap = _cartDishes.value.mapValues { it.value.quantity }.filterValues { it > 0 }
        if (qtyMap.isEmpty()) return

        _submittingOrder.value = true
        viewModelScope.launch {
            runCatching {
                repository.submitTakeawayOrder(
                    serviceId = service.id,
                    menuId = service.menuId,
                    mealTypeId = mealTypeId,
                    turnId = turnId,
                    date = date,
                    placeId = _selectedPickupLocationId.value,
                    dishesQty = qtyMap,
                    userNote = _orderNote.value,
                )
            }.fold(
                onSuccess = { (_, code) ->
                    clearCart()
                    _orderNote.value = ""
                    runCatching { fetchReservationsInternal() }
                    runCatching { repository.refreshAccountInfo() }
                    _backStack.value = listOf(MensaSubPage.Home, MensaSubPage.Reservations)
                    _events.send(MensaEvent.OrderSubmitted(code))
                },
                onFailure = { _events.send(MensaEvent.OrderFailed(it)) },
            )
            _submittingOrder.value = false
        }
    }

    fun openReservations() {
        if (session.value == null) {
            openLoginThen(MensaSubPage.Reservations)
            return
        }
        enterReservations()
    }

    private fun enterReservations() {
        push(MensaSubPage.Reservations)
        refreshReservations()
    }

    fun refreshReservations() {
        viewModelScope.launch { fetchReservationsInternal() }
    }

    private suspend fun fetchReservationsInternal() {
        _reservationsStatus.value = SyncStatus.Refreshing
        runCatching { repository.getTakeawayReservations() }.fold(
            onSuccess = { list ->
                _reservations.value = Loadable.Loaded(list)
                _reservationsStatus.value = SyncStatus.Idle
            },
            onFailure = { cause ->
                _reservationsStatus.value = if (cause is MensaNotLoggedInException) SyncStatus.Idle else SyncStatus.Failed(cause)
            },
        )
    }

    fun openReservationDetail(reservation: MensaReservation) {
        push(MensaSubPage.ReservationDetail(reservation.id))
    }

    fun cancelReservation(reservation: MensaReservation) {
        if (_cancellingId.value != null) return
        _cancellingId.value = reservation.id
        viewModelScope.launch {
            runCatching { repository.cancelTakeawayReservation(reservation.id) }.fold(
                onSuccess = {
                    runCatching { fetchReservationsInternal() }
                    _events.send(MensaEvent.OrderCancelled)
                },
                onFailure = { _events.send(MensaEvent.CancelFailed(it)) },
            )
            _cancellingId.value = null
        }
    }

    private fun currentTakeawayService(): MensaTakeawayService? =
        _takeawayServices.value.valueOrNull()?.firstOrNull { it.id == _selectedServiceId.value }

    private fun pickBestDate(dates: List<LocalDate>, today: LocalDate): LocalDate? =
        dates.firstOrNull { !it.isBefore(today) } ?: dates.lastOrNull()
}

/** Cart entry pairing a dish from the daily menu with its selected quantity. */
data class MensaCartEntry(
    val dish: MensaDish,
    val quantity: Int,
)
