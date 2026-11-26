package it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.component

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.core.net.toUri
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.input.ConsentCheckboxCard
import it.attendance100.mybicocca.domain.model.appointment.AppointmentFormField

/**
 * The GDPR consent, as the app's other consents ([ConsentCheckboxCard]): a card that ticks its
 * checkbox, the privacy notice reference rendered inline as a red, tappable link. Stores "true"/"" in the form
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
    val linkStyles = TextLinkStyles(SpanStyle(color = scheme.primary, fontWeight = FontWeight.SemiBold))
    val openPolicy: (String) -> Unit = { url ->
        CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, url.toUri())
    }

    val gdprPrefix = stringResource(R.string.registry_gdpr_prefix)
    val gdprPolicyLink = stringResource(R.string.registry_gdpr_policy_link)
    val gdprSuffix = stringResource(R.string.registry_gdpr_suffix)

    val statement = buildAnnotatedString {
        if (field.label == "gdpr_label") {
            append(gdprPrefix)
            val url = field.policyUrl
            if (url != null) {
                withLink(LinkAnnotation.Clickable("policy", linkStyles) { openPolicy(url) }) {
                    append(gdprPolicyLink)
                }
            } else {
                append(gdprPolicyLink)
            }
            append(gdprSuffix)
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

    ConsentCheckboxCard(
        checked = accepted,
        onCheckedChange = { onValueChange(field.code, if (it) "true" else "") },
        text = statement,
        enabled = enabled,
        modifier = modifier,
    )
}
