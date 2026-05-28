package it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.ui.screen.account.AccountViewModel
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.component.AddAccountCard
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.component.ProfileCard
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.component.RemoveSwipeBackground
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.component.SwipeToRemoveBox
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.component.UndoRemovalBar
import kotlinx.coroutines.launch

private val CardShape = RoundedCornerShape(28.dp)
private const val ADD_ACCOUNT_KEY = "__add_account__"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSwitcherSheet(
    onDismiss: () -> Unit,
    onAddAccount: (returnTo: Account) -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val active by viewModel.activeAccount.collectAsStateWithLifecycle()
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val pending by viewModel.pendingRemoval.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val motion = MaterialTheme.motionScheme

    val close: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    // Active profile first; the rest keep their natural order. We do NOT drop the pending
    // account here — its card is collapsed in place so the gesture reads as a smooth fold.
    // The LazyColumn keys items by account id, so this re-sort triggers placement animations
    // when the active account changes instead of a layout jump.
    val activeId = active?.id
    val ordered = remember(accounts, activeId) {
        accounts.sortedByDescending { it.id == activeId }
    }

    // Keep the last removed name around so the undo bar text doesn't blank out mid-exit.
    var lastRemovedName by remember { mutableStateOf("") }
    LaunchedEffect(pending) { pending?.let { lastRemovedName = it.displayName } }

    val maxListHeight = LocalConfiguration.current.screenHeightDp.dp * 0.68f

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
        ) {
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 12.dp),
            )

            // LazyColumn (instead of Column + verticalScroll) so each item can use
            // Modifier.animateItem — that's what makes the active card slide to the top
            // and the previously active one slide down when the user switches accounts.
            // Placement uses the project's standard spatial spec (no extra bounce) so the
            // reorder feels controlled rather than springy.
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
                            onConfirmRemove = { viewModel.requestRemove(account) },
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
                                // Active header: open the profile page (closes the sheet).
                                onOpenDetails = {
                                    onOpenProfile()
                                    close()
                                },
                                // Inactive header / radio: promote this account but stay on
                                // the sheet so the user sees the new layout reorder itself.
                                onSwitchAccount = {
                                    viewModel.switchAccount(account.id)
                                },
                                // Inactive carriere don't render for non-active accounts;
                                // for the active account this switches the selected career
                                // in place without closing the sheet.
                                onSelectCareer = { careerId ->
                                    viewModel.selectAccountCareer(account.id, careerId)
                                },
                            )
                        }
                    }
                }

                item(key = ADD_ACCOUNT_KEY) {
                    AddAccountCard(
                        onClick = {
                            active?.let { onAddAccount(it) }
                            close()
                        },
                        modifier = Modifier.animateItem(
                            placementSpec = motion.defaultSpatialSpec(),
                        ),
                    )
                }
            }

            AnimatedVisibility(
                visible = pending != null,
                enter = slideInVertically(motion.defaultSpatialSpec()) { it } + fadeIn(motion.defaultEffectsSpec()),
                exit = slideOutVertically(motion.defaultSpatialSpec()) { it } + fadeOut(motion.defaultEffectsSpec()),
            ) {
                UndoRemovalBar(
                    displayName = lastRemovedName,
                    onUndo = { viewModel.undoRemove() },
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }
}
