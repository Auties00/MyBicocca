package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import java.time.format.FormatStyle
import java.time.format.DateTimeFormatter
import java.time.LocalTime
import it.attendance100.mybicocca.core.os.currentLocale
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import android.text.format.DateFormat
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.mensa.MensaFavourite
import it.attendance100.mybicocca.domain.model.mensa.MensaFavouriteSettings
import it.attendance100.mybicocca.ui.component.input.SegmentedSwitch
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaDishArtwork
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.mensaDishDrawable
import kotlinx.coroutines.launch

/** The two pages of the Personalize sheet, in pager order. */
internal enum class MensaPersonalizeTab { FAVOURITES, ALLERGENS }

/**
 * Favourites and allergens side by side, swiped between or picked with the switch at the bottom,
 * like the passed-exams and credits pages. [pagerState] is the caller's, so the sheet header can
 * follow the page.
 */
@Composable
internal fun MensaPersonalizePage(
    pagerState: PagerState,
    favourites: @Composable () -> Unit,
    allergens: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val haptic = rememberHapticManager()
    val tab = MensaPersonalizeTab.entries[pagerState.targetPage]
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                snapAnimationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            ),
            overscrollEffect = null,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
        ) { page ->
            when (MensaPersonalizeTab.entries[page]) {
                MensaPersonalizeTab.FAVOURITES -> favourites()
                MensaPersonalizeTab.ALLERGENS -> allergens()
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(100)),
        ) {
            SegmentedSwitch(
                options = MensaPersonalizeTab.entries,
                selected = tab,
                onSelected = { selected ->
                    haptic.tap()
                    scope.launch { pagerState.animateScrollToPage(selected.ordinal) }
                },
                label = {
                    stringResource(
                        if (it == MensaPersonalizeTab.FAVOURITES) R.string.mensa_favourites_title else R.string.mensa_allergens_title,
                    )
                },
                borderColor = Color.White.copy(alpha = 0.5f),
            )
        }
    }
}

/** Lookahead choices offered: today only, today and tomorrow, or every day the canteens publish. */
private val LookaheadChoices = listOf(0, 1, 4)

/**
 * The dishes the user likes, and whether (and at what time of day) to be told when one is on a menu. Dishes
 * are added from a dish's page with its heart, or from all dishes.
 */
@Composable
internal fun MensaFavouritesPage(
    favourites: List<MensaFavourite>,
    settings: MensaFavouriteSettings,
    onRemove: (MensaFavourite) -> Unit,
    onBrowseDishes: () -> Unit,
    onNotifyChange: (Boolean) -> Unit,
    onLookaheadChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onNotifyTimeChange: (LocalTime) -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    val context = LocalContext.current
    val locale = currentLocale()
    var pickingTime by rememberSaveable { mutableStateOf(false) }
    // Turning the reminder on asks for the notification permission first, where the system wants one.
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) onNotifyChange(true)
    }
    fun enable() {
        val needsAsk = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (needsAsk) permission.launch(Manifest.permission.POST_NOTIFICATIONS) else onNotifyChange(true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp)
            .testTag(MensaTestTags.FAVOURITES_PAGE),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (favourites.isEmpty()) {
            Surface(shape = RoundedCornerShape(20.dp), color = scheme.surfaceContainerHigh) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, tint = scheme.primary)
                    Text(
                        text = stringResource(R.string.mensa_favourites_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            favourites.forEach { favourite ->
                Surface(shape = RoundedCornerShape(20.dp), color = scheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        val art = MensaDishArtwork.forName(favourite.name) ?: MensaDishArtwork.forCourse(null)
                        MensaPlate(art = art, size = 48.dp, drawable = mensaDishDrawable(favourite.dishId, art.drawable))
                        Text(
                            text = favourite.name.toDishDisplayName(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { haptic.tap(); onRemove(favourite) }) {
                            Icon(
                                imageVector = Icons.Rounded.Favorite,
                                contentDescription = stringResource(R.string.mensa_favourite_remove),
                                tint = scheme.primary,
                            )
                        }
                    }
                }
            }
        }

        FilledTonalButton(onClick = { haptic.tap(); onBrowseDishes() }, modifier = Modifier.align(Alignment.Start)) {
            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(stringResource(R.string.mensa_favourites_browse), modifier = Modifier.padding(start = 6.dp))
        }

        // The reminder lives with the favourites it is about.
        Surface(shape = RoundedCornerShape(20.dp), color = scheme.surfaceContainerHigh, modifier = Modifier.padding(top = 6.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.mensa_favourites_notify_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.mensa_favourites_notify_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = settings.notify,
                        onCheckedChange = { on -> haptic.tap(); if (on) enable() else onNotifyChange(false) },
                        modifier = Modifier.testTag(MensaTestTags.FAVOURITES_NOTIFY),
                    )
                }
                AnimatedVisibility(visible = settings.notify) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { haptic.tap(); pickingTime = true }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            Text(
                                text = stringResource(R.string.mensa_favourites_notify_time),
                                style = MaterialTheme.typography.labelLarge,
                                color = scheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = settings.notifyAt.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = scheme.primary,
                                modifier = Modifier.testTag(MensaTestTags.FAVOURITES_NOTIFY_TIME),
                            )
                        }
                        Text(
                            text = stringResource(R.string.mensa_favourites_lookahead),
                            style = MaterialTheme.typography.labelLarge,
                            color = scheme.onSurfaceVariant,
                        )
                        SegmentedSwitch(
                            options = LookaheadChoices,
                            selected = LookaheadChoices.minBy { kotlin.math.abs(it - settings.lookaheadDays) },
                            onSelected = { haptic.tap(); onLookaheadChange(it) },
                            label = {
                                stringResource(
                                    when (it) {
                                        0 -> R.string.mensa_favourites_lookahead_today
                                        1 -> R.string.mensa_favourites_lookahead_tomorrow
                                        else -> R.string.mensa_favourites_lookahead_all
                                    },
                                )
                            },
                            modifier = Modifier.heightIn(min = 40.dp),
                        )
                    }
                }
            }
        }
    }

    if (pickingTime) {
        NotifyTimeDialog(
            initial = settings.notifyAt,
            onDismiss = { pickingTime = false },
            onConfirm = { time ->
                pickingTime = false
                onNotifyTimeChange(time)
            },
        )
    }
}

/** Picks the time of the daily notification, in the phone's 12 or 24 hour clock. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotifyTimeDialog(initial: LocalTime, onDismiss: () -> Unit, onConfirm: (LocalTime) -> Unit) {
    val context = LocalContext.current
    val state = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = DateFormat.is24HourFormat(context),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.mensa_favourites_notify_time_title)) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) { Text(stringResource(android.R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        },
    )
}

@Preview(name = "Favourites", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 640)
@Composable
private fun FavouritesPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaFavouritesPage(
            favourites = listOf(MensaFavourite(38612, "PASTA AL POMODORO"), MensaFavourite(88465, "PIZZA MARGHERITA")),
            settings = MensaFavouriteSettings(notify = true, lookaheadDays = 1),
            onRemove = {},
            onBrowseDishes = {},
            onNotifyChange = {},
            onLookaheadChange = {},
        )
    }
}

@Preview(name = "Favourites · empty", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 420)
@Composable
private fun FavouritesEmptyPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaFavouritesPage(
            favourites = emptyList(),
            settings = MensaFavouriteSettings(),
            onRemove = {},
            onBrowseDishes = {},
            onNotifyChange = {},
            onLookaheadChange = {},
        )
    }
}

@Preview(name = "Personalize", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 720)
@Composable
private fun PersonalizePreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaPersonalizePage(
            pagerState = rememberPagerState { MensaPersonalizeTab.entries.size },
            favourites = {
                MensaFavouritesPage(
                    favourites = listOf(MensaFavourite(38612, "PASTA AL POMODORO")),
                    settings = MensaFavouriteSettings(),
                    onRemove = {},
                    onBrowseDishes = {},
                    onNotifyChange = {},
                    onLookaheadChange = {},
                )
            },
            allergens = {},
        )
    }
}
