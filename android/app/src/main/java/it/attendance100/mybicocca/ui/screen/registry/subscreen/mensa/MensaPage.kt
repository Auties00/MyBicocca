package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa

import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.LocalMensaDishAvailability
import it.attendance100.mybicocca.domain.model.mensa.availabilityAt
import it.attendance100.mybicocca.domain.model.mensa.MensaDishAvailability
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import it.attendance100.mybicocca.ui.component.modal.holdSheetFromContent
import androidx.compose.runtime.SideEffect
import androidx.compose.material3.Text
import it.attendance100.mybicocca.core.state.Loadable
import androidx.compose.material3.FilledTonalButton
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import it.attendance100.mybicocca.ui.screen.lock.findFragmentActivity
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
import it.attendance100.mybicocca.ui.component.modal.LocalSheetDismissControl
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
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaSignUpRoute
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaMenuPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaOrderSummaryPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaReservationDetailPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaReservationsPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaTakeawayPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaAccountField
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaSubPage
import androidx.compose.foundation.pager.rememberPagerState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaPersonalizePage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaPersonalizeTab
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaFavouritesPage
import androidx.compose.foundation.rememberScrollState

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
    val strSignUpActivatedTitle = stringResource(R.string.mensa_signup_activated_title)
    val strSignUpActivatedBody = stringResource(R.string.mensa_signup_activated_body)
    val todayLabel = stringResource(R.string.mensa_today)
    val tomorrowLabel = stringResource(R.string.mensa_tomorrow)
    val locale = currentLocale()
    val context = LocalContext.current
    val haptic = rememberHapticManager()

    DisposableEffectOnPop { viewModel.resetNavigation() }

    // Owned here, not by the menu page, which leaves the composition while the carousel is open.
    val menuListState = rememberLazyListState()
    val catalogScroll = rememberScrollState()

    var outcome by remember { mutableStateOf<SheetOutcome?>(null) }
    var outcomeTitle by remember { mutableStateOf("") }
    var pendingCancel by remember { mutableStateOf<MensaReservation?>(null) }

    val backStack by viewModel.backStack.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()
    val menus by viewModel.menus.collectAsStateWithLifecycle()
    val hubTray by viewModel.hubTray.collectAsStateWithLifecycle()
    val today by viewModel.today.collectAsStateWithLifecycle()
    val pickupPoints by viewModel.pickupPoints.collectAsStateWithLifecycle()
    val menusStatus by viewModel.menusStatus.collectAsStateWithLifecycle()
    val selectedPlace by viewModel.selectedPlace.collectAsStateWithLifecycle()
    val orderAvailability by viewModel.orderAvailability.collectAsStateWithLifecycle()
    val signInHintDismissed by viewModel.signInHintDismissed.collectAsStateWithLifecycle()
    val cartPricingError by viewModel.cartPricingError.collectAsStateWithLifecycle()
    val onePerCourse by viewModel.onePerCourse.collectAsStateWithLifecycle()
    val favourites by viewModel.favourites.collectAsStateWithLifecycle()
    val favouriteSettings by viewModel.favouriteSettings.collectAsStateWithLifecycle()
    val favouriteIds = remember(favourites) { favourites.map { it.dishId }.toSet() }
    // Owned here so the sheet header can follow which Personalize page is showing.
    val personalizePager = rememberPagerState { MensaPersonalizeTab.entries.size }
    val onFavourites = personalizePager.targetPage == MensaPersonalizeTab.FAVOURITES.ordinal
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
    val loginPrefillEmail by viewModel.loginPrefillEmail.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                MensaEvent.LoginSucceeded -> Unit
                // Signed in already: the only thing left is to let the password manager keep the
                // password, for this app, so the next sign-in fills itself.
                is MensaEvent.SignedUp -> offerToSavePassword(context, event.email, event.password)
                MensaEvent.SignUpNeedsLogin -> {
                    outcomeTitle = strSignUpActivatedTitle
                    outcome = SheetOutcome.Success(strSignUpActivatedTitle, strSignUpActivatedBody)
                }
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
    // The next booking still to be picked up, which the home shows while there is one.
    val upcomingBooking = remember(reservationList) {
        val today = LocalDate.now(AppClock.clock)
        reservationList
            .filter { (it.active || it.deletable) && !it.provided && it.date?.isBefore(today) == false }
            .minByOrNull { it.date!! }
    }
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

    // The sign-up form asks before it is left with something typed in it, by back or by closing the sheet.
    var signUpDirty by remember { mutableStateOf(false) }
    var leaveSignUp by remember { mutableStateOf<SignUpLeave?>(null) }
    val guardSignUp = current == MensaSubPage.Register && signUpDirty && !busy
    val dismissControl = LocalSheetDismissControl.current
    SideEffect {
        // Material does not take a veto while a drag settles (the sheet would end hidden but open), so
        // a sheet that would ask first is not draggable at all; back and the scrim still ask.
        dismissControl?.gesturesEnabled = !busy && !guardSignUp
        dismissControl?.confirmDismiss = {
            when {
                busy -> false
                guardSignUp -> {
                    leaveSignUp = SignUpLeave.CloseSheet
                    false
                }
                else -> true
            }
        }
    }

    // A different canteen, day or meal is a different list, so it starts from the top.
    LaunchedEffect(selectedMenuId, selectedDate, selectedMealTypeId) { menuListState.requestScrollToItem(0) }

    // A dish opened from the menu is the menu page with its carousel up, not a page of its own, so
    // the header and the course strip stay where they are while the content underneath changes.
    val carouselHost = backStack.getOrNull(depth - 1)
        ?.takeIf { current == MensaSubPage.DishPager && (it == MensaSubPage.Menu || it == MensaSubPage.FoodCatalog) }
    val currentDisplay = MensaDisplay.Page(current, depth, carouselHost)

    val display: MensaDisplay = when {
        outcome != null -> MensaDisplay.Outcome
        pendingCancel != null -> MensaDisplay.ConfirmCancel
        leaveSignUp != null -> MensaDisplay.ConfirmLeaveSignUp
        else -> currentDisplay
    }

    val backTo: MensaDisplay? = when (display) {
        MensaDisplay.Outcome -> if (pendingCancel != null) MensaDisplay.ConfirmCancel else currentDisplay
        MensaDisplay.ConfirmCancel -> currentDisplay
        MensaDisplay.ConfirmLeaveSignUp -> currentDisplay
        is MensaDisplay.Page if guardSignUp -> MensaDisplay.ConfirmLeaveSignUp
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
                MensaDisplay.ConfirmLeaveSignUp -> leaveSignUp = null
                is MensaDisplay.Page -> if (guardSignUp) leaveSignUp = SignUpLeave.Back else viewModel.back()
            }
        },
        modifier = Modifier.testTag(MensaTestTags.ROOT),
        key = { displayKey(it) },
        header = { target ->
            SheetHeaderSpec(
                title = when (target) {
                    MensaDisplay.Outcome -> outcomeTitle.ifBlank { stringResource(R.string.mensa_title) }
                    MensaDisplay.ConfirmCancel -> stringResource(R.string.mensa_order_cancel_confirmation)
                    MensaDisplay.ConfirmLeaveSignUp -> stringResource(R.string.mensa_signup_leave_title)
                    is MensaDisplay.Page -> when (val page = target.host ?: target.page) {
                        MensaSubPage.Home -> stringResource(R.string.mensa_title)
                        MensaSubPage.PlacePicker -> stringResource(R.string.mensa_menu_title)
                        MensaSubPage.MenuPicker -> stringResource(R.string.mensa_menu_title)
                        MensaSubPage.Menu -> stringResource(R.string.mensa_menu_title)
                        MensaSubPage.FoodCatalog -> stringResource(R.string.mensa_catalog_title)
                        MensaSubPage.Allergens -> stringResource(if (onFavourites) R.string.mensa_favourites_title else R.string.mensa_allergens_title)
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
                        MensaSubPage.Register -> stringResource(R.string.mensa_register_title)
                        MensaSubPage.OrderSummary -> stringResource(R.string.mensa_summary_title)
                        MensaSubPage.TakeawayOrder -> stringResource(R.string.mensa_takeaway_title)
                        MensaSubPage.Reservations -> stringResource(R.string.mensa_orders_title)
                        is MensaSubPage.ReservationDetail -> stringResource(R.string.mensa_order_detail_title)
                    }
                },
                subtitle = when (target) {
                    MensaDisplay.Outcome -> stringResource(R.string.mensa_title)
                    MensaDisplay.ConfirmCancel -> shownCancel?.canteenName ?: shownCancel?.code
                    MensaDisplay.ConfirmLeaveSignUp -> stringResource(R.string.mensa_register_subtitle)
                    is MensaDisplay.Page -> when (val page = target.host ?: target.page) {
                        // No balance: Bicocca's canteen does not take payments through the wallet.
                        MensaSubPage.Home -> if (session != null) stringResource(R.string.mensa_subtitle_logged_in) else null
                        MensaSubPage.PlacePicker -> stringResource(R.string.mensa_place_picker_subtitle)
                        MensaSubPage.MenuPicker -> stringResource(R.string.mensa_picker_subtitle)
                        MensaSubPage.Menu -> menuContext ?: stringResource(R.string.mensa_menu_subtitle)
                        MensaSubPage.MenuSetup -> stringResource(R.string.mensa_setup_subtitle)
                        MensaSubPage.DishPager -> menuContext ?: stringResource(R.string.mensa_catalog_title)
                        MensaSubPage.AccountDelete -> session?.email
                        MensaSubPage.FoodCatalog -> stringResource(R.string.mensa_catalog_subtitle)
                        MensaSubPage.Allergens -> stringResource(if (onFavourites) R.string.mensa_favourites_subtitle else R.string.mensa_allergens_subtitle)
                        MensaSubPage.Account -> session?.email
                        is MensaSubPage.AccountEdit -> session?.email
                        MensaSubPage.Login -> stringResource(R.string.mensa_register_subtitle)
                        MensaSubPage.Register -> stringResource(R.string.mensa_register_subtitle)
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
                    MensaDisplay.ConfirmLeaveSignUp -> true
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
                            }
                        }
                    }
                    else -> null
                },
            )
        },
    ) { shown ->
        val orderMenuKind = menus.valueOrNull()?.firstOrNull { it.id == selectedMenuId }?.identity()?.kind
        val dishAvailability: (MensaDish) -> MensaDishAvailability = remember(orderMenuKind, selectedPlace) {
            { dish -> dish.availabilityAt(orderMenuKind, selectedPlace) }
        }
        CompositionLocalProvider(
            LocalMensaShared provides MensaSharedScopes(sharedScope, this),
            LocalMensaDishAvailability provides dishAvailability,
        ) {
        // Only the handle and the header move the sheet: a scroll or drag in a page never closes it.
        Box(modifier = Modifier.holdSheetFromContent()) {
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

            MensaDisplay.ConfirmLeaveSignUp -> SheetConfirmPage(
                body = stringResource(R.string.mensa_signup_leave_body),
                confirmLabel = stringResource(R.string.mensa_signup_leave_confirm),
                keepLabel = stringResource(R.string.mensa_signup_leave_keep),
                onConfirm = {
                    val leave = leaveSignUp
                    leaveSignUp = null
                    signUpDirty = false
                    when (leave) {
                        SignUpLeave.CloseSheet -> dismissControl?.dismiss?.invoke()
                        else -> viewModel.back()
                    }
                },
                onKeep = { leaveSignUp = null },
            )

            // A carousel opened from the menu or the catalog is that page with its carousel up, drawn
            // from the same call, so the page keeps its state and the plate can fly between the two.
            is MensaDisplay.Page -> when (val page = shown.host ?: shown.page) {
                MensaSubPage.Home -> MensaHubPage(
                    session = session,
                    allergenPreferences = allergenPreferences,
                    favouriteCount = favourites.size,
                    upcomingBooking = upcomingBooking,
                    onOpenMenu = viewModel::openMenu,
                    onOpenBooking = viewModel::openReservationDetail,
                    onOpenOrders = viewModel::openReservations,
                    onOpenAllergens = viewModel::openAllergens,
                    onOpenCatalog = viewModel::openFoodCatalog,
                    tray = hubTray,
                    today = today,
                    pickupPoints = pickupPoints,
                    playTrayIntro = !viewModel.hubTrayIntroPlayed,
                    onTrayIntroPlayed = viewModel::markHubTrayIntroPlayed,
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
                    carouselOpen = shown.host != null,
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
                    favouriteIds = favouriteIds,
                    onToggleFavourite = viewModel::toggleFavourite,
                    singlePick = onePerCourse,
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
                    singlePick = onePerCourse,
                )

                MensaSubPage.DishPager -> run {
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
                            favouriteIds = favouriteIds,
                            onToggleFavourite = viewModel::toggleFavourite,
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
                    carouselOpen = shown.host != null,
                    pager = pager,
                    scrollState = catalogScroll,
                    dishDetails = dishDetails,
                    favouriteIds = favouriteIds,
                    onToggleFavourite = viewModel::toggleFavourite,
                    onCloseCarousel = viewModel::back,
                    onRequestDetail = viewModel::requestDishDetail,
                    onRetryDetail = viewModel::retryDishDetail,
                )

                MensaSubPage.Allergens -> MensaPersonalizePage(
                    pagerState = personalizePager,
                    favourites = {
                        MensaFavouritesPage(
                            favourites = favourites,
                            settings = favouriteSettings,
                            onRemove = viewModel::removeFavourite,
                            onBrowseDishes = viewModel::openFoodCatalog,
                            onNotifyChange = viewModel::setFavouriteNotify,
                            onLookaheadChange = viewModel::setFavouriteLookahead,
                            onNotifyTimeChange = viewModel::setFavouriteNotifyTime,
                        )
                    },
                    allergens = {
                        MensaAllergensPage(
                            allergens = allergens,
                            status = allergensStatus,
                            preferences = allergenPreferences,
                            onToggleAllergen = viewModel::setAllergenSelected,
                            onHideMatchingChange = viewModel::setHideMatchingDishes,
                            onClear = viewModel::clearAllergens,
                            onRetry = viewModel::retryAllergens,
                        )
                    },
                )

                MensaSubPage.Account -> session?.let {
                    MensaAccountPage(
                        session = it,
                        onEdit = viewModel::openAccountEdit,
                        onOpenOrders = viewModel::openReservations,
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
                    onCreateAccount = viewModel::openRegister,
                    initialEmail = loginPrefillEmail,
                )

                MensaSubPage.Register -> MensaSignUpRoute(
                    onActivated = viewModel::finishRegistration,
                    onDirtyChange = { signUpDirty = it },
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
                    singlePick = onePerCourse,
                )

                MensaSubPage.Reservations -> MensaReservationsPage(
                    reservations = reservations,
                    status = reservationsStatus,
                    onOpenReservation = viewModel::openReservationDetail,
                    onRetry = viewModel::refreshReservations,
                )

                is MensaSubPage.ReservationDetail -> {
                    val reservation = reservationList.firstOrNull { it.id == page.reservationId }
                    when {
                        reservation != null -> MensaReservationDetailPage(
                            reservation = reservation,
                            pickupPoint = pickupPoints[reservation.id],
                            isCancelling = cancellingId == reservation.id,
                            onCancel = { pendingCancel = it },
                        )
                        reservations !is Loadable.Loaded -> SheetLoadingIndicator(label = stringResource(R.string.mensa_orders_loading))
                        // Gone since the page opened: expired, or removed by a refresh.
                        else -> SheetMessage(
                            icon = Icons.Outlined.EventBusy,
                            title = stringResource(R.string.mensa_order_gone_title),
                            body = stringResource(R.string.mensa_order_gone_body),
                            action = {
                                FilledTonalButton(onClick = { haptic.tap(); viewModel.back() }) {
                                    Text(stringResource(R.string.mensa_order_gone_back))
                                }
                            },
                        )
                    }
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
     * [host] is the page (menu or all dishes) a dish carousel belongs to, when it is part of that
     * page rather than a page of its own.
     */
    data class Page(val page: MensaSubPage, val level: Int, val host: MensaSubPage? = null) : MensaDisplay {
        val inMenu: Boolean get() = host != null
    }
    data object ConfirmCancel : MensaDisplay
    data object ConfirmLeaveSignUp : MensaDisplay
    data object Outcome : MensaDisplay
}

private fun displayDepth(display: MensaDisplay): Int = when (display) {
    is MensaDisplay.Page -> display.level
    MensaDisplay.ConfirmCancel -> 100
    MensaDisplay.ConfirmLeaveSignUp -> 100
    MensaDisplay.Outcome -> 101
}

private fun displayKey(display: MensaDisplay): String = when (display) {
    is MensaDisplay.Page -> display.host?.key ?: display.page.key
    MensaDisplay.ConfirmCancel -> "confirm_cancel"
    MensaDisplay.ConfirmLeaveSignUp -> "confirm_leave_sign_up"
    MensaDisplay.Outcome -> "outcome"
}

/**
 * Asks the user's password manager to keep the new account's password. The prompt saves it for this
 * app (and mybicocca.it, through the asset links), not for compasscloud.it where the form lived.
 * Declining or having no password manager is fine: nothing else depends on it.
 */
private suspend fun offerToSavePassword(context: Context, email: String, password: String) {
    val activity = context.findFragmentActivity() ?: return
    runCatching {
        CredentialManager.create(activity).createCredential(activity, CreatePasswordRequest(id = email, password = password))
    }
}


/** How the user tried to leave a sign-up with something typed in it, so confirming does the same. */
private enum class SignUpLeave { Back, CloseSheet }
