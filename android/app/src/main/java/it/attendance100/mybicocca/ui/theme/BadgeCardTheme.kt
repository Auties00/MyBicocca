package it.attendance100.mybicocca.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Selectable visual styles for the flippable student ID badge ([StudentCard]).
// Default reproduces the physical red Bicocca card; White is the pale variant. New palettes
// can be added here and given a branch in [BadgeCardTheme.colors] without touching the faces.
enum class BadgeCardTheme(val displayName: String) {
    Default("Standard"),
    White("White"),
}

// The full palette a badge face needs, already resolved for the active theme and light/dark
// mode, so the faces (CardFace / BadgeFront / BadgeBack) never branch on the theme themselves.
@Immutable
data class BadgeCardColors(
    val container: Color,       // card surface behind the chromatic overlay
    val content: Color,         // holder name / matricola / labels / logo art
    val chipTint: Color,        // multiply tint over the EMV chip art
    val signatureBox: Color,    // signature panel background
    val signatureStripe: Color, // ruled lines inside the signature panel
    val signatureText: Color,   // cursive signature
    val magneticStripe: Color,  // back magnetic stripe
)

/** Resolves the [BadgeCardColors] for this theme in the requested light/dark mode. */
@Composable
fun BadgeCardTheme.colors(dark: Boolean = isSystemInDarkTheme()): BadgeCardColors = when (this) {
    // Pre-refactor badge red (deep red in dark, bright red in light) instead of the lightened
    // Material primary, so the chromatic face reads as the original ID card in both modes.
    BadgeCardTheme.Default -> BadgeCardColors(
        container = if (dark) BadgeCardColorDark else BadgeCardColorLight,
        content = OnBackgroundColor,
        chipTint = Color(0xFFFFAD42),
        signatureBox = BadgeSignatureBoxColorRed,
        signatureStripe = BadgeSignatureBoxColorRed2,
        signatureText = Color.Black,
        magneticStripe = Color(0xFF000000).copy(alpha = 0.95f),
    )

    BadgeCardTheme.White -> BadgeCardColors(
        container = Color.White,
        content = BadgeWhiteDrawableColor,
        chipTint = Color.Red.copy(alpha = 0.1f),
        signatureBox = BadgeSignatureBoxColorWhite,
        signatureStripe = BadgeSignatureBoxColorWhite2,
        signatureText = BadgeWhiteDrawableColor,
        magneticStripe = BadgeSignatureBoxColorWhite,
    )
}
