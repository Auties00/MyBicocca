package it.attendance100.mybicocca.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark theme colors
val OnPrimaryColor = Color(0xFFffb2bc)
val PrimaryColor = Color(0xFF9C0C35)
val SecondaryColor = Color(0xFFffb2bc)
val onSecondaryColor = Color(0xFF571c28)
val TertiaryColor = Color(0xFFffce9b)
val onTertiaryColor = Color(0xFF482900)
val ErrorColor = Color(0xFFffb4ab)
val onErrorColor = Color(0xFF690005)
val primaryContainerColor = Color(0xFFa32043)
val onPrimaryContainerColor = Color(0xFFffb9c1)
val secondaryContainerColor = Color(0xFF763440)
val onSecondaryContainerColor = Color(0xFFf8a0ac)
val tertiaryContainerColor = Color(0xFFfea93e)
val onTertiaryContainerColor = Color(0xFF6d4100)
val errorContainerColor = Color(0xFF93000a)
val onErrorContainerColor = Color(0xFFffdad6)
val surfaceDimColor = Color(0xFF1c1012)

// val surfaceColor = Color(0xFF1c1012) // Predictive back animation background
val surfaceBrightColor = Color(0xFF443537)
val surfaceContainerLowestColor = Color(0xFF160b0d)
val surfaceContainerLowColor = Color(0xFF1a0d0f) // Drawer and BottomNavBar and Dialogs
val surfaceContainerColor = Color(0xFF1c1012)
val surfaceContainerHighColor = Color(0xFF291c1e)
val surfaceContainerHighestColor = Color(0xFF403133)
val onSurfaceColor = Color(0xFFf5ddde)
val onSurfaceVarColor = Color(0xFFdfbfc2)
val outlineColor = Color(0xFFa68a8c)
val outlineVariantColor = Color(0xFF584143)
val inverseSurfaceColor = Color(0xFFf5ddde)
val inverseOnSurfaceColor = Color(0xFF3b2d2e)
val inversePrimaryColor = Color(0xFFf01d59)
val BackgroundColor = Color(0xFF0d0d0d)
val surfaceColor = BackgroundColor // Predictive back animation background
val OnBackgroundColor = Color(0xFFFFF8F7)
val GrayColorDark = Color(0xFF9d9d9d)
val BadgeWhiteDrawableColor = Color(0xFF551420) // Color(0xFF281E1E)
val BadgeSignatureBoxColorRed = Color(0xFFa6a6a6).copy(alpha = 0.75f)
val BadgeSignatureBoxColorWhite = Color(0xFFE3E3E3).copy(alpha = 0.95f)
val BadgeSignatureBoxColorRed2 = Color(0xFFB8B8B8).copy(alpha = 0.75f)
val BadgeSignatureBoxColorWhite2 = Color(0xFFEEEEEE).copy(alpha = 0.95f)


// Light theme colors
val OnPrimaryColorLight = Color(0xFFFFFFFF)
val PrimaryColorLight = Color(0xFFf01d59)
val SecondaryColorLight = Color(0xFF904954)
val onSecondaryColorLight = Color(0xFFa32043)
val TertiaryColorLight = Color(0xFF885200)
val onTertiaryColorLight = Color(0xFFFFFFFF)
val ErrorColorLight = Color(0xFFBA1A1A)
val onErrorColorLight = Color(0xFFFFFFFF)
val primaryContainerColorLight = Color(0xFFA32043)
val onPrimaryContainerColorLight = Color(0xFFFFB9C1)
val secondaryContainerColorLight = Color(0xFFFFA5B2)
val onSecondaryContainerColorLight = Color(0xFF7A3843)
val tertiaryContainerColorLight = Color(0xFFFEA93E)
val onTertiaryContainerColorLight = Color(0xFF6D4100)
val errorContainerColorLight = Color(0xFFFFDAD6)
val onErrorContainerColorLight = Color(0xFF93000A)
val surfaceDimColorLight = Color(0xffeee3e4)
val surfaceColorLight = Color(0xFFFFF8F7)
val surfaceBrightColorLight = Color(0xFFFFF8F7)
val surfaceContainerLowestColorLight = Color(0xFFFFFFFF)
val surfaceContainerLowColorLight = Color(0xFFFFFFFF)
val surfaceContainerColorLight = Color(0xFFFFE9EA)
val surfaceContainerHighColorLight = Color(0xFFFBE3E4)
val surfaceContainerHighestColorLight = Color(0xFFF5DDDE)
val onSurfaceColorLight = Color(0xFF25181A)
val onSurfaceVarColorLight = Color(0xFF584143)
val outlineColorLight = Color(0xFF8B7073)
val outlineVariantColorLight = Color(0xFFDFBFC2)
val inverseSurfaceColorLight = Color(0xFF3B2D2E)
val inverseOnSurfaceColorLight = Color(0xFFFFECED)
val inversePrimaryColorLight = Color(0xffe6124f)
val BackgroundColorLight = Color(0xFFFFFFFF)
val CardColorLight = Color(0xFFFFE9EA)
val GrayColorLight = Color(0xFF666666)
val OnBackgroundColorLight = Color(0xFF0d0d0d)
val TextColorLight = Color(0xFF1C1C1C)

// Calendar event colors - Improved saturation and contrast
val EventLectureColor = Color(0xFF9333EA)       // Vibrant Purple - Lectures
val EventLabColor = Color(0xFF0EA5E9)           // Bright Cyan - Labs
val EventExamColor = Color(0xFFDC2626)          // Strong Red - Exams
val EventOfficeHoursColor = Color(0xFFF97316)   // Vivid Orange - Office Hours
val EventInProgressColor = Color(0xFF10B981)    // Emerald Green - Event in progress
val EventOtherColor = Color(0xFF8B5CF6)         // Medium Purple - Other events

// Event colors with opacity for backgrounds
val EventLectureColorAlpha = Color(0x1A9333EA)  // 10% opacity
val EventLabColorAlpha = Color(0x1A0EA5E9)
val EventExamColorAlpha = Color(0x1ADC2626)
val EventOfficeHoursColorAlpha = Color(0x1AF97316)
val EventOtherColorAlpha = Color(0x1A8B5CF6)

@Composable
fun GrayColor(): Color {
    return if (MaterialTheme.colorScheme.background == BackgroundColor) GrayColorDark else GrayColorLight
}