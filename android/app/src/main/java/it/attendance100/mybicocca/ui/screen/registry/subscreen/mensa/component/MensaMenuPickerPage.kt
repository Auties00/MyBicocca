package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.ripple
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuIdentity
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuKind
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuSummary
import it.attendance100.mybicocca.domain.model.mensa.MensaPlace
import it.attendance100.mybicocca.domain.model.mensa.identity
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.friendlyMessage
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MENSA_MENU_ART_STYLE
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaMenuCard
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.res.painterResource
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MENSA_MENU_CARD_ASPECT
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.mensaMenuCardArt

/** First step of the menu flow: where. One card per canteen and bar; tapping one moves on. */
@Composable
internal fun MensaPlacePickerPage(
    onPick: (MensaPlace) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticManager()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(MensaTestTags.PLACE_PICKER_PAGE)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MensaPlace.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { place ->
                    MensaPickerCard(
                        identity = place.identity(),
                        place = place,
                        subtitle = when {
                            !place.isCanteen -> stringResource(R.string.mensa_menu_card_building, place.building)
                            place.lunchOnly -> stringResource(R.string.mensa_place_lunch_only)
                            else -> stringResource(R.string.mensa_place_lunch_dinner)
                        },
                        onClick = { haptic.tap(); onPick(place) },
                        fromFan = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag(MensaTestTags.placeCard(place.name)),
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/** Second step, for the canteens: the menu or its gluten-free variant, of the place just chosen. */
@Composable
internal fun MensaMenuPickerPage(
    menus: Loadable<List<MensaMenuSummary>>,
    menusStatus: SyncStatus,
    place: MensaPlace?,
    onPick: (Int) -> Unit,
    onRetryMenus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticManager()
    val list = menus.valueOrNull().orEmpty().filter { place == null || it.id in place.menuIds }
    val ordered = remember(list) {
        list.sortedWith(
            compareBy<MensaMenuSummary>(
                { it.identity().kind.ordinal },
                { it.identity().building?.filter(Char::isDigit)?.toIntOrNull() ?: 0 },
            ),
        )
    }

    Column(modifier = modifier.fillMaxWidth().testTag(MensaTestTags.MENU_PICKER_PAGE)) {
        when {
            list.isEmpty() && menusStatus is SyncStatus.Failed -> SheetMessage(
                icon = Icons.Outlined.CloudOff,
                title = stringResource(R.string.mensa_menus_load_failed),
                body = stringResource(menusStatus.cause.friendlyMessage()),
                action = { RetryButton(onClick = onRetryMenus) },
            )

            menus is Loadable.NotYetLoaded && list.isEmpty() -> Box(Modifier.fillMaxWidth().height(260.dp)) {
                SheetLoadingIndicator(
                    label = stringResource(R.string.mensa_loading_menus),
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            list.isEmpty() -> EmptyState(
                icon = Icons.Outlined.Restaurant,
                title = stringResource(R.string.mensa_no_menus),
                body = "",
                modifier = Modifier.padding(24.dp),
            )

            else -> Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ordered.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { menu ->
                            MensaPickerCard(
                                identity = menu.identity(),
                                place = place,
                                subtitle = place?.displayName.orEmpty(),
                                onClick = { haptic.tap(); onPick(menu.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(MensaTestTags.menuCard(menu.id)),
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private val ArtWidth = 104.dp
private const val ArtAspect = MENSA_MENU_CARD_ASPECT
private val ArtHeight = ArtWidth / ArtAspect
private val ArtOutside = ArtHeight * 0.55f

/**
 * A menu card in the dish card's mould: a plain surface with the menu's artwork standing over its
 * top edge, then the menu's name and where it is served. The artwork is a sibling drawn after the
 * surface, so nothing clips the part that overflows.
 */
@Composable
private fun MensaPickerCard(
    identity: MensaMenuIdentity,
    place: MensaPlace?,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fromFan: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val art = mensaMenuCardArt(identity, place)
    // The whole card answers the tap, the artwork over its edge included.
    val press = remember { MutableInteractionSource() }
    Box(modifier = modifier.clickable(interactionSource = press, indication = null, onClick = onClick)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = ArtOutside),
            shape = RoundedCornerShape(28.dp),
            color = scheme.surfaceContainerHigh,
        ) {
            Column(
                modifier = Modifier
                    .indication(press, ripple())
                    .padding(start = 14.dp, end = 14.dp, top = ArtHeight - ArtOutside + 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = identity.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Image(
            painter = painterResource(art),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .then(if (fromFan) Modifier.mensaMenuCardHero(art) else Modifier)
                .width(ArtWidth)
                .aspectRatio(ArtAspect),
        )
    }
}

@Preview(name = "Menu picker", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 760)
@Composable
private fun MenuPickerPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaMenuPickerPage(
            menus = Loadable.Loaded(MensaPreviewData.menus),
            menusStatus = SyncStatus.Idle,
            place = MensaPlace.CANTEEN_U12,
            onPick = {},
            onRetryMenus = {},
        )
    }
}

@Preview(name = "Place picker", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 760)
@Composable
private fun PlacePickerPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) { MensaPlacePickerPage(onPick = {}) }
}
