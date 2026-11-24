package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.runtime.Composable
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.remember
import androidx.compose.material3.ripple
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.indication
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.mensa.MensaSession
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaArt
import it.attendance100.mybicocca.domain.model.mensa.MensaReservation
import it.attendance100.mybicocca.core.os.currentLocale
import it.attendance100.mybicocca.core.time.AppClock
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Landing page of the Mensa sheet: the two things people come for, plus account shortcuts. */
@Composable
internal fun MensaHubPage(
    session: MensaSession?,
    allergenPreferences: MensaAllergenPreferences,
    favouriteCount: Int,
    upcomingBooking: MensaReservation?,
    onOpenMenu: () -> Unit,
    onOpenBooking: (MensaReservation) -> Unit,
    onOpenOrders: () -> Unit,
    onOpenAllergens: () -> Unit,
    onOpenWallet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
            .testTag(MensaTestTags.HOME_PAGE),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val booking = upcomingBooking?.takeIf { session != null }
        if (booking != null) {
            // A booking to pick up matters as much as the menu, so it shares the menu's row.
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HubBox(
                    tag = MensaTestTags.HUB_MENU,
                    title = stringResource(R.string.mensa_hub_menu_title),
                    body = stringResource(R.string.mensa_hub_menu_body),
                    onClick = onOpenMenu,
                    modifier = Modifier.weight(1f),
                ) { HubPlates(listOf(MensaArt.PASTA_TOMATO, MensaArt.SALAD)) }
                BookingBox(booking = booking, onClick = { onOpenBooking(booking) }, modifier = Modifier.weight(1f))
            }
        } else {
            HubBox(
                tag = MensaTestTags.HUB_MENU,
                title = stringResource(R.string.mensa_hub_menu_title),
                body = stringResource(R.string.mensa_hub_menu_body),
                onClick = onOpenMenu,
                modifier = Modifier.fillMaxWidth(),
            ) { HubPlates(listOf(MensaArt.PASTA_TOMATO, MensaArt.MEAT, MensaArt.SALAD, MensaArt.SANDWICH)) }
            HubRow(
                tag = MensaTestTags.HUB_ORDERS,
                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                title = stringResource(R.string.mensa_orders_title),
                body = stringResource(R.string.mensa_orders_subtitle),
                trailing = Icons.Outlined.ChevronRight,
                onClick = onOpenOrders,
            )
        }

        HubRow(
            tag = "mensa_hub_wallet",
            icon = Icons.Outlined.AccountBalanceWallet,
            title = stringResource(R.string.mensa_hub_wallet_title),
            body = session?.walletBalance
                ?.let { stringResource(R.string.mensa_hub_wallet_body_balance, it) }
                ?: stringResource(R.string.mensa_hub_wallet_body),
            trailing = Icons.AutoMirrored.Outlined.OpenInNew,
            onClick = onOpenWallet,
        )

        HubRow(
            tag = MensaTestTags.HUB_PERSONALIZE,
            icon = MensaAllergenIcon,
            title = stringResource(R.string.mensa_hub_personalize_title),
            body = listOfNotNull(
                favouriteCount.takeIf { it > 0 }?.let { pluralStringResource(R.plurals.mensa_hub_favourites, it, it) },
                allergenPreferences.allergenIds.size.takeIf { it > 0 }?.let {
                    pluralStringResource(R.plurals.mensa_hub_personalize_active, it, it)
                },
            ).joinToString(" · ").ifBlank { stringResource(R.string.mensa_hub_personalize_body) },
            trailing = Icons.Outlined.ChevronRight,
            onClick = onOpenAllergens,
        )
    }
}

/**
 * A big card with its artwork (plates, the bag) overflowing its top edge; the artwork answers the
 * tap too. [label] is a small line above the title.
 */
@Composable
private fun HubBox(
    tag: String,
    title: String,
    body: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    artwork: @Composable BoxScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    val press = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .height(184.dp)
            .testTag(tag)
            .clickable(interactionSource = press, indication = null) { haptic.tap(); onClick() },
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 44.dp),
            shape = RoundedCornerShape(28.dp),
            color = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .indication(press, ripple())
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                if (label != null) {
                    Text(text = label, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        artwork()
    }
}

/** A string of plates at the card's top-right, staggered so it reads as a spread of dishes. */
@Composable
private fun BoxScope.HubPlates(arts: List<MensaArt>) {
    Row(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 6.dp),
        horizontalArrangement = Arrangement.spacedBy((-22).dp),
    ) {
        arts.forEachIndexed { index, art ->
            MensaPlate(
                art = art,
                size = 92.dp,
                modifier = Modifier.offset(y = if (index % 2 == 0) 0.dp else 14.dp),
            )
        }
    }
}

/** The next booking to pick up, as big as the menu card: when, which meal, where, and its code. */
@Composable
private fun BookingBox(booking: MensaReservation, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val locale = currentLocale()
    val today = remember { LocalDate.now(AppClock.clock) }
    val day = when (booking.date) {
        today -> stringResource(R.string.mensa_today)
        today.plusDays(1) -> stringResource(R.string.mensa_tomorrow)
        null -> booking.rawDate
        else -> booking.date.format(DateTimeFormatter.ofPattern("EEE d MMM", locale)).replaceFirstChar { it.titlecase(locale) }
    }
    HubBox(
        tag = MensaTestTags.HUB_BOOKING,
        label = stringResource(R.string.mensa_hub_booking_label),
        title = listOfNotNull(day, booking.mealTypeName).joinToString(" · "),
        // The pickup slot, then the code on a line of its own: it is long and must not break.
        body = listOfNotNull(booking.shiftName?.takeIf { it.isNotBlank() }, booking.code).joinToString("\n"),
        onClick = onClick,
        modifier = modifier,
    ) {
        Image(
            painter = painterResource(R.drawable.mensa_art_takeaway_bag),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(104.dp),
        )
    }
}

@Composable
private fun HubRow(
    tag: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    trailing: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = scheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(tag)
                .clickable { haptic.tap(); onClick() }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(shape = RoundedCornerShape(14.dp), color = scheme.secondaryContainer, modifier = Modifier.size(42.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = scheme.onSecondaryContainer, modifier = Modifier.size(22.dp))
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(trailing, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}

/** Header action: a sign-in pill while signed out, the user's initials once signed in. */
@Composable
internal fun MensaAccountAction(
    session: MensaSession?,
    onLogin: () -> Unit,
    onOpenAccount: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    val accountLabel = stringResource(R.string.mensa_account_title)
    // The header lets trailing actions overhang its end inset, which suits icon buttons; these two
    // are wider and need the room back.
    if (session == null) {
        FilledTonalButton(onClick = { haptic.tap(); onLogin() }, modifier = Modifier.padding(end = 12.dp).testTag(MensaTestTags.ACCOUNT_BUTTON)) {
            Icon(Icons.AutoMirrored.Outlined.Login, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.mensa_login_action))
        }
    } else {
        Surface(
            shape = CircleShape,
            color = scheme.primaryContainer,
            modifier = Modifier
                .padding(end = 12.dp)
                .size(42.dp)
                .testTag(MensaTestTags.ACCOUNT_BUTTON)
                .clickable { haptic.tap(); onOpenAccount() },
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.semantics { contentDescription = accountLabel },
            ) {
                Text(
                    text = session.initials(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onPrimaryContainer,
                )
            }
        }
    }
}

private fun MensaSession.initials(): String {
    val source = userName?.takeIf { it.isNotBlank() } ?: email.substringBefore('@')
    val letters = source.split(' ', '.', '_', '-').filter { it.isNotBlank() }.map { it.first().uppercaseChar() }
    return when {
        letters.size >= 2 -> "${letters[0]}${letters[1]}"
        letters.size == 1 -> letters[0].toString()
        else -> "?"
    }
}

@Preview(name = "Mensa home · signed in", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 640)
@Composable
private fun HubSignedInPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaHubPage(
            session = MensaPreviewData.session,
            allergenPreferences = MensaPreviewData.glutenSelected,
            onOpenMenu = {},
            favouriteCount = 2,
            upcomingBooking = MensaPreviewOrders.reservation,
            onOpenBooking = {},
            onOpenOrders = {},
            onOpenAllergens = {},
            onOpenWallet = {},
        )
    }
}

@Preview(name = "Mensa home · signed out", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 560)
@Composable
private fun HubSignedOutPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaHubPage(
            session = null,
            allergenPreferences = MensaAllergenPreferences(),
            onOpenMenu = {},
            favouriteCount = 2,
            upcomingBooking = MensaPreviewOrders.reservation,
            onOpenBooking = {},
            onOpenOrders = {},
            onOpenAllergens = {},
            onOpenWallet = {},
        )
    }
}
