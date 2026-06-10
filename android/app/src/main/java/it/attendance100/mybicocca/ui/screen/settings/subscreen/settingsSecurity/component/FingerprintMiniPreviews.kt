package it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsSecurity.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.os.DeviceType
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import it.attendance100.mybicocca.ui.theme.PreviewBgDark

/**
 * The fingerprint glyph at the heart of the lock-gate mock, sized per device form factor: lit
 * in the palette's primary when [enabled], dimmed to a faint neutral when not.
 */
@Composable
fun FingerprintGraphic(
    enabled: Boolean,
    deviceType: DeviceType,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Box(modifier, contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Filled.Fingerprint,
            contentDescription = null,
            tint = if (enabled) scheme.primary else scheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(
                when (deviceType) {
                    DeviceType.Phone -> 64.dp
                    DeviceType.Foldable -> 78.dp
                    DeviceType.Tablet -> 86.dp
                },
            ),
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = PhonePreviewW,
    heightDp = PhonePreviewH,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun FingerprintGraphicOnPreview() {
    BicoccaTheme(dark = true) {
        FingerprintGraphic(
            enabled = true,
            deviceType = DeviceType.Phone,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = PhonePreviewW,
    heightDp = PhonePreviewH,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun FingerprintGraphicOffPreview() {
    BicoccaTheme(dark = true) {
        FingerprintGraphic(
            enabled = false,
            deviceType = DeviceType.Phone,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
