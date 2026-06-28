package it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.enrollment.RenewalState

/**
 * Footer action of the enrollments sheet, always brand-filled (brand red in light,
 * primaryContainer in dark — the shared footer-CTA scheme): a tappable renewal button
 * while the renewal window is open, an inert "already enrolled" confirmation once
 * enrolled, and nothing when renewal does not apply. There is no student REST submission
 * path (confirmed against the OpenAPI specs), so the renewal action honestly deep-links
 * to the official Esse3 web flow.
 */
@Composable
fun EnrollmentRenewalButton(
    state: RenewalState,
    onRenew: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    val brandFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary
    val haptic = rememberHapticManager()
    when (state) {
        is RenewalState.Renewable -> Button(
            onClick = { haptic.tap(); onRenew() },
            modifier = modifier.height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = brandBg,
                contentColor = brandFg,
            ),
            shape = RoundedCornerShape(100),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = stringResource(
                    R.string.enrollments_renew_button,
                    state.academicYear,
                    (state.academicYear + 1) % 100
                ),
                fontWeight = FontWeight.Bold,
            )
        }

        is RenewalState.Enrolled -> Surface(
            modifier = modifier.height(56.dp),
            shape = RoundedCornerShape(100),
            color = brandBg,
            contentColor = brandFg,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(
                        R.string.enrollments_enrolled_button,
                        state.academicYear,
                        (state.academicYear + 1) % 100
                    ),
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        RenewalState.NotApplicable -> Unit
    }
}
