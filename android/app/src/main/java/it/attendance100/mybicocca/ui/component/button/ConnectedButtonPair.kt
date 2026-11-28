package it.attendance100.mybicocca.ui.component.button

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme

/** How tall the pair's buttons are. */
private val PairHeight = 56.dp

/** Narrower than a lone button's, so an icon and a two-word label fit the smaller half. */
private val PairContentPadding = PaddingValues(horizontal = 12.dp)

/**
 * The joined pair at the bottom of a step or a booking, as in the appointment form: a neutral
 * tonal [secondary] button leading and a wider, brand-filled [primary] one trailing. With no
 * [primary], the secondary button stands alone at full width.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConnectedButtonPair(
    secondary: @Composable RowScope.() -> Unit,
    onSecondary: () -> Unit,
    modifier: Modifier = Modifier,
    primary: (@Composable RowScope.() -> Unit)? = null,
    onPrimary: () -> Unit = {},
    secondaryEnabled: Boolean = true,
    primaryEnabled: Boolean = true,
    secondaryColors: ButtonColors = ConnectedButtonPairDefaults.secondaryColors(),
    primaryColors: ButtonColors = ConnectedButtonPairDefaults.primaryColors(),
) {
    val haptic = rememberHapticManager()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FilledTonalButton(
            onClick = { haptic.tap(); onSecondary() },
            enabled = secondaryEnabled,
            modifier = Modifier
                .weight(1f)
                .height(PairHeight),
            shape = if (primary != null) ButtonGroupDefaults.connectedLeadingButtonShape else ButtonDefaults.filledTonalShape,
            colors = secondaryColors,
            contentPadding = PairContentPadding,
            content = secondary,
        )
        if (primary != null) {
            Button(
                onClick = { haptic.tap(); onPrimary() },
                enabled = primaryEnabled,
                modifier = Modifier
                    .weight(1.4f)
                    .height(PairHeight),
                shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                colors = primaryColors,
                contentPadding = PairContentPadding,
                content = primary,
            )
        }
    }
}

/** An icon and a one-line label, the usual content of a [ConnectedButtonPair] button. */
@Composable
fun RowScope.PairButtonLabel(text: String, icon: ImageVector? = null) {
    if (icon != null) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
    }
    Text(text, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

object ConnectedButtonPairDefaults {
    @Composable
    fun secondaryColors(): ButtonColors {
        val scheme = MaterialTheme.colorScheme
        return ButtonDefaults.filledTonalButtonColors(
            containerColor = scheme.surfaceContainerHighest,
            contentColor = scheme.onSurface,
        )
    }

    /** For a secondary action that undoes something, such as cancelling a booking. */
    @Composable
    fun destructiveColors(): ButtonColors {
        val scheme = MaterialTheme.colorScheme
        return ButtonDefaults.filledTonalButtonColors(
            containerColor = scheme.errorContainer,
            contentColor = scheme.onErrorContainer,
        )
    }

    @Composable
    fun primaryColors(): ButtonColors {
        val scheme = MaterialTheme.colorScheme
        val dark = LocalDarkTheme.current
        return ButtonDefaults.buttonColors(
            containerColor = if (dark) scheme.primaryContainer else scheme.primary,
            contentColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
        )
    }
}

@Preview(name = "Button pair, light")
@Preview(name = "Button pair, dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConnectedButtonPairPreview() {
    val dark = (LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    BicoccaTheme(dark = dark) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ConnectedButtonPair(
                    secondary = { PairButtonLabel("Back") },
                    onSecondary = {},
                    primary = { PairButtonLabel("Confirm order") },
                )
                ConnectedButtonPair(
                    secondary = { PairButtonLabel("Cancel order", Icons.Outlined.Cancel) },
                    onSecondary = {},
                    primary = { PairButtonLabel("Show on map", Icons.Outlined.Place) },
                    secondaryColors = ConnectedButtonPairDefaults.destructiveColors(),
                )
                ConnectedButtonPair(
                    secondary = { PairButtonLabel("Cancel order", Icons.Outlined.Cancel) },
                    onSecondary = {},
                    secondaryColors = ConnectedButtonPairDefaults.destructiveColors(),
                )
            }
        }
    }
}
