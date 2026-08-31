package it.attendance100.mybicocca.ui.screen.settings.subscreen.appInfo

import android.annotation.SuppressLint
import androidx.activity.compose.PredictiveBackHandler
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import it.attendance100.mybicocca.BuildConfig
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.currentLocale
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.data.update.DownloadState
import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.model.update.UpdateCheckResult
import it.attendance100.mybicocca.domain.model.update.UpdateStatus
import it.attendance100.mybicocca.ui.component.brand.MyBicoccaWordmark
import it.attendance100.mybicocca.ui.component.directory.SegmentedIconChip
import it.attendance100.mybicocca.ui.component.directory.SegmentedTile
import it.attendance100.mybicocca.ui.component.directory.segmentedShape
import it.attendance100.mybicocca.ui.component.feedback.AppSnackbarHost
import it.attendance100.mybicocca.ui.component.feedback.rememberAppSnackbarController
import it.attendance100.mybicocca.ui.component.modal.UpdateModalSheet
import it.attendance100.mybicocca.ui.component.modal.sheetPageTransform
import kotlinx.coroutines.launch
import java.time.Year
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

private const val COPYRIGHT_START_YEAR = 2025
private const val GITHUB_URL = "https://github.com/Auties00/MyBicocca"

private val versionText: String = buildString {
    append("Versione ${BuildConfig.VERSION_NAME}")
    @Suppress("KotlinConstantConditions", "SimplifyBooleanWithConstants")
    if (BuildConfig.DEBUG && BuildConfig.BUILD_TYPE != "release") append(" [Debug]")

    val nightlyId = BuildConfig.NIGHTLY_IDENTIFIER
    if (BuildConfig.VERSION_NAME.contains("nightly") && nightlyId.isNotEmpty()) {
        append(" [$nightlyId]")
    }
}

private val copyrightText: String
    get() {
        val current = Year.now().value
        val span =
            if (current > COPYRIGHT_START_YEAR) "$COPYRIGHT_START_YEAR–$current" else "$COPYRIGHT_START_YEAR"
        return "© $span 100% Attendance"
    }

private data class Credit(val name: String, val githubUsername: String)

private val CREDITS = listOf(
    Credit("Alessandro Autiero", "Auties00"),
    Credit("Lorenzo Angelo Lupi", "LordLux"),
    Credit("Alessandro Ferrari Pagini", "AleFerroExe"),
    Credit("Federico Giarrusso", "Fedogia"),
)

/**
 * The settings About modal — a full-height, three-level bottom sheet built on the same
 * predictive-back machinery as the account switcher. The levels form a stack by [depth]:
 * 0 = About, 1 = What's New (the merged changelog), 2 = All versions (the per-release list).
 *
 * The sheet always expands to the full available height even when the About content is short, so
 * every level shares one stable frame. A back gesture is staged through a seekable transition:
 * above the root it drives the page back one level (springing back if cancelled); at the root it
 * drives the close transition, shrinking the sheet's height in step with the finger before
 * dismissing. Tapping a tile / the "All versions" button / an in-page back arrow walks the same
 * stack with the shared in-sheet page push.
 *
 * Level 0 keeps the update-aware "Check for Updates" tile (forced check + sheet snackbar while up
 * to date; an "Update available" tile that opens the store-aware page once a newer release is
 * known), the "What's New" navigation tile, the "GitHub" Custom Tab link, and the credits.
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppInfoSheet(
    onDismiss: () -> Unit,
    viewModel: AppInfoViewModel = hiltViewModel(),
) {
    val scheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val snackbar = rememberAppSnackbarController()
    val githubIcon = ImageVector.vectorResource(R.drawable.ic_github)

    val updateStatus by viewModel.status.collectAsStateWithLifecycle()
    val nightlyStatus by viewModel.nightlyStatus.collectAsStateWithLifecycle()
    val nightlyEnabled by viewModel.nightlyEnabled.collectAsStateWithLifecycle()
    val checking by viewModel.checking.collectAsStateWithLifecycle()
    var showRestoreStableDialog by remember { mutableStateOf(false) }
    // In-sheet depth: 0 = About, 1 = What's New (merged), 2 = All versions, 3 = Update Settings.
    var depth by rememberSaveable { mutableIntStateOf(0) }
    var showUpdateModal by remember { mutableStateOf<AppRelease?>(null) }
    val nightlyAutoInstall by viewModel.nightlyAutoInstall.collectAsStateWithLifecycle()

    showUpdateModal?.let { release ->
        UpdateModalSheet(
            release = release,
            downloadStateFlow = viewModel.downloadState,
            onDownload = { viewModel.startDownload(release) },
            onInstall = { file ->
                viewModel.installDownload(file, silent = release.isPreRelease)
                viewModel.clearDownload()
                showUpdateModal = null
            },
            onDismiss = {
                viewModel.dismissDownloadError()
                showUpdateModal = null
            },
            autoInstallOnSuccess = release.isPreRelease && nightlyAutoInstall
        )
    }

    val noUpdatesMsg = stringResource(R.string.settings_no_updates_found)
    val newVersionMsg = stringResource(R.string.shell_update_available)
    val checkFailedMsg = stringResource(R.string.settings_update_check_failed)

    val onCheckResult: (UpdateCheckResult) -> Unit = { result ->
        scope.launch {
            when (result) {
                UpdateCheckResult.UpToDate -> snackbar.showInfo(noUpdatesMsg)
                is UpdateCheckResult.UpdateAvailable -> snackbar.showInfo(newVersionMsg)
                is UpdateCheckResult.Failed -> snackbar.showError(checkFailedMsg, result.cause)
            }
        }
    }

    // Seekable depth pager (About -> Merged -> All versions), advanced by the tiles/buttons and
    // driven frame-by-frame by the back gesture while not at the root.
    val pageSeekable = remember { SeekableTransitionState(0) }
    val pageTransition = rememberTransition(pageSeekable, label = "appInfoPage")
    LaunchedEffect(depth) {
        if (pageSeekable.targetState != depth) {
            pageSeekable.animateTo(depth, tween(durationMillis = 450))
        }
    }
    val depthState = rememberUpdatedState(depth)

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newState ->
            !(depthState.value > 0 && newState == SheetValue.Hidden)
        },
    )

    // Seekable close transition, driven by the back gesture in the About state so the sheet's
    // height collapses with the finger before it dismisses.
    val closeSeekable = remember { SeekableTransitionState(true) }
    val closeTransition = rememberTransition(closeSeekable, label = "appInfoClose")

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val topWindowInsets =
        WindowInsets.safeDrawing.asPaddingValues(LocalDensity.current).calculateTopPadding()
    val handleHeight = 16.dp
    val fullHeight = screenHeight - topWindowInsets - handleHeight

    ModalBottomSheet(
        onDismissRequest = { if (depth > 0) depth -= 1 else onDismiss() },
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0) },
        dragHandle = { Box(Modifier.padding(top = handleHeight)) },
        shape = BottomSheetDefaults.ExpandedShape,
        containerColor = scheme.surfaceContainerLow,
    ) {
        PredictiveBackHandler(enabled = depth > 0) { progress ->
            val target = depth - 1
            try {
                progress.collect { backEvent ->
                    pageSeekable.seekTo(backEvent.progress, targetState = target)
                }
                depth = target
                pageSeekable.animateTo(target)
            } catch (_: CancellationException) {
                pageSeekable.animateTo(depth)
            }
        }

        PredictiveBackHandler(enabled = depth == 0 && sheetState.isVisible) { progress ->
            try {
                progress.collect { backEvent ->
                    closeSeekable.seekTo(
                        backEvent.progress,
                        targetState = false
                    )
                }
                closeSeekable.animateTo(false)
                onDismiss()
            } catch (_: CancellationException) {
                closeSeekable.animateTo(true)
            }
        }

        closeTransition.AnimatedContent(
            modifier = Modifier.fillMaxWidth(),
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn(tween(durationMillis = 400)),
                    initialContentExit = fadeOut(tween(durationMillis = 400)),
                    sizeTransform = SizeTransform(clip = true) { _, _ -> tween(durationMillis = 450) },
                )
            },
            contentKey = { it },
        ) { isVisible ->
            if (isVisible) {
                Box(Modifier.fillMaxWidth()) {
                    pageTransition.AnimatedContent(
                        modifier = Modifier.fillMaxWidth(),
                        transitionSpec = { sheetPageTransform(forward = targetState > initialState) },
                        contentKey = { it },
                    ) { pageDepth ->
                        val pageModifier = Modifier
                            .fillMaxWidth()
                            .height(fullHeight)
                        when (pageDepth) {
                            0 -> AboutScene(
                                modifier = pageModifier,
                                viewModel = viewModel,
                                updateStatus = updateStatus,
                                checking = checking,
                                githubIcon = githubIcon,
                                nightlyStatus = nightlyStatus,
                                nightlyEnabled = nightlyEnabled,
                                onOpenWhatsNew = { depth = 1 },
                                onOpenUpdateSettings = { depth = 3 },
                                onCheckResult = onCheckResult,
                                onShowUpdateModal = { showUpdateModal = it }
                            )

                            1 -> WhatsNewScene(
                                onBack = { depth = 0 },
                                onAllVersions = { depth = 2 },
                                modifier = pageModifier,
                            )
                            
                            2 -> WhatsNewAllVersionsScene(
                                onBack = { depth = 1 },
                                modifier = pageModifier,
                            )

                            3 -> UpdateSettingsScene(
                                modifier = pageModifier,
                                viewModel = viewModel,
                                nightlyEnabled = nightlyEnabled,
                                onBack = { depth = 0 },
                                setShowRestoreStableDialog = { showRestoreStableDialog = it }
                            )
                        }
                    }
                    AppSnackbarHost(
                        controller = snackbar,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            } else {
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(0.dp),
                )
            }
        }
    }

    // Hoisted above the in-sheet pages (rather than scoped to AboutScene) so it shows up
    // immediately when toggling beta updates off from the Update Settings page itself, not just
    // after navigating back to About.
    if (showRestoreStableDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreStableDialog = false },
            title = { Text(stringResource(R.string.settings_beta_restore_stable_title)) },
            text = { Text(stringResource(R.string.settings_beta_restore_stable_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreStableDialog = false
                    viewModel.setNightlyEnabled(false)
                    viewModel.check { result ->
                        if (result is UpdateCheckResult.UpdateAvailable) {
                            showUpdateModal = result.release
                            // Respect the same auto-download setting the background checker
                            // honors, so this deliberate action doesn't silently ignore it —
                            // the modal still opens so progress stays visible either way.
                            if (viewModel.stableAutoDownload.value) {
                                viewModel.startDownload(result.release)
                            }
                        }
                    }
                }) {
                    Text(stringResource(R.string.settings_beta_restore_stable_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreStableDialog = false }) {
                    Text(stringResource(R.string.settings_beta_restore_stable_dismiss))
                }
            }
        )
    }
}

/**
 * The About state's content: the centered logo/wordmark/version/copyright header, the action
 * tiles, and the credits. Sized to fill the sheet via [modifier]; its content scrolls when it
 * outgrows the frame and simply sits at the top otherwise.
 */
@Composable
private fun AboutScene(
    modifier: Modifier,
    viewModel: AppInfoViewModel,
    updateStatus: UpdateStatus,
    checking: Boolean,
    githubIcon: ImageVector,
    nightlyStatus: UpdateStatus,
    nightlyEnabled: Boolean,
    onOpenWhatsNew: () -> Unit,
    onOpenUpdateSettings: () -> Unit,
    onCheckResult: (UpdateCheckResult) -> Unit,
    onShowUpdateModal: (AppRelease) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val secondary = scheme.onSurfaceVariant
    val haptic = rememberHapticManager()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = stringResource(R.string.app_info_logo_content_description),
                modifier = Modifier.size(168.dp),
            )
            MyBicoccaWordmark(
                modifier = Modifier.offset(y = (-10).dp),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(text = versionText, color = secondary, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Text(text = copyrightText, color = secondary, fontSize = 13.sp)
        }

        Spacer(Modifier.height(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            val nightlyAvailable = nightlyStatus as? UpdateStatus.UpdateAvailable
            val stableAvailable = updateStatus as? UpdateStatus.UpdateAvailable

            if (nightlyAvailable != null) {
                NightlyUpdateTile(
                    release = nightlyAvailable.release,
                    downloadStateFlow = viewModel.downloadState,
                    onShowUpdateModal = onShowUpdateModal,
                    isFirst = true,
                    isLast = false
                )
            } else if (stableAvailable != null) {
                UpdateAvailableTile(
                    release = stableAvailable.release,
                    downloadStateFlow = viewModel.downloadState,
                    onShowUpdateModal = onShowUpdateModal,
                    isFirst = true,
                    isLast = false
                )
            } else {
                SegmentedTile(
                    isFirst = true,
                    isLast = false,
                    title = stringResource(R.string.settings_check_updates_title),
                    subtitle = stringResource(R.string.settings_check_updates_subtitle),
                    onClick = {
                        haptic.tap()
                        if (!checking) viewModel.check(onCheckResult)
                    },
                    leading = {
                        SegmentedIconChip(
                            Icons.Outlined.Update,
                            scheme.secondaryContainer,
                            scheme.onSecondaryContainer,
                        )
                    },
                    trailing = {
                        Spacer(Modifier.width(6.dp))
                        if (checking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = scheme.primary,
                            )
                        }
                    },
                )
            }
            SegmentedTile(
                isFirst = false,
                isLast = false,
                title = stringResource(R.string.settings_whats_new_title),
                subtitle = stringResource(R.string.settings_whats_new_subtitle),
                onClick = {
                    haptic.tap()
                    onOpenWhatsNew()
                },
                leading = {
                    SegmentedIconChip(
                        Icons.Outlined.NewReleases,
                        scheme.secondaryContainer,
                        scheme.onSecondaryContainer,
                    )
                },
                trailing = { TrailingGlyph(Icons.Rounded.ChevronRight) },
            )

            SegmentedTile(
                isFirst = false,
                isLast = false,
                title = stringResource(R.string.settings_update_settings_title),
                subtitle = stringResource(R.string.settings_update_settings_subtitle),
                onClick = {
                    haptic.tap()
                    onOpenUpdateSettings()
                },
                leading = {
                    SegmentedIconChip(
                        ImageVector.vectorResource(R.drawable.rule_settings_24px),
                        scheme.secondaryContainer,
                        scheme.onSecondaryContainer
                    )
                },
                trailing = { TrailingGlyph(Icons.Rounded.ChevronRight) },
            )

            SegmentedTile(
                isFirst = false,
                isLast = true,
                title = stringResource(R.string.settings_github_title),
                subtitle = stringResource(R.string.settings_github_subtitle),
                onClick = {
                    haptic.tap()
                    CustomTabsIntent.Builder().setShowTitle(true).build()
                        .launchUrl(context, GITHUB_URL.toUri())
                },
                leading = {
                    SegmentedIconChip(
                        githubIcon,
                        scheme.secondaryContainer,
                        scheme.onSecondaryContainer,
                    )
                },
                trailing = { TrailingGlyph(Icons.Rounded.Link) },
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.settings_credits_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            CREDITS.forEachIndexed { index, credit ->
                CreditTile(
                    credit = credit,
                    isFirst = index == 0,
                    isLast = index == CREDITS.lastIndex,
                )
            }
        }
    }
}

/** The standard trailing chevron/link glyph for the action tiles. */
@Composable
private fun TrailingGlyph(icon: ImageVector) {
    Spacer(Modifier.width(6.dp))
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier.size(22.dp),
    )
}

/**
 * Compact credit row: a circular avatar slot + the member's name. Kept separate from the
 * directory tiles because it is intentionally denser (smaller avatar/text, no trailing); it
 * still shares the connected-card [segmentedShape]. The avatar renders the credit's icon lambda
 * inside a secondary-container circle, a slot meant to carry each member's photo.
 */
@Composable
private fun CreditTile(
    credit: Credit,
    isFirst: Boolean,
    isLast: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val haptic = rememberHapticManager()

    Surface(
        onClick = {
            haptic.tap()
            CustomTabsIntent.Builder().setShowTitle(true).build()
                .launchUrl(context, "https://github.com/${credit.githubUsername}".toUri())
        },
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = segmentedShape(isFirst, isLast),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = "https://github.com/${credit.githubUsername}.png",
                contentDescription = "${credit.name} Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(scheme.secondaryContainer),
            )
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = credit.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "@${credit.githubUsername}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.weight(1f))
            TrailingGlyph(Icons.Rounded.ChevronRight)
        }
    }
}

@Composable
private fun UpdateAvailableTile(
    release: AppRelease,
    downloadStateFlow: kotlinx.coroutines.flow.StateFlow<DownloadState>,
    onShowUpdateModal: (AppRelease) -> Unit,
    isFirst: Boolean,
    isLast: Boolean,
) {
    val downloadState by downloadStateFlow.collectAsStateWithLifecycle()
    val isDownloading = downloadState is DownloadState.Downloading
    val progress = (downloadState as? DownloadState.Downloading)?.progress ?: 0
    val subtitle =
        if (isDownloading) stringResource(R.string.update_modal_downloading, progress)
        else stringResource(
            R.string.settings_update_available_subtitle,
            release.versionName
        )
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme

    SegmentedTile(
        isFirst = isFirst,
        isLast = isLast,
        title = stringResource(R.string.settings_update_available_title),
        subtitle = subtitle,
        progress = if (isDownloading) progress / 100f else null,
        onClick = if (isDownloading) null else {
            {
                haptic.tap()
                onShowUpdateModal(release)
            }
        },
        leading = {
            SegmentedIconChip(
                Icons.Outlined.Update,
                scheme.primaryContainer,
                scheme.onPrimaryContainer,
            )
        },
        trailing = { TrailingGlyph(Icons.Rounded.ChevronRight) },
    )
}

@Composable
private fun NightlyUpdateTile(
    release: AppRelease,
    downloadStateFlow: kotlinx.coroutines.flow.StateFlow<DownloadState>,
    onShowUpdateModal: (AppRelease) -> Unit,
    isFirst: Boolean,
    isLast: Boolean,
) {
    val downloadState by downloadStateFlow.collectAsStateWithLifecycle()
    val isDownloading = downloadState is DownloadState.Downloading
    val progress = (downloadState as? DownloadState.Downloading)?.progress ?: 0
    val scheme = MaterialTheme.colorScheme

    val base = release.versionName
    val sha = release.commitSha
    val downloadingStr = stringResource(R.string.update_modal_downloading, progress)
    val fromStr = stringResource(R.string.settings_nightly_from, base)
    val commitStr = sha?.let { stringResource(R.string.settings_nightly_commit, it) }

    val subtitleAnnotated = if (isDownloading) {
        androidx.compose.ui.text.AnnotatedString(downloadingStr)
    } else {
        androidx.compose.ui.text.AnnotatedString(fromStr)
    }
    val haptic = rememberHapticManager()

    SegmentedTile(
        isFirst = isFirst,
        isLast = isLast,
        title = stringResource(R.string.settings_nightly_available_title),
        subtitleAnnotated = subtitleAnnotated,
        progress = if (isDownloading) progress / 100f else null,
        onClick = if (isDownloading) null else {
            {
                haptic.tap()
                onShowUpdateModal(release)
            }
        },
        leading = {
            SegmentedIconChip(
                ImageVector.vectorResource(R.drawable.moon_stars_24px),
                scheme.tertiaryContainer,
                scheme.onTertiaryContainer,
            )
        },
        trailing = { TrailingGlyph(Icons.Rounded.ChevronRight) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdateSettingsScene(
    modifier: Modifier,
    viewModel: AppInfoViewModel,
    nightlyEnabled: Boolean,
    onBack: () -> Unit,
    setShowRestoreStableDialog: (Boolean) -> Unit,
) {
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme

    val stableAutoDownload by viewModel.stableAutoDownload.collectAsStateWithLifecycle()
    val nightlyAutoDownload by viewModel.nightlyAutoDownload.collectAsStateWithLifecycle()
    val nightlyAutoInstall by viewModel.nightlyAutoInstall.collectAsStateWithLifecycle()
    val checkIntervalMinutes by viewModel.checkIntervalMinutes.collectAsStateWithLifecycle()

    Column(modifier = modifier) {
        TopAppBar(
            title = { Text(stringResource(R.string.settings_update_settings_title)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_update_check_frequency_header),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            CheckIntervalSlider(
                intervalMinutes = checkIntervalMinutes,
                onIntervalChange = { viewModel.setCheckIntervalMinutes(it) },
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.settings_update_stable_header),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
            
            SegmentedTile(
                isFirst = true,
                isLast = true,
                role = androidx.compose.ui.semantics.Role.Switch,
                title = stringResource(R.string.settings_update_stable_auto_download_title),
                subtitle = stringResource(R.string.settings_update_stable_auto_download_subtitle),
                onClick = {
                    haptic.tap()
                    viewModel.setStableAutoDownload(!stableAutoDownload)
                },
                trailing = {
                    Switch(
                        checked = stableAutoDownload,
                        onCheckedChange = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.settings_update_beta_header),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            SegmentedTile(
                isFirst = true,
                isLast = false,
                role = androidx.compose.ui.semantics.Role.Switch,
                title = stringResource(R.string.settings_beta_updates_title),
                subtitle = stringResource(R.string.settings_beta_updates_subtitle),
                onClick = {
                    haptic.tap()
                    if (nightlyEnabled) {
                        viewModel.checkAndOfferStable {
                            setShowRestoreStableDialog(true)
                        }
                    } else {
                        viewModel.setNightlyEnabled(true)
                    }
                },
                trailing = {
                    Switch(
                        checked = nightlyEnabled,
                        onCheckedChange = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            )

            val betaTogglesEnabled = nightlyEnabled
            val alpha = if (betaTogglesEnabled) 1f else 0.5f

            SegmentedTile(
                isFirst = false,
                isLast = false,
                role = androidx.compose.ui.semantics.Role.Switch,
                title = stringResource(R.string.settings_update_beta_auto_download_title),
                subtitle = stringResource(R.string.settings_update_beta_auto_download_subtitle),
                onClick = if (!betaTogglesEnabled) null else { {
                    haptic.tap()
                    viewModel.setNightlyAutoDownload(!nightlyAutoDownload)
                } },
                trailing = {
                    Switch(
                        checked = nightlyAutoDownload && betaTogglesEnabled,
                        onCheckedChange = null,
                        enabled = betaTogglesEnabled,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                },
                modifier = Modifier.alpha(alpha)
            )

            SegmentedTile(
                isFirst = false,
                isLast = true,
                role = androidx.compose.ui.semantics.Role.Switch,
                title = stringResource(R.string.settings_update_beta_auto_install_title),
                subtitle = stringResource(R.string.settings_update_beta_auto_install_subtitle),
                onClick = if (!betaTogglesEnabled) null else { {
                    haptic.tap()
                    viewModel.setNightlyAutoInstall(!nightlyAutoInstall)
                } },
                trailing = {
                    Switch(
                        checked = nightlyAutoInstall && betaTogglesEnabled,
                        onCheckedChange = null,
                        enabled = betaTogglesEnabled,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                },
                modifier = Modifier.alpha(alpha)
            )
        }
    }
}

/** Discrete steps (in minutes) offered by [CheckIntervalSlider], from WorkManager's own floor up to a day. */
private val CHECK_INTERVAL_STEPS_MINUTES = listOf(15, 30, 60, 120, 180, 360, 720, 1440)

/**
 * The "Controlla aggiornamenti ogni" control: a discrete slider over [CHECK_INTERVAL_STEPS_MINUTES]
 * choosing how often UpdateChecker's periodic worker fires. Mirrors SettingsSecuritySheet's
 * TimeoutSlider: haptic ticks on step-crossing only, commit on release.
 */
@Composable
private fun CheckIntervalSlider(
    intervalMinutes: Int,
    onIntervalChange: (Int) -> Unit,
) {
    val haptic = rememberHapticManager()

    var sliderPos by remember(intervalMinutes) {
        mutableFloatStateOf(
            CHECK_INTERVAL_STEPS_MINUTES.indexOf(intervalMinutes).coerceAtLeast(0).toFloat()
        )
    }
    val index = sliderPos.roundToInt().coerceIn(0, CHECK_INTERVAL_STEPS_MINUTES.lastIndex)
    val colors = SliderDefaults.colors(
        activeTickColor = MaterialTheme.colorScheme.onSurface,
        inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        inactiveTickColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f),
    )

    val locale = currentLocale()
    val minutesFormat = stringResource(R.string.settings_security_minutes_format)
    val selectedMinutes = CHECK_INTERVAL_STEPS_MINUTES[index]
    val intervalLabel = if (selectedMinutes < 60) {
        String.format(locale, minutesFormat, selectedMinutes)
    } else {
        val hours = selectedMinutes / 60
        pluralStringResource(R.plurals.update_check_interval_hours, hours, hours)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.settings_update_check_interval_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = intervalLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = sliderPos,
            onValueChange = { newPos ->
                if (newPos.roundToInt().coerceIn(0, CHECK_INTERVAL_STEPS_MINUTES.lastIndex) != index)
                    haptic.feather()
                sliderPos = newPos
            },
            onValueChangeFinished = { onIntervalChange(CHECK_INTERVAL_STEPS_MINUTES[index]) },
            valueRange = 0f..CHECK_INTERVAL_STEPS_MINUTES.lastIndex.toFloat(),
            steps = CHECK_INTERVAL_STEPS_MINUTES.size - 2,
            colors = colors,
            track = { state ->
                SliderDefaults.Track(
                    sliderState = state,
                    colors = colors,
                    drawStopIndicator = null,
                )
            }
        )
    }
}
