package it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.component

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.appointment.AppointmentFormField

/**
 * The GDPR consent, styled like the app's other memorable-choice rows (the elearning file
 * chooser's "Ricorda la scelta"): a clickable surface with a trailing switch, the privacy
 * notice reference rendered inline as a red, tappable link. Stores "true"/"" in the form
 * values. The backend ships an untranslated "gdpr_label" key, for which local copy is
 * rendered with the notice reference as the inline link; custom labels are wrapped whole in
 * the link.
 */
@Composable
internal fun ConsentSwitchRow(
    field: AppointmentFormField.Consent,
    accepted: Boolean,
    enabled: Boolean,
    onValueChange: (code: String, value: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    val linkStyles = TextLinkStyles(SpanStyle(color = scheme.primary, fontWeight = FontWeight.SemiBold))
    val openPolicy: (String) -> Unit = { url ->
        CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, url.toUri())
    }

    val statement = buildAnnotatedString {
        if (field.label == "gdpr_label") {
            append("Ho letto ")
            val url = field.policyUrl
            if (url != null) {
                withLink(LinkAnnotation.Clickable("policy", linkStyles) { openPolicy(url) }) {
                    append("l'informativa")
                }
            } else {
                append("l'informativa")
            }
            append(" sul trattamento dei dati personali")
        } else {
            val url = field.policyUrl
            if (url != null) {
                withLink(LinkAnnotation.Clickable("policy", linkStyles) { openPolicy(url) }) {
                    append(field.label)
                }
            } else {
                append(field.label)
            }
        }
    }

    Surface(
        onClick = {
            if (enabled) {
                haptic.tap(); onValueChange(field.code, if (!accepted) "true" else "")
            }
        },
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = scheme.surfaceContainerHigh,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        ) {
            Text(
                text = statement,
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurface,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = accepted,
                onCheckedChange = { onValueChange(field.code, if (it) "true" else "") },
                enabled = enabled,
            )
        }
    }
}
