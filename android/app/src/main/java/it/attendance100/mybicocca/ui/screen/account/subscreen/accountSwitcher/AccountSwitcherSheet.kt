package it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.component.UndoRemovalBar
import kotlinx.coroutines.launch

private val CardShape = RoundedCornerShape(28.dp)

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
        Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 12.dp),
            )

            Column(
                modifier = Modifier
                    .heightIn(max = maxListHeight)
                    .verticalScroll(rememberScrollState()),
            ) {
                ordered.forEach { account ->
                    key(account.id.value) {
                        val isPending = pending?.id == account.id
                        val dismissState = rememberSwipeToDismissBoxState(
                            positionalThreshold = { distance -> distance * 0.4f },
                        )
                        // Undo: slide the (off-screen) card back to its resting position so it
                        // reappears together with the expand animation.
                        LaunchedEffect(isPending) {
                            if (!isPending && dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                                dismissState.reset()
                            }
                        }

                        AnimatedVisibility(
                            visible = !isPending,
                            enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
                            exit = shrinkVertically(motion.defaultSpatialSpec()) + fadeOut(motion.defaultEffectsSpec()),
                        ) {
                            Column {
                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = false,
                                    enableDismissFromEndToStart = true,
                                    backgroundContent = {
                                        RemoveSwipeBackground(state = dismissState, shape = CardShape)
                                    },
                                    onDismiss = { direction ->
                                        if (direction == SwipeToDismissBoxValue.EndToStart) {
                                            viewModel.requestRemove(account)
                                        }
                                    },
                                ) {
                                    ProfileCard(
                                        account = account,
                                        isActive = account.id == activeId,
                                        photo = photos[account.id],
                                        onOpenDetails = {
                                            // Make sure the profile screen shows the tapped
                                            // account: switch to it first if it isn't active.
                                            viewModel.selectAccountCareer(account.id, account.academic.selectedCareerId)
                                            onOpenProfile()
                                            close()
                                        },
                                        onSelectCareer = { careerId ->
                                            viewModel.selectAccountCareer(account.id, careerId)
                                            close()
                                        },
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }

                AddAccountCard(
                    onClick = {
                        active?.let { onAddAccount(it) }
                        close()
                    },
                )
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
