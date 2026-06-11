package it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.appointment.AppointmentFormField
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.AppointmentsViewModel
import it.attendance100.mybicocca.ui.theme.LocalIsOnline

/**
 * Booking wizard step 2: the dynamic form (the consent rendered as its own switch row below
 * the fields), then the Indietro / Prenota action pair pinned at the bottom. The sheet header
 * carries the picked date and time, so there is no recap card here; Prenota enables only once
 * every field is satisfied and the device is online.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun FormPage(
    viewModel: AppointmentsViewModel,
    modifier: Modifier = Modifier,
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val values by viewModel.values.collectAsStateWithLifecycle()
    val submitting by viewModel.submitting.collectAsStateWithLifecycle()
    val autoFocusCode by viewModel.autoFocusFieldCode.collectAsStateWithLifecycle()

    val fields = form.valueOrNull()?.fields.orEmpty()
    val consent = fields.filterIsInstance<AppointmentFormField.Consent>().firstOrNull()
    val canSubmit = !submitting && fields.isNotEmpty() && fields.all { it.isSatisfiedBy(values) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 720.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BookingForm(
                fields = fields,
                values = values,
                enabled = !submitting,
                autoFocusCode = autoFocusCode,
                onValueChange = viewModel::setFieldValue,
            )

            consent?.let {
                ConsentSwitchRow(
                    field = it,
                    accepted = values[it.code] == "true",
                    enabled = !submitting,
                    onValueChange = viewModel::setFieldValue,
                )
            }
        }

        ActionButtons(
            canSubmit = canSubmit,
            submitting = submitting,
            onSubmit = viewModel::submit,
            onBack = viewModel::back,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 24.dp),
        )
    }
}

/** Connected pair: tonal "Indietro" leads (1f), brand-filled "Prenota" trails (1.4f) and shows the in-flight indicator while submitting. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ActionButtons(
    canSubmit: Boolean,
    submitting: Boolean,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FilledTonalButton(
            onClick = onBack,
            enabled = !submitting,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = ButtonGroupDefaults.connectedLeadingButtonShape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = scheme.surfaceContainerHighest,
                contentColor = scheme.onSurface,
            ),
        ) {
            Text(stringResource(R.string.appointments_back), fontWeight = FontWeight.SemiBold)
        }
        Button(
            onClick = onSubmit,
            enabled = canSubmit && LocalIsOnline.current,
            modifier = Modifier
                .weight(1.4f)
                .height(56.dp),
            shape = ButtonGroupDefaults.connectedTrailingButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (dark) scheme.primaryContainer else scheme.primary,
                contentColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
            ),
        ) {
            if (submitting) {
                LoadingIndicator(
                    modifier = Modifier.size(24.dp),
                    color = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
                )
            } else {
                Text(
                    stringResource(R.string.appointments_book_final),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Whether the field's current value allows submitting: required consents must be accepted,
 * email fields must parse as an address when required or primary (optional ones may stay
 * blank), and any other required field must be non-blank.
 */
private fun AppointmentFormField.isSatisfiedBy(values: Map<String, String>): Boolean {
    val value = values[code].orEmpty()
    return when (this) {
        is AppointmentFormField.Consent -> !required || value == "true"
        is AppointmentFormField.Email -> {
            val presentAndValid = value.contains('@') && value.substringAfter('@').contains('.')
            if (required || primary) presentAndValid else value.isBlank() || presentAndValid
        }
        else -> !required || value.isNotBlank()
    }
}
