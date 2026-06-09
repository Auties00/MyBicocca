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

private val InfoContainerLight = Color(0xFFD5E3FF)
private val OnInfoContainerLight = Color(0xFF001B3D)
private val InfoContainerDark = Color(0xFF1B3A66)
private val OnInfoContainerDark = Color(0xFFD5E3FF)

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
        RegistryBadgeTone.Info -> RegistryStatusTone(
            if (dark) InfoContainerDark else InfoContainerLight,
            if (dark) OnInfoContainerDark else OnInfoContainerLight,
        )
        RegistryBadgeTone.Neutral -> RegistryStatusTone(
            scheme.surfaceContainerHighest,
            scheme.onSurfaceVariant,
        )
    }
}

// Saturated success / warning accents (status dots, rings, "passed/paid" text) — the single
// source for the "passed/paid = green, pending = amber" semantics several sheets render. Not
// Material roles; fixed light/dark pairs picked off the active surface luminance, like the
// container tones above.
enum class RegistryAccent { Success, Warning }

private val SuccessAccentLight = Color(0xFF1E8E3E)
private val SuccessAccentDark = Color(0xFF6DD58C)
private val WarningAccentLight = Color(0xFFE8710A)
private val WarningAccentDark = Color(0xFFFFB868)

@Composable
fun registryAccent(accent: RegistryAccent): Color {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return when (accent) {
        RegistryAccent.Success -> if (dark) SuccessAccentDark else SuccessAccentLight
        RegistryAccent.Warning -> if (dark) WarningAccentDark else WarningAccentLight
    }
}
