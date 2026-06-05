package it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// Success / warning / info tones aren't Material color roles and the registry palette is
// owned by a different screen, so the enrollments sub-screen carries its own status tones
// (same design tokens). Light vs dark is read off the active surface luminance so it tracks
// BicoccaTheme regardless of how dark was selected.
data class EnrollmentStatusTone(
    val container: Color,
    val onContainer: Color,
)

enum class EnrollmentBadgeTone { Active, Attention, Alert, Info, Neutral }

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
fun enrollmentBadgeTone(tone: EnrollmentBadgeTone): EnrollmentStatusTone {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.surface.luminance() < 0.5f
    return when (tone) {
        EnrollmentBadgeTone.Active -> EnrollmentStatusTone(
            if (dark) SuccessContainerDark else SuccessContainerLight,
            if (dark) OnSuccessContainerDark else OnSuccessContainerLight,
        )
        EnrollmentBadgeTone.Attention -> EnrollmentStatusTone(
            if (dark) WarningContainerDark else WarningContainerLight,
            if (dark) OnWarningContainerDark else OnWarningContainerLight,
        )
        EnrollmentBadgeTone.Alert -> EnrollmentStatusTone(
            if (dark) ErrorContainerDark else ErrorContainerLight,
            if (dark) OnErrorContainerDark else OnErrorContainerLight,
        )
        EnrollmentBadgeTone.Info -> EnrollmentStatusTone(
            if (dark) InfoContainerDark else InfoContainerLight,
            if (dark) OnInfoContainerDark else OnInfoContainerLight,
        )
        EnrollmentBadgeTone.Neutral -> EnrollmentStatusTone(
            scheme.surfaceContainerHighest,
            scheme.onSurfaceVariant,
        )
    }
}
