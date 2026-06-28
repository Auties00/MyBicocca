package it.attendance100.mybicocca.ui.component.button

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager

/**
 * The standard retry action shared by error states across the app: expressive press morph on
 * the brand fill. White is explicit per the brand-red rule: onPrimary flips to black-on-red in
 * dark mode.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RetryButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val haptic = rememberHapticManager()
    Button(
        onClick = {
            haptic.tap()
            onClick()
        },
        modifier = modifier,
        shapes = ButtonDefaults.shapes(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
        ),
    ) {
        Text(stringResource(R.string.common_retry))
    }
}
