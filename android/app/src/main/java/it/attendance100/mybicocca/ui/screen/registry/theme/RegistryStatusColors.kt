package it.attendance100.mybicocca.ui.screen.registry.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryBadgeTone

// Success / warning tones don't exist as Material color roles, and the brand error
// container isn't wired into BicoccaTheme, so the registry directory carries its own
// fixed status palette mirroring the design tokens. Light vs dark is read off the
// active surface luminance so it tracks BicoccaTheme regardless of how dark was set.
data class RegistryStatusTone(
    val container: Color,
    val onContainer: Color,
)

private val SuccessContainerLight = Color(0xFFBFF0C4)
private val OnSuccessContainerLight = Color(0xFF002109)
private val SuccessContainerDark = Color(0xFF08491D)
private val OnSuccessContainerDark = Color(0xFFBFF0C4)

private val WarningContainerLight = Color(0xFFFFDDB0)
private val OnWarningContainerLight = Color(0xFF2A1800)
private val WarningContainerDark = Color(0xFF5A4019)
private val OnWarningContainerDark = Color(0xFFFFDDB0)

private val ErrorContainerLight = Color(0xFFFFDAD6)
private val OnErrorContainerLight = Color(0xFF410002)
private val ErrorContainerDark = Color(0xFF93000A)
private val OnErrorContainerDark = Color(0xFFFFDAD6)

@Composable
fun registryBadgeTone(tone: RegistryBadgeTone): RegistryStatusTone {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.surface.luminance() < 0.5f
    return when (tone) {
        RegistryBadgeTone.Ok -> RegistryStatusTone(
            if (dark) SuccessContainerDark else SuccessContainerLight,
            if (dark) OnSuccessContainerDark else OnSuccessContainerLight,
        )
        RegistryBadgeTone.Attention -> RegistryStatusTone(
            if (dark) WarningContainerDark else WarningContainerLight,
            if (dark) OnWarningContainerDark else OnWarningContainerLight,
        )
        RegistryBadgeTone.Alert -> RegistryStatusTone(
            if (dark) ErrorContainerDark else ErrorContainerLight,
            if (dark) OnErrorContainerDark else OnErrorContainerLight,
        )
        RegistryBadgeTone.Neutral -> RegistryStatusTone(
            scheme.surfaceContainerHighest,
            scheme.onSurfaceVariant,
        )
    }
}
