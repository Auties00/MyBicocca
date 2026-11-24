package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaDishDetailState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaDishPagerRequest
import kotlin.coroutines.cancellation.CancellationException
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import kotlin.math.abs
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Animatable
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaOrderAvailability
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenPreferences
import it.attendance100.mybicocca.domain.model.mensa.MensaDaySchedule
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.domain.model.mensa.allergenMatch
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.friendlyMessage
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.component.modal.rememberLastNonNull
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaCartEntry
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaDishArtwork
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * The dishes of the chosen menu, day and meal: a course strip that stays put above either the grid
 * of two-column cards or, once a dish is opened, the carousel of plates. The two share the strip and
 * the sheet header, so opening a dish changes only what is under the strip. The strip follows
 * whichever is showing and scrolls it when a course is tapped.
 *
 * The list state is owned by the caller, so the carousel can keep the grid on the dish it is
 * showing while the grid is off screen.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MensaMenuPage(
    carouselOpen: Boolean,
    pager: MensaDishPagerRequest?,
    selectedDate: LocalDate?,
    selectedMealTypeId: Int?,
    schedule: Loadable<Map<LocalDate, MensaDaySchedule>>,
    scheduleStatus: SyncStatus,
    cartDishes: Map<Int, MensaCartEntry>,
    allergenPreferences: MensaAllergenPreferences,
    dishDetails: Map<Int, MensaDishDetailState>,
    listState: LazyListState,
    onOpenDishes: (dishes: List<MensaDish>, startIndex: Int, menuRows: List<Int>) -> Unit,
    onCloseCarousel: () -> Unit,
    onRequestDetail: (Int) -> Unit,
    onRetryDetail: (Int) -> Unit,
    onAddDishToCart: (MensaDish) -> Unit,
    onDecrementDish: (Int) -> Unit,
    onSetQuantity: (MensaDish, Int) -> Unit,
    onOpenOrder: () -> Unit,
    onRetrySchedule: () -> Unit,
    orderAvailability: MensaOrderAvailability,
    onSignIn: () -> Unit,
    signInHintDismissed: Boolean,
    onDismissSignInHint: () -> Unit,
    modifier: Modifier = Modifier,
    favouriteIds: Set<Int> = emptySet(),
    onToggleFavourite: ((MensaDish) -> Unit)? = null,
    singlePick: Boolean = false,
) {
    val scheduleMap = schedule.valueOrNull().orEmpty()
    val availableMeals = selectedDate?.let { scheduleMap[it] }?.mealTypes.orEmpty()
    val activeMeal = availableMeals.firstOrNull { it.id == selectedMealTypeId } ?: availableMeals.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(MensaTestTags.MENU_PAGE),
    ) {
        when {
            scheduleMap.isEmpty() && scheduleStatus is SyncStatus.Failed -> Column(Modifier.verticalScroll(rememberScrollState())) {
                SheetMessage(
                    icon = Icons.Outlined.CloudOff,
                    title = stringResource(R.string.mensa_schedule_load_failed),
                    body = stringResource(scheduleStatus.cause.friendlyMessage()),
                    action = { RetryButton(onClick = onRetrySchedule) },
                )
            }

            schedule is Loadable.NotYetLoaded -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            ) {
                SheetLoadingIndicator(
                    label = stringResource(R.string.mensa_loading_schedule),
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            activeMeal == null || activeMeal.courses.all { it.dishes.isEmpty() } -> EmptyState(
                icon = Icons.Outlined.Restaurant,
                title = stringResource(R.string.mensa_no_dishes_for_day),
                body = "",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
            )

            else -> {
                val layout = remember(activeMeal, allergenPreferences) {
                    buildMenuLayout(activeMeal.courses.filter { it.dishes.isNotEmpty() }, allergenPreferences)
                }
                MenuBrowser(
                    layout = layout,
                    carouselOpen = carouselOpen,
                    pager = pager,
                    listState = listState,
                    cartDishes = cartDishes,
                    allergenPreferences = allergenPreferences,
                    dishDetails = dishDetails,
                    onOpenDishes = onOpenDishes,
                    onCloseCarousel = onCloseCarousel,
                    onRequestDetail = onRequestDetail,
                    onRetryDetail = onRetryDetail,
                    onAddDishToCart = onAddDishToCart,
                    onDecrementDish = onDecrementDish,
                    onSetQuantity = onSetQuantity,
                    onOpenOrder = onOpenOrder,
                    orderAvailability = orderAvailability,
                    onSignIn = onSignIn,
                    signInHintDismissed = signInHintDismissed,
                    onDismissSignInHint = onDismissSignInHint,
                    favouriteIds = favouriteIds,
                    onToggleFavourite = onToggleFavourite,
                    singlePick = singlePick,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun MenuBrowser(
    layout: MensaMenuLayout,
    carouselOpen: Boolean,
    pager: MensaDishPagerRequest?,
    listState: LazyListState,
    cartDishes: Map<Int, MensaCartEntry>,
    allergenPreferences: MensaAllergenPreferences,
    dishDetails: Map<Int, MensaDishDetailState>,
    onOpenDishes: (dishes: List<MensaDish>, startIndex: Int, menuRows: List<Int>) -> Unit,
    onCloseCarousel: () -> Unit,
    onRequestDetail: (Int) -> Unit,
    onRetryDetail: (Int) -> Unit,
    onAddDishToCart: (MensaDish) -> Unit,
    onDecrementDish: (Int) -> Unit,
    onSetQuantity: (MensaDish, Int) -> Unit,
    onOpenOrder: () -> Unit,
    orderAvailability: MensaOrderAvailability,
    onSignIn: () -> Unit,
    signInHintDismissed: Boolean,
    onDismissSignInHint: () -> Unit,
    favouriteIds: Set<Int>,
    onToggleFavourite: ((MensaDish) -> Unit)?,
    singlePick: Boolean,
) {
    val canOrder = orderAvailability is MensaOrderAvailability.Available
    val scope = rememberCoroutineScope()
    var editing by remember { mutableStateOf<MensaDish?>(null) }

    // The request stays on show while the carousel leaves, so it does not empty out mid-exit.
    val shownRequest = rememberLastNonNull(pager.takeIf { carouselOpen })
    val pagerState = key(shownRequest) {
        rememberPagerState(initialPage = shownRequest?.startIndex ?: 0) { shownRequest?.dishes?.size ?: 0 }
    }

    // Every course has a tile; "All" is gone now that the strip scrolls instead of filtering.
    val tiles = remember(layout) {
        // listOf(MensaCourseTile(null, allLabel, null)) +
        layout.items.filterIsInstance<MensaMenuItem.Header>()
            .map { MensaCourseTile(it.course.id, it.course.name, MensaDishArtwork.forCourse(it.course.id)) }
    }
    // The course at the top of the list; the end of the list counts for its last course, which
    // may be too short to ever reach the top.
    val listCourse by remember(layout) {
        derivedStateOf {
            val items = layout.items
            when {
                items.isEmpty() -> null
                !listState.canScrollBackward -> items.first().courseId
                !listState.canScrollForward -> items.last().courseId
                else -> items[listState.firstVisibleItemIndex.coerceIn(items.indices)].courseId
            }
        }
    }
    val carouselCourse = shownRequest?.dishes?.getOrNull(pagerState.currentPage)?.courseId

    // One transition drives grid and carousel, seekable so the back gesture can scrub it. Both
    // pages hang off the same sheet page, so nothing above them moves.
    val seek = remember { SeekableTransitionState(carouselOpen) }
    val transition = rememberTransition(seek, label = "mensa_menu_browser")
    val latestOpen by rememberUpdatedState(carouselOpen)
    val rewindScope = rememberCoroutineScope()
    LaunchedEffect(carouselOpen) {
        if (seek.currentState != carouselOpen || seek.targetState != carouselOpen) seek.animateTo(carouselOpen)
    }
    PredictiveBackHandler(enabled = carouselOpen) { progress ->
        try {
            progress.collect { event -> seek.seekTo(event.progress, targetState = false) }
        } catch (cancelled: CancellationException) {
            rewindScope.launch { seek.animateTo(latestOpen) }
            throw cancelled
        }
        onCloseCarousel()
        // A vetoed back step leaves the carousel open: bring the transition back to it.
        rewindScope.launch {
            withFrameNanos { }
            if (latestOpen != seek.targetState) seek.animateTo(latestOpen)
        }
    }

    val outerShared = LocalMensaShared.current
    Column(modifier = Modifier.fillMaxSize()) {
        MensaCourseStrip(
            tiles = tiles,
            selectedId = if (carouselOpen) carouselCourse else listCourse,
            onSelect = { courseId ->
                if (carouselOpen) {
                    val dishes = shownRequest?.dishes.orEmpty()
                    val target = dishes.indexOfFirst { it.courseId == courseId }
                    if (target >= 0) scope.launch { pagerState.animateScrollToPage(target, animationSpec = mensaScrollSpec()) }
                } else {
                    val target = layout.headerOfCourse[courseId] ?: return@MensaCourseStrip
                    scope.launch { listState.animateToItemSlow(target) }
                }
            },
            modifier = Modifier.padding(bottom = 8.dp),
        )
        transition.AnimatedContent(
            modifier = Modifier.weight(1f),
            transitionSpec = {
                (fadeIn(tween(200)) togetherWith fadeOut(tween(160))).using(SizeTransform(clip = false) { _, _ -> snap() })
            },
            contentAlignment = Alignment.TopStart,
        ) { open ->
            val shared = outerShared?.let { MensaSharedScopes(it.transition, this) }
            CompositionLocalProvider(LocalMensaShared provides shared) {
                if (open && shownRequest != null) {
                    MensaDishCarousel(
                        request = shownRequest,
                        pagerState = pagerState,
                        details = dishDetails,
                        cartDishes = cartDishes,
                        allergenPreferences = allergenPreferences,
                        onRequestDetail = onRequestDetail,
                        onRetryDetail = onRetryDetail,
                        onAdd = onAddDishToCart,
                        onDecrement = onDecrementDish,
                        onFocusMenuRow = { row -> listState.requestScrollToItem(row) },
                        canOrder = canOrder,
                        favouriteIds = favouriteIds,
                        onToggleFavourite = onToggleFavourite,
                        singlePick = singlePick,
                    )
                } else {
                    MenuGrid(
                        layout = layout,
                        listState = listState,
                        cartDishes = cartDishes,
                        allergenPreferences = allergenPreferences,
                        onOpenDishes = onOpenDishes,
                        onAddDishToCart = onAddDishToCart,
                        onDecrementDish = onDecrementDish,
                        onEditQuantity = { editing = it },
                        onOpenOrder = onOpenOrder,
                        orderAvailability = orderAvailability,
                        onSignIn = onSignIn,
                        signInHintDismissed = signInHintDismissed,
                        onDismissSignInHint = onDismissSignInHint,
                        favouriteIds = favouriteIds,
                        onToggleFavourite = onToggleFavourite,
                        singlePick = singlePick,
                    )
                }
            }
        }
    }

    editing?.let { dish ->
        MensaQuantitySheet(
            dishName = dish.name.toDishDisplayName(),
            quantity = cartDishes[dish.id]?.quantity ?: 0,
            onSetQuantity = { onSetQuantity(dish, it) },
            onDismiss = { editing = null },
        )
    }
}

@Composable
private fun MenuGrid(
    layout: MensaMenuLayout,
    listState: LazyListState,
    cartDishes: Map<Int, MensaCartEntry>,
    allergenPreferences: MensaAllergenPreferences,
    onOpenDishes: (dishes: List<MensaDish>, startIndex: Int, menuRows: List<Int>) -> Unit,
    onAddDishToCart: (MensaDish) -> Unit,
    onDecrementDish: (Int) -> Unit,
    onEditQuantity: (MensaDish) -> Unit,
    onOpenOrder: () -> Unit,
    orderAvailability: MensaOrderAvailability,
    onSignIn: () -> Unit,
    signInHintDismissed: Boolean,
    onDismissSignInHint: () -> Unit,
    favouriteIds: Set<Int>,
    onToggleFavourite: ((MensaDish) -> Unit)?,
    singlePick: Boolean,
) {
    val canOrder = orderAvailability is MensaOrderAvailability.Available
    val cartCount = if (canOrder) cartDishes.values.sumOf { it.quantity } else 0
    val hintShown = orderAvailability == MensaOrderAvailability.SignedOut && !signInHintDismissed
    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            // Room under the last row for the floating note, which sits over the list.
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = if (hintShown) 76.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(layout.items, key = { it.key }, contentType = { it::class }) { item ->
                when (item) {
                    is MensaMenuItem.Header -> Text(
                        text = item.course.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp, top = 10.dp),
                    )

                    is MensaMenuItem.DishRow -> Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item.dishes.forEach { dish ->
                            MensaDishPlateCard(
                                dish = dish,
                                art = MensaDishArtwork.forDish(dish),
                                allergenMatch = dish.allergenMatch(allergenPreferences),
                                cartQty = cartDishes[dish.id]?.quantity ?: 0,
                                showAdd = canOrder,
                                onClick = {
                                    onOpenDishes(layout.dishes, layout.dishes.indexOfFirst { it.id == dish.id }, layout.rowOfDish)
                                },
                                onAdd = { onAddDishToCart(dish) },
                                onDecrement = { onDecrementDish(dish.id) },
                                onEditQuantity = { onEditQuantity(dish) },
                                modifier = Modifier.weight(1f),
                                isFavourite = dish.id in favouriteIds,
                                onToggleFavourite = onToggleFavourite?.let { toggle -> { toggle(dish) } },
                                singlePick = singlePick,
                            )
                        }
                        if (item.dishes.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        // The last count stays on show while the bar slides away, so it never flashes "0".
        val footerCount = rememberLastNonNull(cartCount.takeIf { it > 0 }) ?: 0
        AnimatedVisibility(
            visible = cartCount > 0,
            enter = slideInVertically(initialOffsetY = { it }) + expandVertically() + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + shrinkVertically() + fadeOut(),
        ) {
            CartFooterBar(cartCount = footerCount, onOpenOrder = onOpenOrder)
        }
        if (orderAvailability == MensaOrderAvailability.Unavailable || orderAvailability is MensaOrderAvailability.AlreadyBooked) {
            Text(
                text = stringResource(
                    if (orderAvailability is MensaOrderAvailability.AlreadyBooked) R.string.mensa_order_already_booked else R.string.mensa_order_unavailable,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 20.dp),
            )
        }
    }
    if (hintShown) {
        SignInPill(
            onSignIn = onSignIn,
            onDismiss = onDismissSignInHint,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
    }
}

/**
 * Says why the dishes carry no add button when ordering needs an account. It floats over the list,
 * on no background of its own, and goes away when swiped sideways or down.
 */
@Composable
private fun SignInPill(onSignIn: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val x = remember { Animatable(0f) }
    val y = remember { Animatable(0f) }
    val threshold = with(density) { 64.dp.toPx() }
    val away = with(density) { 420.dp.toPx() }

    fun release() {
        scope.launch {
            val dx = x.value
            val dy = y.value
            when {
                abs(dx) > threshold -> {
                    haptic.feather()
                    launch { y.animateTo(0f) }
                    x.animateTo(if (dx > 0) away else -away, tween(180))
                    onDismiss()
                }
                dy > threshold -> {
                    haptic.feather()
                    launch { x.animateTo(0f) }
                    y.animateTo(away, tween(180))
                    onDismiss()
                }
                else -> {
                    launch { x.animateTo(0f, spring()) }
                    y.animateTo(0f, spring())
                }
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = scheme.surfaceContainerHighest,
        shadowElevation = 6.dp,
        modifier = modifier
            .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
            .fillMaxWidth()
            .graphicsLayer {
                translationX = x.value
                translationY = y.value
                alpha = 1f - (maxOf(abs(x.value), y.value) / (threshold * 4)).coerceIn(0f, 0.8f)
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { release() },
                    onDragCancel = { release() },
                ) { change, drag ->
                    change.consume()
                    scope.launch {
                        x.snapTo(x.value + drag.x)
                        y.snapTo((y.value + drag.y).coerceAtLeast(0f))
                    }
                }
            }
            .testTag(MensaTestTags.ORDER_SIGN_IN),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.mensa_order_sign_in_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = { haptic.tap(); onSignIn() },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.heightIn(min = 36.dp),
            ) {
                Text(stringResource(R.string.mensa_order_sign_in))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CartFooterBar(
    cartCount: Int,
    onOpenOrder: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val haptic = rememberHapticManager()
    val bg = if (dark) scheme.primaryContainer else scheme.primary
    val fg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    Button(
        onClick = { haptic.tap(); onOpenOrder() },
        shapes = ButtonDefaults.shapes(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp)
            .heightIn(min = ButtonDefaults.MediumContainerHeight),
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
    ) {
        Icon(Icons.Outlined.ShoppingBag, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = pluralStringResource(R.plurals.mensa_cart_footer, cartCount, cartCount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

internal fun parseHexColorOrNull(hex: String?): Color? {
    val cleaned = hex?.trim()?.removePrefix("#")?.takeIf { it.length == 6 || it.length == 8 } ?: return null
    val value = cleaned.toLongOrNull(16) ?: return null
    val argb = if (cleaned.length == 6) value or 0xFF000000L else value
    return Color(argb)
}

@Preview(name = "Menu page", widthDp = MENSA_PREVIEW_WIDTH, heightDp = MENSA_PREVIEW_HEIGHT)
@Composable
private fun MenuPagePreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaMenuPage(
            carouselOpen = false,
            pager = null,
            selectedDate = MensaPreviewData.today,
            selectedMealTypeId = 3,
            schedule = MensaPreviewData.schedule,
            scheduleStatus = SyncStatus.Idle,
            cartDishes = mapOf(MensaPreviewData.plainDish.id to MensaCartEntry(MensaPreviewData.plainDish, 2)),
            allergenPreferences = MensaPreviewData.glutenSelected,
            dishDetails = emptyMap(),
            listState = rememberLazyListState(),
            onOpenDishes = { _, _, _ -> },
            onCloseCarousel = {},
            onRequestDetail = {},
            onRetryDetail = {},
            onAddDishToCart = {},
            onDecrementDish = {},
            onSetQuantity = { _, _ -> },
            onOpenOrder = {},
            onRetrySchedule = {},
            orderAvailability = MensaOrderAvailability.SignedOut,
            onSignIn = {},
            signInHintDismissed = false,
            onDismissSignInHint = {},
        )
    }
}

@Preview(name = "Sign-in pill", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 120)
@Composable
private fun SignInPillPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) { Box(Modifier.fillMaxSize()) { SignInPill(onSignIn = {}, onDismiss = {}, modifier = Modifier.align(Alignment.BottomCenter)) } }
}
