package it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.appointment.AppointmentFormField
import kotlinx.coroutines.delay

/**
 * Renders the server-defined booking form fields; values live in the ViewModel keyed by field
 * code. Consent fields are skipped here — the FormPage renders the GDPR consent separately as
 * a switch row. The field matching [autoFocusCode] grabs focus and raises the IME after a
 * short delay that lets the sheet's page transition settle.
 */
@Composable
internal fun BookingForm(
    fields: List<AppointmentFormField>,
    values: Map<String, String>,
    enabled: Boolean,
    onValueChange: (code: String, value: String) -> Unit,
    modifier: Modifier = Modifier,
    autoFocusCode: String? = null,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(autoFocusCode) {
        if (autoFocusCode != null) {
            delay(250)
            runCatching { focusRequester.requestFocus() }
        }
    }

    fun fieldModifier(code: String): Modifier =
        if (code == autoFocusCode) Modifier.focusRequester(focusRequester) else Modifier

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        fields.forEach { field ->
            when (field) {
                is AppointmentFormField.Email -> TextFieldRow(
                    field = field,
                    value = values[field.code].orEmpty(),
                    enabled = enabled,
                    keyboardType = KeyboardType.Email,
                    onValueChange = onValueChange,
                    modifier = fieldModifier(field.code),
                )

                is AppointmentFormField.Text -> TextFieldRow(
                    field = field,
                    value = values[field.code].orEmpty(),
                    enabled = enabled,
                    keyboardType = KeyboardType.Text,
                    onValueChange = onValueChange,
                    modifier = fieldModifier(field.code),
                )

                is AppointmentFormField.Phone -> TextFieldRow(
                    field = field,
                    value = values[field.code].orEmpty(),
                    enabled = enabled,
                    keyboardType = KeyboardType.Phone,
                    onValueChange = onValueChange,
                    modifier = fieldModifier(field.code),
                )

                is AppointmentFormField.TextArea -> TextFieldRow(
                    field = field,
                    value = values[field.code].orEmpty(),
                    enabled = enabled,
                    keyboardType = KeyboardType.Text,
                    onValueChange = onValueChange,
                    minLines = 3,
                    maxLines = 6,
                    modifier = fieldModifier(field.code),
                )

                is AppointmentFormField.Select -> SelectFieldRow(
                    field = field,
                    value = values[field.code],
                    enabled = enabled,
                    onValueChange = onValueChange,
                )

                is AppointmentFormField.Consent -> Unit
            }
        }
    }
}

@Composable
private fun TextFieldRow(
    field: AppointmentFormField,
    value: String,
    enabled: Boolean,
    keyboardType: KeyboardType,
    onValueChange: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    maxLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(field.code, it) },
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        label = { Text(field.requiredLabel()) },
        placeholder = field.placeholder?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = maxLines == 1,
        minLines = minLines,
        maxLines = maxLines,
        enabled = enabled,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectFieldRow(
    field: AppointmentFormField.Select,
    value: String?,
    enabled: Boolean,
    onValueChange: (String, String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = field.options.firstOrNull { it.value == value }?.label.orEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled),
            shape = MaterialTheme.shapes.large,
            label = { Text(field.requiredLabel()) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            enabled = enabled,
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            field.options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onValueChange(field.code, option.value)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

private fun AppointmentFormField.requiredLabel(): String =
    if (required) "$label *" else label
