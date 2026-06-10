package it.attendance100.mybicocca.ui.screen.calendar.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * One event swatch: [accent] paints the leading stripe and indicator marks, [container]
 * fills the card, [onContainer] is the readable content color on that fill.
 */
@Immutable
data class EventColorVariant(
    val accent: Color,
    val container: Color,
    val onContainer: Color,
)

/**
 * Swatch set for calendar events. [variants] is the hash pool lessons rotate through for
 * variety; the named variants are fixed identities so exams, deadlines, appointments and
 * library seats stay recognizable at a glance — red is deliberately absent from the pool
 * to keep exams unmistakable. [inProgress] overrides every kind while an event is
 * actually running.
 */
@Immutable
data class EventPalette(
    val variants: List<EventColorVariant>,
    val inProgress: EventColorVariant,
    val exam: EventColorVariant,
    val deadline: EventColorVariant,
    val appointment: EventColorVariant,
    val library: EventColorVariant,
)

private fun variantOf(accent: Long, container: Long, onContainer: Long) =
    EventColorVariant(Color(accent), Color(container), Color(onContainer))

val LightEventPalette = EventPalette(
    variants = listOf(
        variantOf(accent = 0xFFEA580C, container = 0xFFFED7AA, onContainer = 0xFF9A3412),
        variantOf(accent = 0xFFD97706, container = 0xFFFDE68A, onContainer = 0xFF92400E),
        variantOf(accent = 0xFFCA8A04, container = 0xFFFEF08A, onContainer = 0xFF854D0E),
        variantOf(accent = 0xFF65A30D, container = 0xFFD9F99D, onContainer = 0xFF3F6212),
        variantOf(accent = 0xFF16A34A, container = 0xFFBBF7D0, onContainer = 0xFF166534),
        variantOf(accent = 0xFF0D9488, container = 0xFF99F6E4, onContainer = 0xFF115E59),
        variantOf(accent = 0xFF0891B2, container = 0xFFA5F3FC, onContainer = 0xFF155E75),
        variantOf(accent = 0xFF0284C7, container = 0xFFBAE6FD, onContainer = 0xFF075985),
        variantOf(accent = 0xFF2563EB, container = 0xFFBFDBFE, onContainer = 0xFF1E40AF),
        variantOf(accent = 0xFF4F46E5, container = 0xFFC7D2FE, onContainer = 0xFF3730A3),
        variantOf(accent = 0xFF7C3AED, container = 0xFFDDD6FE, onContainer = 0xFF5B21B6),
        variantOf(accent = 0xFF9333EA, container = 0xFFE9D5FF, onContainer = 0xFF6B21A8),
        variantOf(accent = 0xFFC026D3, container = 0xFFF5D0FE, onContainer = 0xFF86198F),
        variantOf(accent = 0xFFDB2777, container = 0xFFFBCFE8, onContainer = 0xFF9D174D),
        variantOf(accent = 0xFF57534E, container = 0xFFE7E5E4, onContainer = 0xFF44403C),
        variantOf(accent = 0xFF52525B, container = 0xFFE4E4E7, onContainer = 0xFF3F3F46),
    ),
    inProgress = variantOf(accent = 0xFF10B981, container = 0xFFD1FAE5, onContainer = 0xFF065F46),
    exam = variantOf(accent = 0xFFDC2626, container = 0xFFFECACA, onContainer = 0xFF991B1B),
    deadline = variantOf(accent = 0xFFB45309, container = 0xFFFEF3C7, onContainer = 0xFF78350F),
    appointment = variantOf(accent = 0xFF4338CA, container = 0xFFE0E7FF, onContainer = 0xFF312E81),
    library = variantOf(accent = 0xFF0F766E, container = 0xFFCCFBF1, onContainer = 0xFF134E4A),
)

val DarkEventPalette = EventPalette(
    variants = listOf(
        variantOf(accent = 0xFFFB923C, container = 0xFF3B1A0E, onContainer = 0xFFFED7AA),
        variantOf(accent = 0xFFFBBF24, container = 0xFF3F1D08, onContainer = 0xFFFDE68A),
        variantOf(accent = 0xFFFACC15, container = 0xFF3F1F0E, onContainer = 0xFFFEF08A),
        variantOf(accent = 0xFFA3E635, container = 0xFF1A2E05, onContainer = 0xFFD9F99D),
        variantOf(accent = 0xFF4ADE80, container = 0xFF0A2E1E, onContainer = 0xFFBBF7D0),
        variantOf(accent = 0xFF2DD4BF, container = 0xFF082F2E, onContainer = 0xFF99F6E4),
        variantOf(accent = 0xFF22D3EE, container = 0xFF083344, onContainer = 0xFFA5F3FC),
        variantOf(accent = 0xFF38BDF8, container = 0xFF082F49, onContainer = 0xFFBAE6FD),
        variantOf(accent = 0xFF60A5FA, container = 0xFF172554, onContainer = 0xFFBFDBFE),
        variantOf(accent = 0xFF818CF8, container = 0xFF1E1B4B, onContainer = 0xFFC7D2FE),
        variantOf(accent = 0xFFA78BFA, container = 0xFF2E1065, onContainer = 0xFFDDD6FE),
        variantOf(accent = 0xFFC084FC, container = 0xFF3B0764, onContainer = 0xFFE9D5FF),
        variantOf(accent = 0xFFE879F9, container = 0xFF4A044E, onContainer = 0xFFF5D0FE),
        variantOf(accent = 0xFFF472B6, container = 0xFF500724, onContainer = 0xFFFBCFE8),
        variantOf(accent = 0xFFA8A29E, container = 0xFF1C1917, onContainer = 0xFFE7E5E4),
        variantOf(accent = 0xFFA1A1AA, container = 0xFF18181B, onContainer = 0xFFE4E4E7),
    ),
    inProgress = variantOf(accent = 0xFF34D399, container = 0xFF064E3B, onContainer = 0xFFA7F3D0),
    exam = variantOf(accent = 0xFFF87171, container = 0xFF450A0A, onContainer = 0xFFFECACA),
    deadline = variantOf(accent = 0xFFFBBF24, container = 0xFF451A03, onContainer = 0xFFFDE68A),
    appointment = variantOf(accent = 0xFF818CF8, container = 0xFF1E1B4B, onContainer = 0xFFC7D2FE),
    library = variantOf(accent = 0xFF2DD4BF, container = 0xFF042F2E, onContainer = 0xFF99F6E4),
)

val LocalEventPalette = staticCompositionLocalOf { LightEventPalette }

/**
 * Installs the light or dark [EventPalette] into [LocalEventPalette] for the calendar
 * screen and its sub-screens. The event swatch set has no consumers outside the calendar,
 * so the provider stays screen-scoped and the app-wide theme stays narrow.
 */
@Composable
fun ProvideEventPalette(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val palette = if (dark) DarkEventPalette else LightEventPalette
    CompositionLocalProvider(LocalEventPalette provides palette, content = content)
}
