package it.attendance100.mybicocca.ui.screen.registry.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Per-section icon-chip accent for the Registry "servizi" directory: a tinted chip container
 * plus the saturated glyph color. One distinct hue per section so the groups read apart at a
 * glance. Brand red is deliberately omitted (it's the app accent) — the swatches reuse the
 * calendar palette's hue language, theme-aware for light/dark.
 */
@Immutable
data class ServiceAccent(val container: Color, val onContainer: Color)

/** Blue, teal, violet, amber, green — the dark list mirrors the same hue order. */
private val LightServiceAccents = listOf(
    ServiceAccent(Color(0xFFBFDBFE), Color(0xFF1E40AF)),
    ServiceAccent(Color(0xFF99F6E4), Color(0xFF115E59)),
    ServiceAccent(Color(0xFFDDD6FE), Color(0xFF5B21B6)),
    ServiceAccent(Color(0xFFFEF08A), Color(0xFF854D0E)),
    ServiceAccent(Color(0xFFBBF7D0), Color(0xFF166534)),
)

private val DarkServiceAccents = listOf(
    ServiceAccent(Color(0xFF172554), Color(0xFFBFDBFE)),
    ServiceAccent(Color(0xFF082F2E), Color(0xFF99F6E4)),
    ServiceAccent(Color(0xFF2E1065), Color(0xFFDDD6FE)),
    ServiceAccent(Color(0xFF3F1F0E), Color(0xFFFEF08A)),
    ServiceAccent(Color(0xFF0A2E1E), Color(0xFFBBF7D0)),
)

/** Index-aligned with the directory's sections; callers cycle with `% size` to stay in range. */
@Composable
fun serviceAccents(): List<ServiceAccent> =
    if (isSystemInDarkTheme()) DarkServiceAccents else LightServiceAccents
