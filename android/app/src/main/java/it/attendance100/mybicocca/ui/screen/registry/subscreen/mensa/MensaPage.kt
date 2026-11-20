package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa

import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.os.currentLocale
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.time.AppClock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuKind
import it.attendance100.mybicocca.domain.model.mensa.identity
import it.attendance100.mybicocca.domain.model.mensa.MensaReservation
import it.attendance100.mybicocca.domain.repository.MensaServerMessageException
import it.attendance100.mybicocca.ui.component.modal.LockSheetWhile
import it.attendance100.mybicocca.ui.component.modal.SheetConfirmPage
import it.attendance100.mybicocca.ui.component.modal.SheetHeaderSpec
import it.attendance100.mybicocca.ui.component.modal.SheetOutcome
import it.attendance100.mybicocca.ui.component.modal.SheetPager
import it.attendance100.mybicocca.ui.component.modal.SheetResultPage
import it.attendance100.mybicocca.ui.component.modal.rememberLastNonNull
import it.attendance100.mybicocca.ui.navigation.DisposableEffectOnPop
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaAccountAction
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaAccountEditPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaAccountPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaAllergensPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.LocalMensaShared
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaAccountDeletePage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaDishPagerPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaMenuPickerPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaPlacePickerPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaMenuSetupPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaSharedScopes
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.toDishDisplayName
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaFoodCatalogPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaHubPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaLoginPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaMenuPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaOrderSummaryPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaReservationDetailPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaReservationsPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaTakeawayPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaAccountField
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaSubPage

/**
 * Full Mensa experience — daily/weekly canteen menus, dish allergens & nutrition, RistoCloud account
 * wallet, and take-away meal ordering/reservations — hosted inside a single morphing [SheetPager].
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MensaPage(
    viewModel: MensaViewModel,
) {
    val strLoginFailed = stringResource(R.string.mensa_login_failed)
    val strOrderSubmittedTitle = stringResource(R.string.mensa_takeaway_submitted_title)
    val strOrderSubmittedBody = stringResource(R.string.mensa_takeaway_submitted_body)
    val strOrderFailed = stringResource(R.string.mensa_takeaway_failed)
    val strOrderCancelledTitle = stringResource(R.string.mensa_order_cancelled_title)
    val strOrderCancelledBody = stringResource(R.string.mensa_order_cancelled_body)
    val strOrderCancelFailed = stringResource(R.string.mensa_order_cancel_failed)
    val strServicesFailed = stringResource(R.string.mensa_services_load_failed)
    val strAccountUpdatedTitle = stringResource(R.string.mensa_account_updated_title)
    val strAccountUpdatedBody = stringResource(R.string.mensa_account_updated_body)
    val strAccountFailed = stringResource(R.string.mensa_account_update_failed)
    val strDeletionTitle = stringResource(R.string.mensa_delete_requested_title)
    val strDeletionBody = stringResource(R.string.mensa_delete_requested_body)
    val strDeletionFailed = stringResource(R.string.mensa_delete_failed)
    val todayLabel = stringResource(R.string.mensa_today)
    val tomorrowLabel = stringResource(R.string.mensa_tomorrow)
    val locale = currentLocale()
    val context = LocalContext.current
    val haptic = rememberHapticManager()

    DisposableEffectOnPop { viewModel.resetNavigation() }

    // Owned here, not by the menu page, which leaves the composition while the carousel is open.
    val menuListState = rememberLazyListState()

    var outcome by remember { mutableStateOf<SheetOutcome?>(null) }
    var outcomeTitle by remember { mutableStateOf("") }
    var pendingCancel by remember { mutableStateOf<MensaReservation?>(null) }

    val backStack by viewModel.backStack.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()
    val menus by viewModel.menus.collectAsStateWithLifecycle()
    val menusStatus by viewModel.menusStatus.collectAsStateWithLifecycle()
    val selectedPlace by viewModel.selectedPlace.collectAsStateWithLifecycle()
    val orderAvailability by viewModel.orderAvailability.collectAsStateWithLifecycle()
    val signInHintDismissed by viewModel.signInHintDismissed.collectAsStateWithLifecycle()
    val cartPricingError by viewModel.cartPricingError.collectAsStateWithLifecycle()
    val bookableDates by viewModel.bookableDates.collectAsStateWithLifecycle()
    val selectedMenuId by viewModel.selectedMenuId.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedMealTypeId by viewModel.selectedMealTypeId.collectAsStateWithLifecycle()
    val schedule by viewModel.currentSchedule.collectAsStateWithLifecycle()
    val scheduleStatus by viewModel.scheduleStatus.collectAsStateWithLifecycle()
    val pager by viewModel.pager.collectAsStateWithLifecycle()
    val dishDetails by viewModel.dishDetails.collectAsStateWithLifecycle()
    val deletionCheck by viewModel.deletionCheck.collectAsStateWithLifecycle()
    val loginSubmitting by viewModel.loginSubmitting.collectAsStateWithLifecycle()
    val takeawayServices by viewModel.takeawayServices.collectAsStateWithLifecycle()
    val takeawayStatus by viewModel.takeawayStatus.collectAsStateWithLifecycle()
    val selectedPickupLocationId by viewModel.selectedPickupLocationId.collectAsStateWithLifecycle()
    val selectedTimeSlotId by viewModel.selectedTimeSlotId.collectAsStateWithLifecycle()
    val orderNote by viewModel.orderNote.collectAsStateWithLifecycle()
    val cartDishes by viewModel.cartDishes.collectAsStateWithLifecycle()
    val cartPricing by viewModel.cartPricing.collectAsStateWithLifecycle()
    val submittingOrder by viewModel.submittingOrder.collectAsStateWithLifecycle()
    val reservations by viewModel.reservations.collectAsStateWithLifecycle()
    val reservationsStatus by viewModel.reservationsStatus.collectAsStateWithLifecycle()
    val cancellingId by viewModel.cancellingId.collectAsStateWithLifecycle()
    val allergenPreferences by viewModel.allergenPreferences.collectAsStateWithLifecycle()
    val allergens by viewModel.allergens.collectAsStateWithLifecycle()
    val allergensStatus by viewModel.allergensStatus.collectAsStateWithLifecycle()
    val catalog by viewModel.catalog.collectAsStateWithLifecycle()
    val catalogStatus by viewModel.catalogStatus.collectAsStateWithLifecycle()
    val eligibility by viewModel.eligibility.collectAsStateWithLifecycle()
    val accountBusy by viewModel.accountBusy.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                MensaEvent.LoginSucceeded -> Unit
                is MensaEvent.LoginFailed -> {
                    outcomeTitle = strLoginFailed
                    outcome = SheetOutcome.Error(strLoginFailed, event.cause)
                }
                is MensaEvent.AccountUpdated -> {
                    outcomeTitle = strAccountUpdatedTitle
                    outcome = SheetOutcome.Success(strAccountUpdatedTitle, strAccountUpdatedBody)
                }
                is MensaEvent.AccountUpdateFailed -> {
                    outcomeTitle = strAccountFailed
                    outcome = SheetOutcome.Error(strAccountFailed, event.cause, (event.cause as? MensaServerMessageException)?.serverMessage)
                }
                is MensaEvent.AccountDeletionRequested -> {
                    outcomeTitle = strDeletionTitle
                    outcome = SheetOutcome.Success(strDeletionTitle, event.message ?: strDeletionBody)
                }
                is MensaEvent.AccountDeletionFailed -> {
                    outcomeTitle = strDeletionFailed
                    outcome = SheetOutcome.Error(strDeletionFailed, event.cause, (event.cause as? MensaServerMessageException)?.serverMessage)
                }
                is MensaEvent.OrderSubmitted -> {
                    outcomeTitle = strOrderSubmittedTitle
                    outcome = SheetOutcome.Success(
                        title = strOrderSubmittedTitle,
                        body = strOrderSubmittedBody.format(event.reservationCode ?: "Take-Away"),
                    )
                }
                is MensaEvent.OrderFailed -> {
                    outcomeTitle = strOrderFailed
                    outcome = SheetOutcome.Error(strOrderFailed, event.cause, (event.cause as? MensaServerMessageException)?.serverMessage)
                }
                MensaEvent.OrderCancelled -> {
                    outcomeTitle = strOrderCancelledTitle
                    outcome = SheetOutcome.Success(strOrderCancelledTitle, strOrderCancelledBody)
                }
                is MensaEvent.CancelFailed -> {
                    outcomeTitle = strOrderCancelFailed
                    outcome = SheetOutcome.Error(strOrderCancelFailed, event.cause)
                }
                is MensaEvent.SyncFailed -> {
                    outcomeTitle = strServicesFailed
                    outcome = SheetOutcome.Error(strServicesFailed, event.cause)
                }
            }
        }
    }

    val current = backStack.last()
    val depth = backStack.lastIndex
    val busy = loginSubmitting || submittingOrder || accountBusy
    val reservationList = reservations.valueOrNull().orEmpty()
    val menuContext = remember(menus, selectedPlace, selectedMenuId, selectedDate, selectedMealTypeId, schedule, todayLabel, tomorrowLabel, locale) {
        val today = LocalDate.now(AppClock.clock)
        val menuIdentity = menus.valueOrNull()?.firstOrNull { it.id == selectedMenuId }?.identity()
        // A canteen's usual menu goes without saying; the place and a variant of it are worth showing.
        val canteen = listOfNotNull(
            selectedPlace?.displayName,
            menuIdentity?.displayName?.takeIf { selectedPlace == null || menuIdentity.kind == MensaMenuKind.GLUTEN_FREE },
        ).distinct().joinToString(" · ").ifBlank { null }
        val day = selectedDate?.let {
            when (it) {
                today -> todayLabel
                today.plusDays(1) -> tomorrowLabel
                else -> it.format(DateTimeFormatter.ofPattern("EEE d MMM", locale)).replaceFirstChar { c -> c.titlecase(locale) }
            }
        }
        val meal = selectedDate?.let { schedule.valueOrNull()?.get(it) }
            ?.mealTypes?.firstOrNull { it.id == selectedMealTypeId }?.name
        listOfNotNull(canteen, day, meal).joinToString(" · ").ifBlank { null }
    }

    LockSheetWhile(busy)

    // A different canteen, day or meal is a different list, so it starts from the top.
    LaunchedEffect(selectedMenuId, selectedDate, selectedMealTypeId) { menuListState.requestScrollToItem(0) }

    // A dish opened from the menu is the menu page with its carousel up, not a page of its own, so
    // the header and the course strip stay where they are while the content underneath changes.
    val menuCarousel = current == MensaSubPage.DishPager && pager?.menuRows != null
    val currentDisplay = MensaDisplay.Page(current, depth, menuCarousel)

    val display: MensaDisplay = when {
        outcome != null -> MensaDisplay.Outcome
        pendingCancel != null -> MensaDisplay.ConfirmCancel
        else -> currentDisplay
    }

    val backTo: MensaDisplay? = when (display) {
        MensaDisplay.Outcome -> if (pendingCancel != null) MensaDisplay.ConfirmCancel else currentDisplay
        MensaDisplay.ConfirmCancel -> currentDisplay
        // The carousel closes through its own back handler, which scrubs the transition inside the page.
        is MensaDisplay.Page -> if (depth > 0 && !busy && !display.inMenu) MensaDisplay.Page(backStack[depth - 1], depth - 1) else null
    }
    val shownCancel = rememberLastNonNull(pendingCancel)

    SharedTransitionLayout {
    val sharedScope = this
    SheetPager(
        page = display,
        depth = { displayDepth(it) },
        backTo = backTo,
        onBack = {
            when (display) {
                MensaDisplay.Outcome -> outcome = null
                MensaDisplay.ConfirmCancel -> pendingCancel = null
                is MensaDisplay.Page -> viewModel.back()
            }
        },
        modifier = Modifier.testTag(MensaTestTags.ROOT),
        key = { displayKey(it) },
        header = { target ->
            SheetHeaderSpec(
                title = when (target) {
                    MensaDisplay.Outcome -> outcomeTitle.ifBlank { stringResource(R.string.mensa_title) }
                    MensaDisplay.ConfirmCancel -> stringResource(R.string.mensa_order_cancel_confirmation)
                    is MensaDisplay.Page -> when (val page = if (target.inMenu) MensaSubPage.Menu else target.page) {
                        MensaSubPage.Home -> stringResource(R.string.mensa_title)
                        MensaSubPage.PlacePicker -> stringResource(R.string.mensa_menu_title)
                        MensaSubPage.MenuPicker -> stringResource(R.string.mensa_menu_title)
                        MensaSubPage.Menu -> stringResource(R.string.mensa_menu_title)
                        MensaSubPage.FoodCatalog -> stringResource(R.string.mensa_catalog_title)
                        MensaSubPage.Allergens -> stringResource(R.string.mensa_allergens_title)
                        MensaSubPage.Account -> stringResource(R.string.mensa_account_title)
                        is MensaSubPage.AccountEdit -> when (page.field) {
                            MensaAccountField.PROFILE -> stringResource(R.string.mensa_account_edit_profile)
                            MensaAccountField.EMAIL -> stringResource(R.string.mensa_account_change_email)
                            MensaAccountField.PASSWORD -> stringResource(R.string.mensa_account_change_password)
                        }
                        MensaSubPage.MenuSetup -> stringResource(R.string.mensa_menu_title)
                        MensaSubPage.DishPager -> stringResource(R.string.mensa_menu_title)
                        MensaSubPage.AccountDelete -> stringResource(R.string.mensa_delete_action)
                        MensaSubPage.Login -> stringResource(R.string.mensa_login_title)
                        MensaSubPage.OrderSummary -> stringResource(R.string.mensa_summary_title)
                        MensaSubPage.TakeawayOrder -> stringResource(R.string.mensa_takeaway_title)
                        MensaSubPage.Reservations -> stringResource(R.string.mensa_orders_title)
                        is MensaSubPage.ReservationDetail -> stringResource(R.string.mensa_order_detail_title)
                    }
                },
                subtitle = when (target) {
                    MensaDisplay.Outcome -> stringResource(R.string.mensa_title)
                    MensaDisplay.ConfirmCancel -> shownCancel?.canteenName ?: shownCancel?.code
                    is MensaDisplay.Page -> when (val page = if (target.inMenu) MensaSubPage.Menu else target.page) {
                        MensaSubPage.Home -> session?.walletBalance?.let {
                            stringResource(R.string.mensa_subtitle_wallet, it)
                        } ?: if (session != null) stringResource(R.string.mensa_subtitle_logged_in) else null
                        MensaSubPage.PlacePicker -> stringResource(R.string.mensa_place_picker_subtitle)
                        MensaSubPage.MenuPicker -> stringResource(R.string.mensa_picker_subtitle)
                        MensaSubPage.Menu -> menuContext ?: stringResource(R.string.mensa_menu_subtitle)
                        MensaSubPage.MenuSetup -> stringResource(R.string.mensa_setup_subtitle)
                        MensaSubPage.DishPager -> menuContext ?: stringResource(R.string.mensa_catalog_title)
                        MensaSubPage.AccountDelete -> session?.email
                        MensaSubPage.FoodCatalog -> stringResource(R.string.mensa_catalog_subtitle)
                        MensaSubPage.Allergens -> stringResource(R.string.mensa_allergens_subtitle)
                        MensaSubPage.Account -> session?.email
                        is MensaSubPage.AccountEdit -> session?.email
                        MensaSubPage.Login -> null
                        MensaSubPage.OrderSummary -> stringResource(R.string.mensa_summary_subtitle)
                        MensaSubPage.TakeawayOrder -> stringResource(R.string.mensa_takeaway_subtitle)
                        MensaSubPage.Reservations -> stringResource(R.string.mensa_orders_subtitle)
                        is MensaSubPage.ReservationDetail -> reservationList
                            .firstOrNull { it.id == page.reservationId }
                            ?.statusDescription
                            ?: stringResource(R.string.mensa_orders_title)
                    }
                },
                onSubtitleClick = if ((target as? MensaDisplay.Page)?.let { it.page == MensaSubPage.Menu && !it.inMenu } == true) viewModel::openMenuSetup else null,
                showBack = when (target) {
                    MensaDisplay.Outcome -> false
                    MensaDisplay.ConfirmCancel -> true
                    is MensaDisplay.Page -> !busy && depth > 0
                },
                trailing = when ((target as? MensaDisplay.Page)?.takeUnless { it.inMenu }?.page) {
                    MensaSubPage.Home -> {
                        {
                            MensaAccountAction(
                                session = session,
                                onLogin = viewModel::openLogin,
                                onOpenAccount = viewModel::openAccount,
                            )
                        }
                    }
                    MensaSubPage.Menu -> {
                        {
                            Row {
                                IconButton(onClick = { haptic.tap(); viewModel.openMenuSetup() }, modifier = Modifier.testTag(MensaTestTags.CHANGE_SELECTION_BUTTON)) {
                                    Icon(
                                        imageVector = Icons.Outlined.RestaurantMenu,
                                        contentDescription = stringResource(R.string.mensa_change_selection_action),
                                    )
                                }
                                IconButton(onClick = { haptic.tap(); viewModel.openFoodCatalog() }, modifier = Modifier.testTag(MensaTestTags.CATALOG_BUTTON)) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.List,
                                        contentDescription = stringResource(R.string.mensa_catalog_action),
                                    )
                                }
                            }
                        }
                    }
                    else -> null
                },
            )
        },
    ) { shown ->
        CompositionLocalProvider(LocalMensaShared provides MensaSharedScopes(sharedScope, this)) {
        when (shown) {
            MensaDisplay.Outcome -> outcome?.let { currentOutcome ->
                SheetResultPage(outcome = currentOutcome, onDismiss = { outcome = null })
            }

            MensaDisplay.ConfirmCancel -> pendingCancel?.let { reservation ->
                SheetConfirmPage(
                    body = stringResource(
                        R.string.mensa_order_confirm_cancel,
                        reservation.code.ifBlank { reservation.barcodeValue },
                    ),
                    onConfirm = {
                        pendingCancel = null
                        viewModel.cancelReservation(reservation)
                    },
                    onKeep = { pendingCancel = null },
                    confirmIsPrimary = true,
                )
            }

            is MensaDisplay.Page -> when (val page = shown.page) {
                MensaSubPage.Home -> MensaHubPage(
                    session = session,
                    allergenPreferences = allergenPreferences,
                    onOpenMenu = viewModel::openMenu,
                    onOpenOrders = viewModel::openReservations,
                    onOpenAllergens = viewModel::openAllergens,
                    onOpenWallet = { context.openFreshAndNatural() },
                )

                MensaSubPage.PlacePicker -> MensaPlacePickerPage(onPick = viewModel::pickPlace)

                MensaSubPage.MenuPicker -> MensaMenuPickerPage(
                    menus = menus,
                    menusStatus = menusStatus,
                    place = selectedPlace,
                    onPick = viewModel::pickMenu,
                    onRetryMenus = viewModel::refreshMenus,
                )

                MensaSubPage.MenuSetup -> MensaMenuSetupPage(
                    menus = menus,
                    menusStatus = menusStatus,
                    selectedPlace = selectedPlace,
                    selectedMenuId = selectedMenuId,
                    selectedDate = selectedDate,
                    selectedMealTypeId = selectedMealTypeId,
                    schedule = schedule,
                    bookableDates = bookableDates,
                    onChangePlace = viewModel::openPlacePicker,
                    onChangeMenu = viewModel::openMenuPicker,
                    onSelectDate = viewModel::selectDate,
                    onSelectMealType = viewModel::selectMealType,
                    onConfirm = viewModel::confirmMenuSetup,
                    onRetryMenus = viewModel::refreshMenus,
                )

                MensaSubPage.Menu -> MensaMenuPage(
                    carouselOpen = false,
                    pager = pager,
                    selectedDate = selectedDate,
                    selectedMealTypeId = selectedMealTypeId,
                    schedule = schedule,
                    scheduleStatus = scheduleStatus,
                    cartDishes = cartDishes,
                    allergenPreferences = allergenPreferences,
                    dishDetails = dishDetails,
                    listState = menuListState,
                    onOpenDishes = { dishes, index, rows -> viewModel.openDishPager(dishes, index, rows) },
                    onCloseCarousel = viewModel::back,
                    onRequestDetail = viewModel::requestDishDetail,
                    onRetryDetail = viewModel::retryDishDetail,
                    onAddDishToCart = viewModel::addDishToCart,
                    onDecrementDish = viewModel::decrementCartDish,
                    onSetQuantity = viewModel::setCartQuantity,
                    onOpenOrder = viewModel::openOrderSummary,
                    onRetrySchedule = viewModel::retrySchedule,
                    orderAvailability = orderAvailability,
                    onSignIn = viewModel::openLogin,
                    signInHintDismissed = signInHintDismissed,
                    onDismissSignInHint = viewModel::dismissSignInHint,
                )

                MensaSubPage.OrderSummary -> MensaOrderSummaryPage(
                    cartDishes = cartDishes,
                    cartPricing = cartPricing,
                    signedIn = session != null,
                    pricingError = cartPricingError,
                    onIncrement = viewModel::incrementCartDish,
                    onDecrement = viewModel::decrementCartDish,
                    onBrowseDishes = viewModel::browseDishes,
                    onContinue = viewModel::openTakeawayOrder,
                )

                MensaSubPage.DishPager -> if (shown.inMenu) {
                    // The carousel of the menu page, which hosts it under its own course strip.
                    MensaMenuPage(
                        carouselOpen = true,
                        pager = pager,
                        selectedDate = selectedDate,
                        selectedMealTypeId = selectedMealTypeId,
                        schedule = schedule,
                        scheduleStatus = scheduleStatus,
                        cartDishes = cartDishes,
                        allergenPreferences = allergenPreferences,
                        dishDetails = dishDetails,
                        listState = menuListState,
                        onOpenDishes = { dishes, index, rows -> viewModel.openDishPager(dishes, index, rows) },
                        onCloseCarousel = viewModel::back,
                        onRequestDetail = viewModel::requestDishDetail,
                        onRetryDetail = viewModel::retryDishDetail,
                        onAddDishToCart = viewModel::addDishToCart,
                        onDecrementDish = viewModel::decrementCartDish,
                        onSetQuantity = viewModel::setCartQuantity,
                        onOpenOrder = viewModel::openOrderSummary,
                        onRetrySchedule = viewModel::retrySchedule,
                        orderAvailability = orderAvailability,
                        onSignIn = viewModel::openLogin,
                        signInHintDismissed = signInHintDismissed,
                        onDismissSignInHint = viewModel::dismissSignInHint,
                    )
                } else {
                    pager?.let { request ->
                        MensaDishPagerPage(
                            request = request,
                            details = dishDetails,
                            cartDishes = cartDishes,
                            allergenPreferences = allergenPreferences,
                            onRequestDetail = viewModel::requestDishDetail,
                            onRetryDetail = viewModel::retryDishDetail,
                            onAdd = viewModel::addDishToCart,
                            onDecrement = viewModel::decrementCartDish,
                            canOrder = false,
                        )
                    }
                }

                MensaSubPage.FoodCatalog -> MensaFoodCatalogPage(
                    catalog = catalog,
                    status = catalogStatus,
                    eligibility = eligibility,
                    allergenPreferences = allergenPreferences,
                    onOpenDishes = { dishes, index -> viewModel.openDishPager(dishes, index) },
                    onRetry = viewModel::retryCatalog,
                )

                MensaSubPage.Allergens -> MensaAllergensPage(
                    allergens = allergens,
                    status = allergensStatus,
                    preferences = allergenPreferences,
                    onToggleAllergen = viewModel::setAllergenSelected,
                    onHideMatchingChange = viewModel::setHideMatchingDishes,
                    onClear = viewModel::clearAllergens,
                    onRetry = viewModel::retryAllergens,
                )

                MensaSubPage.Account -> session?.let {
                    MensaAccountPage(
                        session = it,
                        onEdit = viewModel::openAccountEdit,
                        onLogout = viewModel::logout,
                        onDeleteAccount = viewModel::openAccountDelete,
                    )
                }

                MensaSubPage.AccountDelete -> MensaAccountDeletePage(
                    check = deletionCheck,
                    busy = accountBusy,
                    onRequest = viewModel::requestAccountDeletion,
                    onRetry = viewModel::checkAccountDeletion,
                )

                is MensaSubPage.AccountEdit -> session?.let {
                    MensaAccountEditPage(
                        field = page.field,
                        session = it,
                        busy = accountBusy,
                        onSaveProfile = viewModel::updateProfile,
                        onSaveEmail = viewModel::changeEmail,
                        onSavePassword = viewModel::changePassword,
                    )
                }

                MensaSubPage.Login -> MensaLoginPage(
                    submitting = loginSubmitting,
                    onLogin = viewModel::login,
                    onCreateAccount = { context.openFreshAndNatural() },
                )

                MensaSubPage.TakeawayOrder -> MensaTakeawayPage(
                    services = takeawayServices,
                    servicesStatus = takeawayStatus,
                    selectedMenuId = selectedMenuId,
                    selectedPlace = selectedPlace,
                    selectedDate = selectedDate,
                    selectedMealTypeId = selectedMealTypeId,
                    selectedPickupLocationId = selectedPickupLocationId,
                    selectedTimeSlotId = selectedTimeSlotId,
                    cartDishes = cartDishes,
                    cartPricing = cartPricing,
                    orderNote = orderNote,
                    submitting = submittingOrder,
                    pricingError = cartPricingError,
                    walletBalance = session?.walletBalance,
                    onSelectTimeSlot = viewModel::selectTimeSlot,
                    onIncrementDish = viewModel::incrementCartDish,
                    onDecrementDish = viewModel::decrementCartDish,
                    onNoteChange = viewModel::setOrderNote,
                    onBrowseDishes = viewModel::browseDishes,
                    onSubmitOrder = viewModel::submitTakeawayOrder,
                    onRetry = viewModel::refreshTakeawayServices,
                )

                MensaSubPage.Reservations -> MensaReservationsPage(
                    reservations = reservations,
                    status = reservationsStatus,
                    onOpenReservation = viewModel::openReservationDetail,
                    onRetry = viewModel::refreshReservations,
                )

                is MensaSubPage.ReservationDetail -> {
                    val reservation = reservationList.firstOrNull { it.id == page.reservationId }
                    if (reservation != null) {
                        MensaReservationDetailPage(
                            reservation = reservation,
                            isCancelling = cancellingId == reservation.id,
                            onCancel = { pendingCancel = it },
                        )
                    }
                }
            }
        }
        }
    }
    }
}

private sealed interface MensaDisplay {
    /**
     * A back-stack page at [level] (its place in the stack, which sets the direction of the slide).
     * [inMenu] marks a dish carousel that is part of the menu page rather than a page of its own.
     */
    data class Page(val page: MensaSubPage, val level: Int, val inMenu: Boolean = false) : MensaDisplay
    data object ConfirmCancel : MensaDisplay
    data object Outcome : MensaDisplay
}

private fun displayDepth(display: MensaDisplay): Int = when (display) {
    is MensaDisplay.Page -> display.level
    MensaDisplay.ConfirmCancel -> 100
    MensaDisplay.Outcome -> 101
}

private fun displayKey(display: MensaDisplay): String = when (display) {
    is MensaDisplay.Page -> if (display.inMenu) MensaSubPage.Menu.key else display.page.key
    MensaDisplay.ConfirmCancel -> "confirm_cancel"
    MensaDisplay.Outcome -> "outcome"
}
