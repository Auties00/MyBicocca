package it.attendance100.mybicocca.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark theme
val PrimaryColor = Color(0xFF9C0C35)
val OnPrimaryColor = Color(0xFFFFB2BC)
val SecondaryColor = Color(0xFFFFB2BC)
val OnSecondaryColor = Color(0xFF571C28)
val TertiaryColor = Color(0xFFFFCE9B)
val OnTertiaryColor = Color(0xFF482900)
val ErrorColor = Color(0xFFFFB4AB)
val OnErrorColor = Color(0xFF690005)
val PrimaryContainerColor = Color(0xFFA32043)
val OnPrimaryContainerColor = Color(0xFFFFB9C1)
val SecondaryContainerColor = Color(0xFFF8A0AC)
val OnSecondaryContainerColor = Color(0xFF763440)
val TertiaryContainerColor = Color(0xFFFEA93E)
val OnTertiaryContainerColor = Color(0xFF160F0B)
val ErrorContainerColor = Color(0xFF93000A)
val OnErrorContainerColor = Color(0xFFFFDAD6)
val SurfaceDimColor = Color(0xFF1C1012)
val SurfaceBrightColor = Color(0xFF443537)
val SurfaceContainerLowestColor = Color(0xFF160B0D)
val SurfaceContainerLowColor = Color(0xFF1A0D0F)
val SurfaceContainerColor = Color(0xFF1C1012)
val SurfaceContainerHighColor = Color(0xFF291C1E)
val SurfaceContainerHighestColor = Color(0xFF403133)
val OnSurfaceVariantColor = Color(0xFFF5DDDE)
val SurfaceVariantColor = Color(0xFFDFBFC2)
val OutlineColor = Color(0xFFA68A8C)
val OutlineVariantColor = Color(0xFF584143)
val InverseSurfaceColor = Color(0xFFF5DDDE)
val InverseOnSurfaceColor = Color(0xFF3B2D2E)
val InversePrimaryColor = Color(0xFFF01D59)
val BackgroundColor = Color(0xFF0D0D0D)
val OnBackgroundColor = Color(0xFFFFF8F7)

// Light theme
val PrimaryColorLight = Color(0xFFF01D59)
val OnPrimaryColorLight = Color(0xFFFFFFFF)
val SecondaryColorLight = Color(0xFF904954)
val OnSecondaryColorLight = Color(0xFFA32043)
val TertiaryColorLight = Color(0xFF885200)
val OnTertiaryColorLight = Color(0xFFFFFFFF)
val ErrorColorLight = Color(0xFFBA1A1A)
val OnErrorColorLight = Color(0xFFFFFFFF)
val PrimaryContainerColorLight = Color(0xFFA32043)
val OnPrimaryContainerColorLight = Color(0xFFFFB9C1)
val SecondaryContainerColorLight = Color(0xFFFFA5B2)
val OnSecondaryContainerColorLight = Color(0xFF7A3843)
val TertiaryContainerColorLight = Color(0xFFFEA93E)
val OnTertiaryContainerColorLight = Color(0xFF6D4100)
val ErrorContainerColorLight = Color(0xFFFFDAD6)
val OnErrorContainerColorLight = Color(0xFF93000A)
val SurfaceDimColorLight = Color(0xFFEEE3E4)
val SurfaceColorLight = Color(0xFFFFF8F7)
val SurfaceBrightColorLight = Color(0xFFFFF8F7)
val SurfaceContainerLowestColorLight = Color(0xFFFFFFFF)
val SurfaceContainerLowColorLight = Color(0xFFFFFFFF)
val SurfaceContainerColorLight = Color(0xFFFFE9EA)
val SurfaceContainerHighColorLight = Color(0xFFFBE3E4)
val SurfaceContainerHighestColorLight = Color(0xFFF5DDDE)
val OnSurfaceVariantColorLight = Color(0xFF25181A)
val SurfaceVariantColorLight = Color(0xFF584143)
val OutlineColorLight = Color(0xFF8B7073)
val OutlineVariantColorLight = Color(0xFFDFBFC2)
val InverseSurfaceColorLight = Color(0xFF3B2D2E)
val InverseOnSurfaceColorLight = Color(0xFFFFECED)
val InversePrimaryColorLight = Color(0xFFE6124F)
val BackgroundColorLight = Color(0xFFFFFFFF)
val OnBackgroundColorLight = Color(0xFF0D0D0D)

// Gray
val GrayColorDark = Color(0xFF9D9D9D)
val GrayColorLight = Color(0xFF666666)

// Badge
val BadgeWhiteDrawableColor = Color(0xFF551420)
val BadgeSignatureBoxColorRed = Color(0xFFa6a6a6).copy(alpha = 0.75f)
val BadgeSignatureBoxColorWhite = Color(0xFFE3E3E3).copy(alpha = 0.95f)
val BadgeSignatureBoxColorRed2 = Color(0xFFB8B8B8).copy(alpha = 0.75f)
val BadgeSignatureBoxColorWhite2 = Color(0xFFEEEEEE).copy(alpha = 0.95f)

// Calendar Event Colors
val EventLectureColor = Color(0xFF9333EA)
val EventLabColor = Color(0xFF0EA5E9)
val EventExamColor = Color(0xFFDC2626)
val EventInProgressColor = Color(0xFF10B981)

// PagoPA (defined in Theme.kt)

@Composable
fun GrayColor(): Color {
    val isDark = MaterialTheme.colorScheme.background == BackgroundColor
    return if (isDark) GrayColorDark else GrayColorLight
}
