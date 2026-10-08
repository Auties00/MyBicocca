package it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher

import android.annotation.SuppressLint
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.ProvideHapticManager
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.ui.component.modal.LocalSheetDismissControl
import it.attendance100.mybicocca.ui.component.modal.SheetContainerStyle
import it.attendance100.mybicocca.ui.component.modal.SheetHeader
import it.attendance100.mybicocca.ui.component.modal.SheetHeaderSpec
import it.attendance100.mybicocca.ui.component.modal.SheetPager
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.screen.account.AccountViewModel
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.component.AddAccountCard
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.component.ProfileCard
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.component.RemoveSwipeBackground
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.component.SwipeToRemoveBox
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.component.UndoRemovalBar
import it.attendance100.mybicocca.ui.screen.auth.AuthScreenSheetContent
import it.attendance100.mybicocca.ui.screen.auth.AuthViewModel
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import it.attendance100.mybicocca.ui.theme.PreviewBgLowest
import java.io.File
import java.time.Instant
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.launch

private val CardShape = RoundedCornerShape(28.dp)
private val PageShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
private const val ADD_ACCOUNT_KEY = "__add_account__"

/**
 * Container look of the account switcher sheet ([SheetRoute.AccountSwitcher]): larger corners,
 * an invisible handle spacer and no window insets, since the add-account page grows to the full
 * screen height and lays out its own insets.
 */
val AccountSwitcherSheetStyle = SheetContainerStyle(
    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
    dragHandle = { Box(Modifier.padding(top = AccountSwitcherHandleHeight)) },
    contentWindowInsets = { WindowInsets(0) },
)

private val AccountSwitcherHandleHeight = 16.dp

/**
 * Header of the roster: "Account" over the stored-account count, with the settings shortcut at
 * its trailing edge. The sign-in page has none: it takes the whole sheet.
 */
@Composable
internal fun accountSwitcherHeader(accountCount: Int, onOpenSettings: () -> Unit): SheetHeaderSpec =
    SheetHeaderSpec(
        title = stringResource(R.string.account_switcher_title),
        subtitle = pluralStringResource(R.plurals.account_switcher_count, accountCount, accountCount),
        trailing = { AccountSwitcherSettingsShortcut(onClick = onOpenSettings) },
    )

/**
 * The account switcher sheet page: the roster of stored accounts, and the add-account sign-in
 * that rises over it and grows the sheet to the full screen height.
 *
 * This sheet keeps its own motion instead of the shared [SheetPager]: the whole roster, header
 * included, slides up and out while the sign-in rises from below over a full page height, and the
 * sheet height morphs with it. A pinned header would stay behind and swap its texts instead, and
 * the sign-in has no header at all. Its route therefore declares `sheetHeaderInPage()`, and the
 * roster draws the standard [SheetHeader] itself, so it still looks like every other sheet.
 *
 * Back is predictive on both levels: on the sign-in page the gesture seeks the same transition
 * back to the roster (and rewinds when cancelled); on the roster it falls through to the sheet's
 * native predictive dismiss. While signing in the sheet cannot be swiped away — a swipe, scrim
 * tap or back press returns to the roster instead, so a half-typed sign-in is never dropped
 * silently; while the sign-in request is in flight none of them has any effect.
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AccountSwitcherPage(
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(
        checkNotNull(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val active by viewModel.activeAccount.collectAsStateWithLifecycle()
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val pending by viewModel.pendingRemoval.collectAsStateWithLifecycle()
    val inflight by authViewModel.inflight.collectAsStateWithLifecycle()

    val motion = MaterialTheme.motionScheme
    val control = LocalSheetDismissControl.current

    var isAddingAccount by rememberSaveable { mutableStateOf(false) }

    SideEffect {
        control?.gesturesEnabled = !isAddingAccount
        control?.confirmDismiss = {
            if (isAddingAccount) {
                if (!inflight) isAddingAccount = false
                false
            } else {
                true
            }
        }
    }

    // One seekable transition drives the roster ⇄ sign-in morph, so the back gesture can scrub it.
    val levelState = remember { SeekableTransitionState(isAddingAccount) }
    val levelTransition = rememberTransition(levelState, label = "addAccountTransition")
    LaunchedEffect(isAddingAccount) {
        if (levelState.targetState != isAddingAccount) {
            levelState.animateTo(isAddingAccount, tween(durationMillis = 800, easing = FastOutLinearInEasing))
        }
    }

    // A cancelled gesture cancels the handler's own job, and a second back press cancels a running
    // finish: both the rewind and the finish run in a scope of their own.
    val settleScope = rememberCoroutineScope()
    PredictiveBackHandler(enabled = isAddingAccount && !inflight) { progress ->
        try {
            progress.collect { event -> levelState.seekTo(event.progress, targetState = false) }
        } catch (cancelled: CancellationException) {
            settleScope.launch { levelState.animateTo(true) }
            throw cancelled
        }
        isAddingAccount = false
        settleScope.launch { levelState.animateTo(false) }
    }

    val activeId = active?.id
    val ordered = remember(accounts, activeId) {
        accounts.sortedByDescending { it.id == activeId }
    }
    var lastRemovedName by remember { mutableStateOf("") }
    LaunchedEffect(pending) { pending?.let { lastRemovedName = it.displayName } }

    val maxListHeight = LocalConfiguration.current.screenHeightDp.dp * 0.68f
    val statusBarHeightPx = WindowInsets.safeDrawing.getTop(LocalDensity.current)

    val pageModifier = Modifier
        .padding(horizontal = 16.dp)
        .clip(PageShape)
    levelTransition.AnimatedContent(
        modifier = Modifier.fillMaxWidth(),
        transitionSpec = { addAccountTransform(forward = targetState) },
        contentKey = { it },
    ) { adding ->
        if (adding) {
            val animatedCancelPadding by transition.animateDp(
                transitionSpec = {
                    tween(
                        durationMillis = 200,
                        easing = FastOutSlowInEasing
                    )
                },
                label = "cancelPadding"
            ) { state ->
                if (state == EnterExitState.Visible) 32.dp else 0.dp
            }
            val animatedCancelOpacity by transition.animateFloat(
                transitionSpec = { tween(durationMillis = 500) },
                label = "cancelOpacity"
            ) { state ->
                if (state == EnterExitState.Visible) 0f else 1f
            }
            DisposableEffect(Unit) {
                onDispose { authViewModel.reset() }
            }
            AuthScreenSheetContent(
                modifier = pageModifier
                    .fillMaxWidth()
                    .fillHeightBelowStatusBar(statusBarHeightPx),
                onSignedIn = { _, requiresCareerPick ->
                    if (!requiresCareerPick) isAddingAccount = false
                },
                onCancel = { isAddingAccount = false },
                cancelPaddingProvider = { animatedCancelPadding },
                cancelOpacityProvider = { animatedCancelOpacity },
                viewModel = authViewModel,
            )
        } else {
            Column(Modifier.fillMaxWidth()) {
                SheetHeader(
                    accountSwitcherHeader(
                        accountCount = accounts.size,
                        onOpenSettings = { control?.dismiss(); onOpenSettings() },
                    ),
                )
                AccountsScene(
                    modifier = pageModifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    ordered = ordered,
                    activeId = activeId,
                    photos = photos,
                    pending = pending,
                    lastRemovedName = lastRemovedName,
                    maxListHeight = maxListHeight,
                    motion = motion,
                    onOpenDetails = { control?.dismiss(); onOpenProfile() },
                    onSwitchAccount = { viewModel.switchAccount(it) },
                    onSelectCareer = { id, careerId ->
                        viewModel.selectAccountCareer(id, careerId)
                    },
                    onRequestRemove = { viewModel.requestRemove(it) },
                    onUndoRemove = { viewModel.undoRemove() },
                    onAddAccount = { isAddingAccount = true },
                )
            }
        }
    }
}

/**
 * The roster ⇄ sign-in morph: the incoming level slides a full page height in (the sign-in from
 * below, the roster back from above) while the outgoing one leaves the other way, both dimming
 * rather than vanishing, and the sheet's height follows.
 */
private fun AnimatedContentTransitionScope<Boolean>.addAccountTransform(forward: Boolean): ContentTransform {
    val enter = slideInVertically(tween(durationMillis = 400)) { height -> if (forward) height else -height } +
        fadeIn(tween(durationMillis = 400), initialAlpha = 0.2f)
    val exit = slideOutVertically(tween(durationMillis = 600)) { height -> if (forward) -height else height } +
        fadeOut(tween(durationMillis = 400), targetAlpha = 0.2f)
    return ContentTransform(
        targetContentEnter = enter,
        initialContentExit = exit,
        sizeTransform = SizeTransform(clip = true) { _, _ -> tween(durationMillis = 500) },
    )
}

/**
 * Fills the height left in the sheet, short of [statusBarHeightPx]: the sheet spans the whole
 * window, and this keeps its top edge just under the status bar.
 */
private fun Modifier.fillHeightBelowStatusBar(statusBarHeightPx: Int): Modifier =
    layout { measurable, constraints ->
        val placeable = if (constraints.hasBoundedHeight) {
            val height = (constraints.maxHeight - statusBarHeightPx).coerceIn(constraints.minHeight, constraints.maxHeight)
            measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
        } else {
            measurable.measure(constraints)
        }
        layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }

/** The gear at the trailing edge of the roster's header, opening the app settings. */
@Composable
internal fun AccountSwitcherSettingsShortcut(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val haptic = rememberHapticManager()
    IconButton(
        onClick = { haptic.tap(); onClick() },
        modifier = modifier.testTag(AccountSwitcherTestTags.SETTINGS_SHORTCUT),
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = stringResource(R.string.account_switcher_settings),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun AccountsScene(
    modifier: Modifier,
    ordered: List<Account>,
    activeId: AccountId?,
    photos: Map<AccountId, File?>,
    pending: Account?,
    lastRemovedName: String,
    maxListHeight: Dp,
    motion: MotionScheme,
    onOpenDetails: () -> Unit,
    onSwitchAccount: (AccountId) -> Unit,
    onSelectCareer: (AccountId, CareerId) -> Unit,
    onRequestRemove: (Account) -> Unit,
    onUndoRemove: () -> Unit,
    onAddAccount: () -> Unit,
) {
    Column(
        modifier = modifier
            .testTag(AccountSwitcherTestTags.ROSTER)
            .padding(top = 4.dp, bottom = 24.dp),
    ) {
        LazyColumn(
            modifier = Modifier.heightIn(max = maxListHeight),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(ordered, key = { it.id.value }) { account ->
                val isPending = pending?.id == account.id

                AnimatedVisibility(
                    visible = !isPending,
                    enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
                    exit = shrinkVertically(motion.defaultSpatialSpec()) + fadeOut(motion.defaultEffectsSpec()),
                    modifier = Modifier.animateItem(
                        fadeInSpec = motion.defaultEffectsSpec(),
                        fadeOutSpec = motion.defaultEffectsSpec(),
                        placementSpec = motion.defaultSpatialSpec(),
                    ),
                ) {
                    SwipeToRemoveBox(
                        pendingRemoval = isPending,
                        onConfirmRemove = { onRequestRemove(account) },
                        background = { armed, revealed ->
                            RemoveSwipeBackground(
                                armed = armed,
                                revealed = revealed,
                                shape = CardShape,
                            )
                        },
                    ) {
                        ProfileCard(
                            account = account,
                            isActive = account.id == activeId,
                            photo = photos[account.id],
                            onOpenDetails = onOpenDetails,
                            onSwitchAccount = { onSwitchAccount(account.id) },
                            onSelectCareer = { careerId -> onSelectCareer(account.id, careerId) },
                            modifier = Modifier.testTag(AccountSwitcherTestTags.account(account.id)),
                        )
                    }
                }
            }

            item(key = ADD_ACCOUNT_KEY) {
                AddAccountCard(
                    onClick = onAddAccount,
                    modifier = Modifier
                        .testTag(AccountSwitcherTestTags.ADD_ACCOUNT)
                        .animateItem(
                            placementSpec = motion.defaultSpatialSpec(),
                        ),
                )
            }
        }

        AnimatedVisibility(
            visible = pending != null,
            enter = slideInVertically(motion.defaultSpatialSpec()) { it } + fadeIn(motion.defaultEffectsSpec()),
            exit = slideOutVertically(motion.defaultSpatialSpec()) { it } + fadeOut(motion.defaultEffectsSpec()) + shrinkVertically(
                tween(delayMillis = 300)
            ),
        ) {
            UndoRemovalBar(
                displayName = lastRemovedName,
                onUndo = onUndoRemove,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountsScenePreview() {
    BicoccaTheme(dark = false) {
        AccountsScenePreviewContent()
    }
}

@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgLowest
)
@Composable
private fun AccountsSceneDarkPreview() {
    BicoccaTheme(dark = true) {
        AccountsScenePreviewContent()
    }
}

@Composable
private fun AccountsScenePreviewContent() {
    val account = Account(
        id = AccountId("1"),
        username = "m.rossi@campus.unimib.it",
        displayName = "MARIO ROSSI",
        academic = AcademicIdentity(
            recordUserId = "1",
            personId = 1,
            fiscalCode = "RSSMRA80A01H501U",
            careers = listOf(
                Career(
                    id = CareerId(1),
                    enrollmentTraitId = 1,
                    programId = 1,
                    easyStaffProgramCode = "P01",
                    academicYearEnrollmentId = 1,
                    studentNumber = "123456",
                    description = "Informatica",
//                    academicYear = Instant.now().get(ChronoField.YEAR),
                    academicYear = 2026,
                    status = CareerStatus.ACTIVE
                )
            ),
            selectedCareerId = CareerId(1)
        ),
        learning = LearningIdentity(
            lmsUserId = 1,
            lmsUsername = "m.rossi",
            locale = "it",
            isSiteAdmin = false,
            maxUploadFileSizeBytes = 100,
            storageQuotaBytes = 100
        ),
        createdAt = Instant.now(),
        lastUsedAt = Instant.now(),
        lastSyncedAt = Instant.now()
    )

    ProvideHapticManager(enabled = true) {
        AccountsScene(
            modifier = Modifier.fillMaxWidth(),
            ordered = listOf(account),
            activeId = account.id,
            photos = emptyMap(),
            pending = null,
            lastRemovedName = "",
            maxListHeight = 600.dp,
            motion = MaterialTheme.motionScheme,
            onOpenDetails = {},
            onSwitchAccount = {},
            onSelectCareer = { _, _ -> },
            onRequestRemove = {},
            onUndoRemove = {},
            onAddAccount = {}
        )
    }
}
