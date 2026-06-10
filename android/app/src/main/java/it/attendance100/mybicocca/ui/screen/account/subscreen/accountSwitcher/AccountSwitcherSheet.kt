package it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher

import android.annotation.SuppressLint
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.core.os.ProvideHapticManager
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.ui.screen.account.AccountViewModel
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.component.AddAccountCard
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.component.ProfileCard
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.component.RemoveSwipeBackground
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.component.SwipeToRemoveBox
import it.attendance100.mybicocca.ui.screen.auth.AuthScreenSheetContent
import it.attendance100.mybicocca.ui.screen.auth.AuthViewModel
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import it.attendance100.mybicocca.ui.theme.PreviewBgLowest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant

private val CardShape = RoundedCornerShape(28.dp)
private const val ADD_ACCOUNT_KEY = "__add_account__"

/**
 * Modal bottom sheet for switching between saved accounts and their careers.
 *
 * Two scenes share the sheet through a seekable AnimatedContent: the account roster and an
 * in-sheet login for adding another account. The roster shows profile cards active-first —
 * the active card expands its careers inline — followed by an "Aggiungi un altro account"
 * tile, with swipe-to-remove on every card, the active one included: removal signs the
 * account out immediately and is confirmed by a snackbar, with no undo.
 * Entering add-account slides the roster away and stretches the content to nearly
 * full screen height; while it is open the sheet refuses to hide, so back (or a dismiss
 * request) exits login mode first. Predictive back scrubs whichever transition applies —
 * the login-to-roster scene change, or from the roster the sheet content's own collapse —
 * in step with the gesture. The login scene resets its form when it leaves, signing in
 * returns to the roster unless a career pick is required, and the profile/settings
 * shortcuts delegate upstream before closing the sheet.
 */
@Suppress("LABEL_NAME_CLASH")
@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AccountSwitcherSheet(
    onDismiss: () -> Unit,
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
    val inflight by authViewModel.inflight.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val motion = MaterialTheme.motionScheme

    var isAddingAccount by rememberSaveable { mutableStateOf(false) }

    val seekableState = remember { SeekableTransitionState(false) }
    val transition = rememberTransition(seekableState, label = "addAccountTransition")

    LaunchedEffect(isAddingAccount) {
        if (seekableState.targetState != isAddingAccount) {
            seekableState.animateTo(
                isAddingAccount,
                tween(durationMillis = 800, easing = FastOutLinearInEasing)
            )
        }
    }

    val isAddingAccountState = rememberUpdatedState(isAddingAccount)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newState ->
            !(isAddingAccountState.value && newState == SheetValue.Hidden)
        },
    )

    val close: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    val activeId = active?.id
    val ordered = remember(accounts, activeId) {
        accounts.sortedByDescending { it.id == activeId }
    }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val maxListHeight = screenHeight * 0.68f

    val windowInsets = WindowInsets.safeDrawing.asPaddingValues(LocalDensity.current)
    val topWindowInsets = windowInsets.calculateTopPadding()
    val handleHeight = 16.dp

    val closeSeekableState = remember { SeekableTransitionState(true) }
    val closeTransition = rememberTransition(closeSeekableState, label = "closeTransition")

    ModalBottomSheet(
        onDismissRequest = {
            if (isAddingAccount) {
                isAddingAccount = false
            } else {
                onDismiss()
            }
        },
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0) },
        dragHandle = { Box(Modifier.padding(top = handleHeight)) },
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
    ) {
        PredictiveBackHandler(enabled = isAddingAccount && !inflight) { progress ->
            try {
                progress.collect { backEvent ->
                    seekableState.seekTo(backEvent.progress, targetState = false)
                }
                isAddingAccount = false
                seekableState.animateTo(false)
            } catch (_: CancellationException) {
                seekableState.animateTo(true)
            }
        }

        PredictiveBackHandler(enabled = !isAddingAccount && sheetState.isVisible) { progress ->
            try {
                progress.collect { backEvent ->
                    closeSeekableState.seekTo(backEvent.progress, targetState = false)
                }
                closeSeekableState.animateTo(false)
                onDismiss()
            } catch (_: kotlin.coroutines.cancellation.CancellationException) {
                closeSeekableState.animateTo(true)
            }
        }

        closeTransition.AnimatedContent(
            modifier = Modifier.fillMaxWidth(),
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn(tween(durationMillis = 400)),
                    initialContentExit = fadeOut(tween(durationMillis = 400)),
                    sizeTransform = SizeTransform(clip = true) { _, _ ->
                        tween(durationMillis = 500)
                    },
                )
            },
            contentKey = { it }
        ) { isVisible ->
            if (isVisible) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = 24.dp,
                                topEnd = 24.dp,
                                bottomEnd = 0.dp,
                                bottomStart = 0.dp
                            )
                        )
                ) {
                    transition.AnimatedContent(
                        modifier = Modifier
                            .fillMaxWidth(),
                        transitionSpec = {
                            val isForward = targetState
                            val enter = slideInVertically(
                                tween(durationMillis = 400)
                            ) { h ->
                                if (isForward) h else -h
                            } + fadeIn(
                                tween(durationMillis = 400),
                                initialAlpha = 0.2f
                            )

                            val exit = slideOutVertically(
                                tween(durationMillis = 600)
                            ) { h ->
                                if (isForward) -h else h
                            } + fadeOut(
                                tween(durationMillis = 400),
                                targetAlpha = 0.2f
                            )

                            val modalShrinkDownSpeed = 500

                            ContentTransform(
                                targetContentEnter = enter,
                                initialContentExit = exit,
                                sizeTransform = SizeTransform(clip = true) { _, _ ->
                                    tween(durationMillis = modalShrinkDownSpeed)
                                },
                            )
                        },
                        contentKey = { it },
                    ) { adding ->
                        if (adding) {
                            val animatedCancelPadding by this@AnimatedContent.transition.animateDp(
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
                            val animatedCancelOpacity by this@AnimatedContent.transition.animateFloat(
                                transitionSpec = { tween(durationMillis = 500) },
                                label = "cancelOpacity"
                            ) { state ->
                                if (state == EnterExitState.Visible) 0f else 1f
                            }
                            DisposableEffect(Unit) {
                                onDispose { authViewModel.reset() }
                            }
                            AuthScreenSheetContent(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(screenHeight - topWindowInsets - handleHeight),
                                onSignedIn = { _, requiresCareerPick ->
                                    if (!requiresCareerPick) isAddingAccount = false
                                },
                                onCancel = { isAddingAccount = false },
                                cancelPaddingProvider = { animatedCancelPadding },
                                cancelOpacityProvider = { animatedCancelOpacity },
                                viewModel = authViewModel,
                            )
                        } else {
                            AccountsScene(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                ordered = ordered,
                                activeId = activeId,
                                photos = photos,
                                maxListHeight = maxListHeight,
                                motion = motion,
                                onOpenDetails = { onOpenProfile(); close() },
                                onOpenSettings = { onOpenSettings(); close() },
                                onSwitchAccount = { viewModel.switchAccount(it) },
                                onSelectCareer = { id, careerId ->
                                    viewModel.selectAccountCareer(id, careerId)
                                },
                                onRemove = { account -> viewModel.signOut(account.id) },
                                onAddAccount = { isAddingAccount = true },
                            )
                        }
                    }
                }
            } else {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.dp)
                )
            }
        }
    }
}

/**
 * Roster scene of the switcher: an "Account" header with a settings shortcut and the
 * height-capped account list (active card first, each wrapped in swipe-to-remove, the
 * add-account tile last). Removal is immediate — the swiped card slides off and the
 * survivors animate into their new slots when the account list updates.
 */
@Composable
internal fun AccountsScene(
    modifier: Modifier,
    ordered: List<Account>,
    activeId: AccountId?,
    photos: Map<AccountId, File?>,
    maxListHeight: Dp,
    motion: MotionScheme,
    onOpenDetails: () -> Unit,
    onOpenSettings: () -> Unit,
    onSwitchAccount: (AccountId) -> Unit,
    onSelectCareer: (AccountId, CareerId) -> Unit,
    onRemove: (Account) -> Unit,
    onAddAccount: () -> Unit,
) {
    Column(
        modifier = modifier
            .testTag(AccountSwitcherTestTags.ROSTER)
            .padding(bottom = 24.dp, top = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp),
            )
            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag(AccountSwitcherTestTags.SETTINGS_SHORTCUT),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Impostazioni",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        LazyColumn(
            modifier = Modifier.heightIn(max = maxListHeight),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(ordered, key = { it.id.value }) { account ->
                SwipeToRemoveBox(
                    onConfirmRemove = { onRemove(account) },
                    background = { armed, revealed ->
                        RemoveSwipeBackground(
                            armed = armed,
                            revealed = revealed,
                            shape = CardShape,
                        )
                    },
                    modifier = Modifier.animateItem(
                        fadeInSpec = motion.defaultEffectsSpec(),
                        fadeOutSpec = motion.defaultEffectsSpec(),
                        placementSpec = motion.defaultSpatialSpec(),
                    ),
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

    ProvideHapticManager {
        AccountsScene(
            modifier = Modifier.fillMaxWidth(),
            ordered = listOf(account),
            activeId = account.id,
            photos = emptyMap(),
            maxListHeight = 600.dp,
            motion = MaterialTheme.motionScheme,
            onOpenDetails = {},
            onOpenSettings = {},
            onSwitchAccount = {},
            onSelectCareer = { _, _ -> },
            onRemove = {},
            onAddAccount = {}
        )
    }
}
