package it.attendance100.mybicocca.ui.component.modal

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.release.parseReleaseNotes
import it.attendance100.mybicocca.domain.model.update.DownloadState
import it.attendance100.mybicocca.domain.model.update.UpdateModalKind
import it.attendance100.mybicocca.domain.model.update.readyToInstall
import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.ui.screen.settings.subscreen.appInfo.component.ReleaseNotesView
import java.io.File

/**
 * A channel change in progress: the switch is already flipped, and this sheet is where it is
 * carried out or taken back.
 *
 * Carrying the callback rather than sitting beside a flag means there is no way to ask for the
 * channel-change wording without an escape route out of it.
 */
/** A request to open [UpdateModalSheet], in the form the two hosts and the saved slot share. */
data class UpdateModalRequest(
    val release: AppRelease,
    val kind: UpdateModalKind = UpdateModalKind.Standard,
)

/**
 * The sheet's channel-change behaviour for this kind, or null for an ordinary update.
 *
 * [onStay] is handed the value the beta switch must be put *back* to, so a host writes the undo
 * once rather than once per direction and can't get the direction backwards.
 */
fun UpdateModalKind.channelSwitch(onStay: (nightlyEnabled: Boolean) -> Unit): ChannelSwitch? =
    when (this) {
        UpdateModalKind.Standard -> null
        UpdateModalKind.SwitchToNightly -> ChannelSwitch.ToNightly { onStay(false) }
        UpdateModalKind.RestoreStable -> ChannelSwitch.ToStable { onStay(true) }
    }

sealed interface ChannelSwitch {
    /** Undoes the switch that opened the sheet, and stops whatever it started. */
    val onStay: () -> Unit

    /** Stable to nightly. */
    data class ToNightly(override val onStay: () -> Unit) : ChannelSwitch

    /** Nightly back to stable, the one update flow that moves backwards. */
    data class ToStable(override val onStay: () -> Unit) : ChannelSwitch
}

/**
 * [onInstall] fires only from the "Install" button; a finished download never installs itself.
 *
 * [channelSwitch] non-null re-words the sheet for a channel change, whose ordinary copy ("New
 * version available", "Install the update") would describe a move the user isn't making. It also
 * changes what *leaving* means: the switch was flipped before the sheet opened, so dismissing has
 * to put it back rather than merely close, which is why the back gesture goes to `onStay` too and
 * not just the button.
 *
 * Leaving is never blocked, download in flight or not. It used to be, from before downloads ran in
 * a foreground service and closing the sheet really would have killed one; now the download
 * outlives the sheet, and blocking only traps whoever is on a slow connection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateModalSheet(
    release: AppRelease,
    downloadStateFlow: kotlinx.coroutines.flow.StateFlow<DownloadState>,
    onDownload: () -> Unit,
    onInstall: (File) -> Unit,
    onDismiss: () -> Unit,
    channelSwitch: ChannelSwitch? = null,
) {
    val downloadState by downloadStateFlow.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val isDowngrade = channelSwitch is ChannelSwitch.ToStable
    val isSwitchToNightly = channelSwitch is ChannelSwitch.ToNightly
    // For a channel change, leaving *is* declining it; otherwise leaving just closes the sheet and
    // lets the download carry on in the background.
    val onLeave = channelSwitch?.onStay ?: onDismiss

    Dialog(
        onDismissRequest = onLeave,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                // Top section
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Icon(
                        imageVector = when {
                            isDowngrade -> ImageVector.vectorResource(R.drawable.sync_arrow_down_24px)
                            isSwitchToNightly -> ImageVector.vectorResource(R.drawable.moon_stars_24px)
                            release.isPreRelease -> Icons.Outlined.Nightlight
                            else -> Icons.Outlined.NewReleases
                        },
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = when {
                            isDowngrade -> stringResource(R.string.update_modal_downgrade_title)
                            isSwitchToNightly -> stringResource(R.string.update_modal_switch_nightly_title)
                            release.isPreRelease -> "New Nightly available!"
                            else -> stringResource(R.string.update_modal_title)
                        },
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (release.isPreRelease) release.versionName else stringResource(R.string.update_modal_version, release.versionName),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(24.dp))

                    val notes = remember(release.notes) { parseReleaseNotes(release.notes) }
                    ReleaseNotesView(notes = notes, modifier = Modifier.fillMaxWidth())

                    Spacer(Modifier.height(16.dp))
                    TextButton(
                        onClick = { uriHandler.openUri(release.pageUrl) },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text(stringResource(R.string.update_modal_github))
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Bottom actions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isDownloading = downloadState is DownloadState.Downloading
                    // Queued shares the progress box rather than leaving the Download button
                    // sitting there looking untapped: the request is real, it just hasn't started.
                    val isQueued = downloadState is DownloadState.Enqueued
                    val hasError = downloadState is DownloadState.Error
                    // A declined install leaves a perfectly good APK behind, so it offers the same
                    // button as a fresh download rather than sending the user through it again.
                    val readyToInstall = downloadState.readyToInstall
                    val wasDeclined = downloadState is DownloadState.InstallDeclined
                    val progress = (downloadState as? DownloadState.Downloading)?.progress ?: 0
                    val progressFraction = (progress / 100f).coerceIn(0f, 1f)
                    val textToShow =
                        if (isQueued) stringResource(R.string.update_modal_queued)
                        else stringResource(R.string.update_modal_downloading, progress)

                    val animatedProgress by animateFloatAsState(
                        targetValue = progressFraction,
                        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                        label = "progressAnimation"
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (isDownloading || isQueued) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(50))
                                    .testTag(UpdateModalTestTags.PROGRESS),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = textToShow,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .drawWithContent {
                                            clipRect(right = size.width * animatedProgress) {
                                                this@drawWithContent.drawContent()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Text(
                                        text = textToShow,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else if (readyToInstall != null) {
                            Button(
                                onClick = { onInstall(readyToInstall) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag(UpdateModalTestTags.INSTALL)
                            ) {
                                Text(
                                    when {
                                        isDowngrade -> stringResource(
                                            R.string.update_modal_downgrade_install,
                                            release.versionName,
                                        )

                                        isSwitchToNightly -> stringResource(
                                            R.string.update_modal_switch_nightly_install,
                                            release.versionName,
                                        )

                                        else -> stringResource(R.string.update_modal_install)
                                    }
                                )
                            }
                        } else {
                            Button(
                                onClick = onDownload,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag(UpdateModalTestTags.DOWNLOAD)
                            ) {
                                Text(
                                    when {
                                        hasError -> stringResource(R.string.common_retry)
                                        isDowngrade -> stringResource(
                                            R.string.update_modal_downgrade_download,
                                            release.versionName,
                                        )

                                        isSwitchToNightly -> stringResource(
                                            R.string.update_modal_switch_nightly_download,
                                            release.versionName,
                                        )

                                        else -> stringResource(R.string.update_modal_download)
                                    }
                                )
                            }
                        }
                    }

                    if (hasError) {
                        Text(
                            text = (downloadState as DownloadState.Error).message.asString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    } else if (wasDeclined) {
                        Text(
                            text = stringResource(R.string.update_modal_install_cancelled),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }

                    OutlinedButton(
                        onClick = onLeave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag(UpdateModalTestTags.LEAVE),
                    ) {
                        Text(
                            stringResource(
                                when {
                                    isDowngrade -> R.string.update_modal_remain_on_nightly
                                    isSwitchToNightly -> R.string.update_modal_remain_on_stable
                                    else -> R.string.update_modal_not_now
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}
