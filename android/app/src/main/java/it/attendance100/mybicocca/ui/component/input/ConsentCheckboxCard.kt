package it.attendance100.mybicocca.ui.component.input

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.theme.BicoccaTheme

/**
 * Agreeing to a notice, the same everywhere in the app: a card whose whole surface ticks its
 * checkbox, with the notice's name as a link inside [text] that opens it instead.
 */
@Composable
fun ConsentCheckboxCard(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val haptic = rememberHapticManager()
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .toggleable(value = checked, enabled = enabled, role = Role.Checkbox) {
                    haptic.tap()
                    onCheckedChange(it)
                }
                .padding(end = 16.dp, top = 4.dp, bottom = 4.dp),
        ) {
            Checkbox(checked = checked, onCheckedChange = null, enabled = enabled, modifier = Modifier.padding(12.dp))
            Text(text = text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

@Preview(name = "Consent, light")
@Preview(name = "Consent, dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConsentCheckboxCardPreview() {
    val dark = (androidx.compose.ui.platform.LocalConfiguration.current.uiMode and
        android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
    BicoccaTheme(dark = dark) {
        Surface {
            ConsentCheckboxCard(
                checked = true,
                onCheckedChange = {},
                text = AnnotatedString("I have read the privacy notice"),
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
