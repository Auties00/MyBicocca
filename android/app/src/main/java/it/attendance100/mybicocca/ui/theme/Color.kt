package it.attendance100.mybicocca.ui.theme

import androidx.compose.ui.graphics.Color

// Light scheme
internal val PrimaryLight = Color(0xFFD81648)

// Brand-fixed accent for the wordmark. The literal matches PrimaryLight by
// design — both render the same brand red — but they live as separate
// references because they have different theming contracts: PrimaryLight pairs
// with PrimaryDark (pink) for the active Material scheme, while the wordmark
// stays this red across both themes so the brand asset never shifts.
val BicoccaWordmarkAccent = PrimaryLight
internal val OnPrimaryLight = Color(0xFFFFFFFF)
internal val PrimaryContainerLight = Color(0xFFFFD9DF)
internal val OnPrimaryContainerLight = Color(0xFF400014)
internal val SecondaryLight = Color(0xFF7A4F56)
internal val OnSecondaryLight = Color(0xFFFFFFFF)
internal val TertiaryLight = Color(0xFF7B5A2E)
internal val OnTertiaryLight = Color(0xFFFFFFFF)
internal val SurfaceLight = Color(0xFFFFFFFF)
internal val OnSurfaceLight = Color(0xFF1A0A10)
internal val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
internal val SurfaceContainerLowLight = Color(0xFFFFF8F9)
internal val SurfaceContainerLight = Color(0xFFFFF0F2)
internal val SurfaceContainerHighLight = Color(0xFFFFE1E6)
internal val SurfaceContainerHighestLight = Color(0xFFF8D5DA)
internal val OnSurfaceVariantLight = Color(0xFF6B4B53)
internal val OutlineLight = Color(0xFF9B7C82)
internal val OutlineVariantLight = Color(0xFFE8C8CE)

// Dark scheme
internal val PrimaryDark = Color(0xFFFFB1BE)
internal val OnPrimaryDark = Color(0xFF5B0017)
internal val PrimaryContainerDark = Color(0xFF7A1530)
internal val OnPrimaryContainerDark = Color(0xFFFFD9DF)
internal val SecondaryDark = Color(0xFFE5BCC2)
internal val OnSecondaryDark = Color(0xFF432229)
internal val TertiaryDark = Color(0xFFE9C28C)
internal val OnTertiaryDark = Color(0xFF412C04)
internal val SurfaceDark = Color(0xFF0B0808)
internal val OnSurfaceDark = Color(0xFFF3E7E9)
internal val SurfaceContainerLowestDark = Color(0xFF0F0606)
internal val SurfaceContainerLowDark = Color(0xFF140A0C)
internal val SurfaceContainerDark = Color(0xFF1A0D10)
internal val SurfaceContainerHighDark = Color(0xFF261317)
internal val SurfaceContainerHighestDark = Color(0xFF322028)
internal val OnSurfaceVariantDark = Color(0xFFB89398)
internal val OutlineDark = Color(0xFF7C5A60)
internal val OutlineVariantDark = Color(0xFF3A1B22)

// Profile ID-card badge palette. The badge renders its own art over a chromatic
// red face, so it carries fixed colors independent of the active Material scheme.
// The card face is a saturated red in both modes (the post-refactor Material primary
// lightened to pink in dark mode, which washed the badge out), so it's pinned to the
// pre-refactor primary reds here rather than reading colorScheme.primary.
val BadgeCardColorLight = Color(0xFFF01D59)
val BadgeCardColorDark = Color(0xFF9C0C35)
val OnBackgroundColor = Color(0xFFFFF8F7)
val BadgeWhiteDrawableColor = Color(0xFF551420)
val BadgeSignatureBoxColorRed = Color(0xFFA6A6A6).copy(alpha = 0.75f)
val BadgeSignatureBoxColorRed2 = Color(0xFFB8B8B8).copy(alpha = 0.75f)
val BadgeSignatureBoxColorWhite = Color(0xFFE3E3E3).copy(alpha = 0.95f)
val BadgeSignatureBoxColorWhite2 = Color(0xFFEEEEEE).copy(alpha = 0.95f)
