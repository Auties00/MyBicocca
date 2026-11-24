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
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuIds
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuKind
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuSummary
import it.attendance100.mybicocca.domain.model.mensa.MensaPlace
import it.attendance100.mybicocca.domain.model.mensa.MensaReservation
import it.attendance100.mybicocca.domain.model.mensa.asMensaLoginEmail
import it.attendance100.mybicocca.domain.model.mensa.identity
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
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaOrderAvailability
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaSubPage
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaNewAccount
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import it.attendance100.mybicocca.domain.model.mensa.MensaFavourite
import it.attendance100.mybicocca.domain.model.mensa.MensaFavouriteSettings
import it.attendance100.mybicocca.domain.repository.MensaFavouritesRepository
import it.attendance100.mybicocca.data.mensa.MensaFavouritesScheduler
import kotlinx.coroutines.flow.first
import it.attendance100.mybicocca.domain.model.mensa.onePerCourse
import it.attendance100.mybicocca.domain.model.mensa.mensaMenuIdentity

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
    private val favouritesRepository: MensaFavouritesRepository,
    private val favouritesScheduler: MensaFavouritesScheduler,
) : ViewModel() {

    val favourites: StateFlow<List<MensaFavourite>> = favouritesRepository.observeFavourites()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val favouriteSettings: StateFlow<MensaFavouriteSettings> = favouritesRepository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.Eagerly, MensaFavouriteSettings())

    fun toggleFavourite(dish: MensaDish) {
        val isFavourite = favourites.value.none { it.dishId == dish.id }
        viewModelScope.launch { favouritesRepository.setFavourite(MensaFavourite(dish.id, dish.name), isFavourite) }
    }

    fun removeFavourite(favourite: MensaFavourite) {
        viewModelScope.launch { favouritesRepository.setFavourite(favourite, false) }
    }

    /** Turning the reminder on also runs a check straight away, so it is not a day before it says anything. */
    fun setFavouriteNotify(notify: Boolean) {
        viewModelScope.launch {
            favouritesRepository.setNotify(notify)
            favouritesScheduler.apply(notify)
            if (notify) favouritesScheduler.checkNow()
        }
    }

    fun setFavouriteLookahead(days: Int) {
        viewModelScope.launch { favouritesRepository.setLookaheadDays(days) }
    }

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

    private val _selectedPlace = MutableStateFlow<MensaPlace?>(null)
    val selectedPlace: StateFlow<MensaPlace?> = _selectedPlace.asStateFlow()

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

    private val _signInHintDismissed = MutableStateFlow(false)
    val signInHintDismissed: StateFlow<Boolean> = _signInHintDismissed.asStateFlow()

    /** The "sign in to order" note was swiped away; it stays away until the sheet is closed. */
    fun dismissSignInHint() {
        _signInHintDismissed.value = true
    }

    private val _deletionCheck = MutableStateFlow<MensaDeletionCheck>(MensaDeletionCheck.Checking)
    val deletionCheck: StateFlow<MensaDeletionCheck> = _deletionCheck.asStateFlow()

    private val _loginSubmitting = MutableStateFlow(false)
    val loginSubmitting: StateFlow<Boolean> = _loginSubmitting.asStateFlow()

    private val _takeawayServices = MutableStateFlow<Loadable<List<MensaTakeawayService>>>(Loadable.NotYetLoaded)
    val takeawayServices: StateFlow<Loadable<List<MensaTakeawayService>>> = _takeawayServices.asStateFlow()

    private val _takeawayStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val takeawayStatus: StateFlow<SyncStatus> = _takeawayStatus.asStateFlow()

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

    /** Whether the day and meal on the menu can be ordered, which decides if the menu offers to. */
    // Built on first use, as it reads state declared further down.
    val orderAvailability: StateFlow<MensaOrderAvailability> by lazy {
        combine(
            combine(session, _takeawayServices, _selectedPlace, _reservations) { session, services, place, reservations ->
                OrderInputs(session, services, place, reservations.valueOrNull().orEmpty())
            },
            combine(_selectedMenuId, _selectedDate, _selectedMealTypeId, _currentSchedule) { menuId, date, mealId, schedule ->
                val mealName = date?.let { schedule.valueOrNull()?.get(it) }?.mealTypes?.firstOrNull { it.id == mealId }?.name
                MenuInputs(menuId, date, mealId, mealName)
            },
        ) { order, menu ->
            // The canteen takes one booking per day and meal, whatever the menu.
            val booked = order.reservations.firstOrNull { r ->
                (r.active || r.deletable) && !r.provided && r.date == menu.date &&
                    menu.mealName != null && r.mealTypeName.equals(menu.mealName, ignoreCase = true)
            }
            when {
                order.session == null -> MensaOrderAvailability.SignedOut
                order.services !is Loadable.Loaded -> MensaOrderAvailability.Checking
                booked != null -> MensaOrderAvailability.AlreadyBooked(booked)
                else -> orderableService(order.services.value, order.place, menu.menuId, menu.date, menu.mealId)
                    ?.let { MensaOrderAvailability.Available(it) } ?: MensaOrderAvailability.Unavailable
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, MensaOrderAvailability.Checking)
    }

    private data class OrderInputs(
        val session: MensaSession?,
        val services: Loadable<List<MensaTakeawayService>>,
        val place: MensaPlace?,
        val reservations: List<MensaReservation>,
    )

    private data class MenuInputs(val menuId: Int?, val date: LocalDate?, val mealId: Int?, val mealName: String?)

    private val _cartPricingError = MutableStateFlow<String?>(null)

    /** Why the server will not price the cart, usually a combination of dishes it does not allow. */
    val cartPricingError: StateFlow<String?> = _cartPricingError.asStateFlow()

    /** Days the chosen menu can be ordered for, to mark them in the day step. */
    val bookableDates: StateFlow<Set<LocalDate>> = combine(_takeawayServices, _selectedMenuId, _selectedPlace) { services, menuId, place ->
        val service = services.valueOrNull()?.firstOrNull { it.menuId == menuId } ?: return@combine emptySet()
        service.scheduleByDate.filter { (_, meals) ->
            val offered = place?.mealsOf(meals, { meals.indexOf(it) }, place.pickupLocation(service)) ?: meals
            offered.any { meal -> meal.timeSlots.any { it.bookable } }
        }.keys
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

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
        // Keeps the daily check planned for anyone who turned it on, after a reinstall or a cleared job.
        viewModelScope.launch { if (favouritesRepository.observeSettings().first().notify) favouritesScheduler.apply(true) }
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

    /**
     * Back to the hub, and the choice of place, menu, day and meal is forgotten: a sheet that is
     * closed and opened again starts from the questions, not from the answers of last time.
     */
    fun resetNavigation() {
        pendingAfterLogin = null
        _menuContextConfirmed.value = false
        _selectedPlace.value = null
        _signInHintDismissed.value = false
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
        if (_selectedMenuId.value != menuId) clearCart()
        _selectedMenuId.value = menuId
        val menuSummary = list?.firstOrNull { it.id == menuId }
        val today = LocalDate.now(AppClock.clock)
        val cached = scheduleCache[menuId]
        if (cached != null) {
            applyLoadedSchedule(cached, today)
        } else {
            val defaultDate = menuSummary?.activeDays?.let { pickBestDate(it, today) }
            _selectedDate.value = defaultDate
            _selectedMealTypeId.value = defaultMeal(forPlace(menuSummary?.mealTypes.orEmpty()) { it.order }.map { it.id })
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
        val availableMeals = forPlace(day?.mealTypes.orEmpty()) { it.order }
        if (_selectedMealTypeId.value == null || availableMeals.none { it.id == _selectedMealTypeId.value }) {
            _selectedMealTypeId.value = defaultMeal(availableMeals.map { it.id })
        }
    }

    /** Lunch until mid-afternoon, dinner after it, when the canteen serves both. */
    private fun defaultMeal(mealIds: List<Int>): Int? =
        if (mealIds.size > 1 && java.time.LocalTime.now(AppClock.clock).hour >= 15) mealIds.last() else mealIds.firstOrNull()

    /** The meals a place offers out of those a day has: the lunch-only places leave dinner out. */
    private fun <T> forPlace(meals: List<T>, order: (T) -> Int): List<T> =
        _selectedPlace.value?.mealsOf(meals, order) ?: meals

    /** Keeps the chosen meal one the place serves, after the place or the day changes. */
    private fun ensureMealAllowed() {
        val day = _selectedDate.value?.let { _currentSchedule.value.valueOrNull()?.get(it) } ?: return
        val allowed = forPlace(day.mealTypes) { it.order }
        if (allowed.none { it.id == _selectedMealTypeId.value }) _selectedMealTypeId.value = allowed.firstOrNull()?.id
    }

    fun selectDate(date: LocalDate) {
        if (_selectedDate.value != date) clearCart()
        _selectedDate.value = date
        ensureMealAllowed()
    }

    fun selectMealType(mealTypeId: Int) {
        if (_selectedMealTypeId.value != mealTypeId) clearCart()
        _selectedMealTypeId.value = mealTypeId
    }

    /**
     * Opens the menu, asking where first (a canteen or a bar), then which menu when the place has
     * more than one, then the day and meal, unless they were already chosen.
     */
    fun openMenu() {
        push(if (_menuContextConfirmed.value) MensaSubPage.Menu else MensaSubPage.PlacePicker)
    }

    /** The day and meal step, reached from the menu's header to change either. */
    fun openMenuSetup() {
        push(MensaSubPage.MenuSetup)
    }

    fun openPlacePicker() {
        push(MensaSubPage.PlacePicker)
    }

    fun openMenuPicker() {
        push(MensaSubPage.MenuPicker)
    }

    /** The menu a place opens on: its only one, or for a canteen the gluten-free one for those who avoid gluten. */
    private fun defaultMenuFor(place: MensaPlace): Int {
        if (!place.hasMenuChoice) return place.menuIds.first()
        val available = _menus.value.valueOrNull()?.map { it.id }.orEmpty()
        val glutenFree = place.menuIds.firstOrNull { id -> id != MensaMenuIds.CANTEEN && (available.isEmpty() || id in available) }
        val avoidsGluten = allergenPreferences.value.avoidsGluten(_allergens.value.valueOrNull().orEmpty())
        return if (avoidsGluten && glutenFree != null) glutenFree else MensaMenuIds.CANTEEN
    }

    /**
     * Chooses where, and moves on: to the menu when the place has more than one, otherwise to the
     * day. Each step is pushed on top of the last, so back returns to the one before. From the day
     * step (changing the place) it comes back there, unless the menu it had no longer exists at the
     * new place.
     */
    fun pickPlace(place: MensaPlace) {
        val before = _selectedMenuId.value
        _selectedPlace.value = place
        val keepMenu = before != null && before in place.menuIds
        selectMenu(if (keepMenu) before!! else defaultMenuFor(place))
        ensureMealAllowed()
        val stack = _backStack.value
        if (stack.lastOrNull() != MensaSubPage.PlacePicker) return
        val setupAt = stack.lastIndexOf(MensaSubPage.MenuSetup)
        _backStack.value = when {
            place.hasMenuChoice && (!keepMenu || setupAt < 0) -> stack + MensaSubPage.MenuPicker
            setupAt >= 0 -> stack.take(setupAt + 1)
            else -> stack + MensaSubPage.MenuSetup
        }
    }

    /**
     * Chooses a menu of the place and moves on to the day, on top of the menu step. From the day
     * step (changing the menu) it goes back to it.
     */
    fun pickMenu(menuId: Int) {
        selectMenu(menuId)
        ensureMealAllowed()
        val stack = _backStack.value
        if (stack.lastOrNull() != MensaSubPage.MenuPicker) return
        val setupAt = stack.lastIndexOf(MensaSubPage.MenuSetup)
        _backStack.value = if (setupAt >= 0) stack.take(setupAt + 1) else stack + MensaSubPage.MenuSetup
    }

    /** Leaves the selection steps for the menu, which takes their place above wherever it began. */
    fun confirmMenuSetup() {
        _menuContextConfirmed.value = true
        val steps = setOf(MensaSubPage.PlacePicker, MensaSubPage.MenuPicker, MensaSubPage.MenuSetup)
        val base = _backStack.value.takeWhile { it !in steps }
        _backStack.value = if (base.lastOrNull() == MensaSubPage.Menu) base else base + MensaSubPage.Menu
    }

    /**
     * Pre-selects a place from the Campus Map and opens the sheet on the day step, since the place
     * and its menu are known. Someone who avoids gluten gets the gluten-free menu in the canteen's place.
     */
    fun preselectPlace(place: MensaPlace) {
        _selectedPlace.value = place
        selectMenu(defaultMenuFor(place))
        ensureMealAllowed()
        _menuContextConfirmed.value = false
        _backStack.value = listOf(MensaSubPage.Home, MensaSubPage.MenuSetup)
    }

    /** Opens the Menu so dishes can be added to the order; goes back when it is already underneath. */
    fun browseDishes() {
        val stack = _backStack.value
        val menuAt = stack.lastIndexOf(MensaSubPage.Menu)
        if (menuAt >= 0) _backStack.value = stack.take(menuAt + 1) else push(MensaSubPage.Menu)
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

    fun openDishPager(dishes: List<MensaDish>, startIndex: Int, menuRows: List<Int>? = null) {
        if (dishes.isEmpty()) return
        val index = startIndex.coerceIn(dishes.indices)
        _pager.value = MensaDishPagerRequest(dishes, index, menuRows?.takeIf { it.size == dishes.size })
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

    fun openRegister() {
        push(MensaSubPage.Register)
    }

    private val _loginPrefillEmail = MutableStateFlow<String?>(null)

    /** The email of an account just created, so the login page does not ask for it again. */
    val loginPrefillEmail: StateFlow<String?> = _loginPrefillEmail.asStateFlow()

    /**
     * The sign-up form has finished. With the credentials typed in it, the new account is signed in
     * straight away and the login page is skipped; without them (or if Compass refuses them), the
     * login page stays, with the email filled in.
     */
    fun finishRegistration(account: MensaNewAccount?) {
        if (_backStack.value.lastOrNull() == MensaSubPage.Register) {
            _backStack.value = _backStack.value.dropLast(1)
        }
        _loginPrefillEmail.value = account?.email
        if (account == null) {
            viewModelScope.launch { _events.send(MensaEvent.SignUpNeedsLogin) }
            return
        }
        if (_loginSubmitting.value) return
        _loginSubmitting.value = true
        viewModelScope.launch {
            runCatching { repository.login(account.email.asMensaLoginEmail(), account.password) }.fold(
                onSuccess = {
                    if (_backStack.value.lastOrNull() == MensaSubPage.Login) {
                        _backStack.value = _backStack.value.dropLast(1)
                    }
                    _loginPrefillEmail.value = null
                    _events.send(MensaEvent.SignedUp(account.email, account.password))
                    runCatching { fetchTakeawayServicesInternal() }
                    runCatching { fetchReservationsInternal() }
                    pendingAfterLogin = null
                },
                onFailure = { _events.send(MensaEvent.SignUpNeedsLogin) },
            )
            _loginSubmitting.value = false
        }
    }

    private fun openLoginThen(next: MensaSubPage) {
        pendingAfterLogin = next
        openLogin()
    }

    fun login(email: String, password: String) {
        if (_loginSubmitting.value || email.isBlank() || password.isBlank()) return
        _loginSubmitting.value = true
        viewModelScope.launch {
            runCatching { repository.login(email.asMensaLoginEmail(), password) }.fold(
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

    /** Whether the selected menu takes one dish per course, so dishes are picked rather than counted. */
    val onePerCourse: StateFlow<Boolean> = combine(_menus, _selectedMenuId) { menus, id -> isOnePerCourse(menus.valueOrNull(), id) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private fun isOnePerCourse(menus: List<MensaMenuSummary>?, id: Int?): Boolean {
        id ?: return false
        val identity = menus?.firstOrNull { it.id == id }?.identity() ?: mensaMenuIdentity(id, "")
        return identity.kind.onePerCourse
    }

    fun addDishToCart(dish: MensaDish) {
        val current = _cartDishes.value.toMutableMap()
        if (isOnePerCourse(_menus.value.valueOrNull(), _selectedMenuId.value)) {
            // Picking a second dish of a course swaps it in for the first, as the server would
            // refuse both.
            if (dish.courseId != null) current.values.removeAll { it.dish.courseId == dish.courseId }
            current[dish.id] = MensaCartEntry(dish = dish, quantity = 1)
            _cartDishes.value = current
            recalculateCartPricing()
            return
        }
        val existing = current[dish.id]
        val nextQty = ((existing?.quantity ?: 0) + 1).coerceAtMost(MENSA_MAX_CART_QUANTITY)
        current[dish.id] = MensaCartEntry(dish = dish, quantity = nextQty)
        _cartDishes.value = current
        recalculateCartPricing()
    }

    fun incrementCartDish(dishId: Int) {
        if (isOnePerCourse(_menus.value.valueOrNull(), _selectedMenuId.value)) return
        val existing = _cartDishes.value[dishId] ?: return
        val current = _cartDishes.value.toMutableMap()
        current[dishId] = existing.copy(quantity = (existing.quantity + 1).coerceAtMost(MENSA_MAX_CART_QUANTITY))
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

    /** Sets how many of a dish are in the order; zero takes it out. */
    fun setCartQuantity(dish: MensaDish, quantity: Int) {
        val current = _cartDishes.value.toMutableMap()
        if (quantity <= 0) {
            current.remove(dish.id)
        } else {
            current[dish.id] = MensaCartEntry(dish = dish, quantity = quantity.coerceAtMost(MENSA_MAX_CART_QUANTITY))
        }
        _cartDishes.value = current
        recalculateCartPricing()
    }

    fun clearCart() {
        _cartDishes.value = emptyMap()
        _cartPricing.value = Loadable.NotYetLoaded
        _cartPricingError.value = null
    }

    /** Opens the order recap. It needs no account, so a signed-out user can review the order first. */
    fun openOrderSummary() {
        push(MensaSubPage.OrderSummary)
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
        if (_takeawayServices.value !is Loadable.Loaded) refreshTakeawayServices() else applyOrderContext()
        recalculateCartPricing()
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
                applyOrderContext()
                recalculateCartPricing()
            },
            onFailure = { cause ->
                _takeawayStatus.value = if (cause is MensaNotLoggedInException) SyncStatus.Idle else SyncStatus.Failed(cause)
            },
        )
    }

    /** The booking service of the menu being browsed, if it takes bookings. */
    private fun currentTakeawayService(): MensaTakeawayService? =
        _takeawayServices.value.valueOrNull()?.firstOrNull { it.menuId == _selectedMenuId.value }

    /** The service that can take an order for [menuId] on [date] at [mealId], if any; else null. */
    private fun orderableService(
        services: List<MensaTakeawayService>,
        place: MensaPlace?,
        menuId: Int?,
        date: LocalDate?,
        mealId: Int?,
    ): MensaTakeawayService? {
        val service = services.firstOrNull { it.menuId == menuId } ?: return null
        val dayMeals = date?.let { service.scheduleByDate[it] }.orEmpty()
        val offered = place?.mealsOf(dayMeals, { dayMeals.indexOf(it) }, place.pickupLocation(service)) ?: dayMeals
        val meal = offered.firstOrNull { it.id == mealId } ?: return null
        return service.takeIf { meal.timeSlots.any { it.bookable } }
    }

    /** Takes the pickup place and first open time slot from what the menu was set to. */
    private fun applyOrderContext() {
        val service = currentTakeawayService() ?: return
        _selectedPickupLocationId.value = _selectedPlace.value?.pickupLocation(service)?.id
            ?: service.pickupLocations.firstOrNull()?.id
        val slots = _selectedDate.value?.let { service.scheduleByDate[it] }
            ?.firstOrNull { it.id == _selectedMealTypeId.value }
            ?.timeSlots
            .orEmpty()
        if (slots.none { it.id == _selectedTimeSlotId.value }) {
            _selectedTimeSlotId.value = slots.firstOrNull { it.bookable }?.id ?: slots.firstOrNull()?.id
        }
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
            _cartPricingError.value = null
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
            }.fold(
                onSuccess = { pricing ->
                    _cartPricing.value = Loadable.Loaded(pricing)
                    _cartPricingError.value = null
                },
                onFailure = { cause ->
                    _cartPricing.value = Loadable.NotYetLoaded
                    // The first paragraph names what is wrong; the rest repeats the service's rules.
                    _cartPricingError.value = (cause as? MensaServerMessageException)?.serverMessage
                        ?.substringBefore("\n\n")?.trim()?.takeIf { it.isNotEmpty() }
                },
            )
        }
    }

    fun submitTakeawayOrder() {
        if (_submittingOrder.value) return
        val service = currentTakeawayService() ?: return
        val date = _selectedDate.value ?: return
        val mealTypeId = _selectedMealTypeId.value ?: return
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

    private fun pickBestDate(dates: List<LocalDate>, today: LocalDate): LocalDate? =
        dates.firstOrNull { !it.isBefore(today) } ?: dates.lastOrNull()
}

/** Cart entry pairing a dish from the daily menu with its selected quantity. */
/** Most portions of one dish an order can hold. */
const val MENSA_MAX_CART_QUANTITY = 99

data class MensaCartEntry(
    val dish: MensaDish,
    val quantity: Int,
)
