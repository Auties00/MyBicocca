package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.runtime.Composable
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergenPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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

/** Landing page of the Mensa sheet: the two things people come for, plus account shortcuts. */
@Composable
internal fun MensaHubPage(
    session: MensaSession?,
    allergenPreferences: MensaAllergenPreferences,
    onOpenMenu: () -> Unit,
    onOpenBook: () -> Unit,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HubBox(
                tag = MensaTestTags.HUB_MENU,
                title = stringResource(R.string.mensa_hub_menu_title),
                body = stringResource(R.string.mensa_hub_menu_body),
                art = MensaArt.PASTA_TOMATO,
                container = MaterialTheme.colorScheme.primaryContainer,
                content = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = onOpenMenu,
                modifier = Modifier.weight(1f),
            )
            HubBox(
                tag = MensaTestTags.HUB_BOOK,
                title = stringResource(R.string.mensa_hub_book_title),
                body = stringResource(R.string.mensa_hub_book_body),
                art = MensaArt.SANDWICH,
                container = MaterialTheme.colorScheme.tertiaryContainer,
                content = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = onOpenBook,
                modifier = Modifier.weight(1f),
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

        if (session != null) {
            HubRow(
                tag = MensaTestTags.HUB_ORDERS,
                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                title = stringResource(R.string.mensa_my_orders),
                body = stringResource(R.string.mensa_orders_subtitle),
                trailing = Icons.Outlined.ChevronRight,
                onClick = onOpenOrders,
            )
        }

        HubRow(
            tag = MensaTestTags.HUB_PERSONALIZE,
            icon = MensaAllergenIcon,
            title = stringResource(R.string.mensa_hub_personalize_title),
            body = if (allergenPreferences.isActive) {
                pluralStringResource(
                    R.plurals.mensa_hub_personalize_active,
                    allergenPreferences.allergenIds.size,
                    allergenPreferences.allergenIds.size,
                )
            } else {
                stringResource(R.string.mensa_hub_personalize_body)
            },
            trailing = Icons.Outlined.ChevronRight,
            onClick = onOpenAllergens,
        )
    }
}

/** Card with an illustration overflowing its top-right corner. */
@Composable
private fun HubBox(
    tag: String,
    title: String,
    body: String,
    art: MensaArt,
    container: Color,
    content: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticManager()
    Box(modifier = modifier.height(184.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 44.dp),
            shape = RoundedCornerShape(28.dp),
            color = container,
            contentColor = content,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(tag)
                    .clickable { haptic.tap(); onClick() }
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
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
        MensaPlate(
            art = art,
            size = 112.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp),
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
    if (session == null) {
        FilledTonalButton(onClick = { haptic.tap(); onLogin() }, modifier = Modifier.testTag(MensaTestTags.ACCOUNT_BUTTON)) {
            Icon(Icons.AutoMirrored.Outlined.Login, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.mensa_login_action))
        }
    } else {
        Surface(
            shape = CircleShape,
            color = scheme.primaryContainer,
            modifier = Modifier
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
            onOpenBook = {},
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
            onOpenBook = {},
            onOpenOrders = {},
            onOpenAllergens = {},
            onOpenWallet = {},
        )
    }
}
