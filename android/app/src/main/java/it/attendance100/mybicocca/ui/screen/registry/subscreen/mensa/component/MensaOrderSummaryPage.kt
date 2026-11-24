package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.mensa.MensaCartPricing
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaCartEntry
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaDishArtwork
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaDishDrawings
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme
import androidx.compose.material.icons.outlined.Delete

/**
 * The order as it stands, before pickup is chosen. It needs no account: the dishes can be
 * reviewed and changed here, and only continuing asks for sign-in, so a signed-out user sees what
 * they are about to order before committing to anything.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MensaOrderSummaryPage(
    cartDishes: Map<Int, MensaCartEntry>,
    cartPricing: Loadable<MensaCartPricing>,
    signedIn: Boolean,
    pricingError: String?,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit,
    onBrowseDishes: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    singlePick: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val haptic = rememberHapticManager()
    val entries = cartDishes.values.toList()
    val count = entries.sumOf { it.quantity }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(MensaTestTags.ORDER_SUMMARY_PAGE),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (entries.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = scheme.surfaceContainer,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.mensa_summary_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                        )
                        FilledTonalButton(onClick = { haptic.tap(); onBrowseDishes() }) {
                            Icon(Icons.Outlined.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("  " + stringResource(R.string.mensa_takeaway_browse_dishes))
                        }
                    }
                }
            } else {
                entries.forEach { entry ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = scheme.surfaceContainer,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            MensaPlate(
                                art = MensaDishArtwork.forDish(entry.dish),
                                size = 52.dp,
                                drawable = MensaDishDrawings.of(entry.dish.id) ?: MensaDishArtwork.forDish(entry.dish).drawable,
                            )
                            Text(
                                text = entry.dish.name.toDishDisplayName(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            if (singlePick) {
                                FilledTonalIconButton(
                                    onClick = { haptic.tap(); onDecrement(entry.dish.id) },
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.mensa_quantity_remove), modifier = Modifier.size(18.dp))
                                }
                            } else Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                FilledTonalIconButton(
                                    onClick = { haptic.tap(); onDecrement(entry.dish.id) },
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(Icons.Outlined.Remove, contentDescription = stringResource(R.string.mensa_quantity_decrease), modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = "${entry.quantity}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                FilledTonalIconButton(
                                    onClick = { haptic.tap(); onIncrement(entry.dish.id) },
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.mensa_quantity_increase), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = scheme.surfaceContainerHigh,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = pluralStringResource(R.plurals.mensa_summary_dishes, count, count),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            // The price comes from the service chosen next, so it only shows once known. The
                            // canteen sends none, being paid at the till, so a zero means there is no price.
                            (cartPricing as? Loadable.Loaded)?.value?.takeIf { it.total > 0.0 }?.let { pricing ->
                                Text(
                                    text = "${pricing.currencySymbol} ${"%.2f".format(pricing.total)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.primary,
                                )
                            }
                        }
                    }
                }

                if (!signedIn) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = scheme.surfaceContainerHigh,
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Icon(Icons.Outlined.Info, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(22.dp))
                            Text(
                                text = stringResource(R.string.mensa_summary_signin_note),
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        pricingError?.let {
            MensaOrderProblem(text = it, modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp))
        }
        if (entries.isNotEmpty()) {
            val bg = if (dark) scheme.primaryContainer else scheme.primary
            val fg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary
            Button(
                onClick = { haptic.tap(); onContinue() },
                enabled = pricingError == null,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp)
                    .heightIn(min = ButtonDefaults.MediumContainerHeight),
                colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
            ) {
                Icon(Icons.Outlined.ShoppingBag, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(
                    text = "  " + stringResource(if (signedIn) R.string.mensa_summary_continue else R.string.mensa_summary_continue_signin),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/** What stops the order from going through, as the canteen put it: a combination it does not allow, a short wallet. */
@Composable
internal fun MensaOrderProblem(text: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = scheme.errorContainer,
        contentColor = scheme.onErrorContainer,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(Icons.Outlined.ErrorOutline, contentDescription = null, modifier = Modifier.size(22.dp))
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(name = "Order problem", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 160)
@Composable
private fun OrderProblemPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaOrderProblem(
            text = "I seguenti extra non possono essere prenotati: PASTA ALLA CARBONARA (Primi Piatti).",
            modifier = Modifier.padding(20.dp),
        )
    }
}

@Preview(name = "Order summary", widthDp = MENSA_PREVIEW_WIDTH, heightDp = MENSA_PREVIEW_HEIGHT)
@Composable
private fun OrderSummaryPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaOrderSummaryPage(
            cartDishes = mapOf(
                MensaPreviewData.plainDish.id to MensaCartEntry(MensaPreviewData.plainDish, 1),
                MensaPreviewData.containsDish.id to MensaCartEntry(MensaPreviewData.containsDish, 2),
                MensaPreviewData.dishes[3].id to MensaCartEntry(MensaPreviewData.dishes[3], 1),
            ),
            cartPricing = Loadable.NotYetLoaded,
            signedIn = false,
            pricingError = null,
            onIncrement = {},
            onDecrement = {},
            onBrowseDishes = {},
            onContinue = {},
        )
    }
}
