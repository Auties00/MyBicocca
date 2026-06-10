package it.attendance100.mybicocca.ui.screen.map.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Colors for the campus base map, applied to the Protomaps basemap layers at runtime (see
 * [applyBicoccaPalette]) so the map tracks the active app theme — brand light/dark plus
 * Material You and the other palettes. POI icons keep Protomaps' own category colors and are
 * NOT part of this palette.
 */
data class MapPalette(
    val ground: Color,
    val urban: Color,
    val park: Color,
    val water: Color,
    val building: Color,
    val buildingOutline: Color,
    val road: Color,
    val roadCasing: Color,
    val highway: Color,
    val highwayCasing: Color,
    val label: Color,
    val roadLabel: Color,
    val labelHalo: Color,
) {
    companion object {
        /**
         * Exact hand-tuned brand hexes (with [BicoccaDark] as the dark pair), sourced from
         * the Bicocca Google-cloud map style rather than derived from an M3 scheme.
         */
        val BicoccaLight = MapPalette(
            ground = Color(0xFFFFF8F9), urban = Color(0xFFFFF8F9), park = Color(0xFFE9DDD2),
            water = Color(0xFFF3CDD5), building = Color(0xFFF9EBEE), buildingOutline = Color(0xFFF0DDE1),
            road = Color(0xFFFFFFFF), roadCasing = Color(0xFFF3E3E6),
            highway = Color(0xFFFFD9DF), highwayCasing = Color(0xFFF8D5DA),
            label = Color(0xFF6B4B53), roadLabel = Color(0xFF9B7C82), labelHalo = Color(0xFFFFFFFF),
        )
        val BicoccaDark = MapPalette(
            ground = Color(0xFF140A0C), urban = Color(0xFF180C0F), park = Color(0xFF1A130E),
            water = Color(0xFF21161A), building = Color(0xFF1D1013), buildingOutline = Color(0xFF2A181D),
            road = Color(0xFF1A0D10), roadCasing = Color(0xFF261317),
            highway = Color(0xFF7A1530), highwayCasing = Color(0xFF3A1B22),
            label = Color(0xFFB89398), roadLabel = Color(0xFF7C5A60), labelHalo = Color(0xFF0B0808),
        )

        /**
         * Maps any M3 scheme onto the base map — used for Material You and the non-brand
         * palettes so the map follows the wallpaper/accent. Mirrors the brand mapping:
         * surface = ground, containers = buildings/water, primary tones = the road/highway
         * accent.
         */
        fun fromColorScheme(s: ColorScheme, dark: Boolean): MapPalette = MapPalette(
            ground = s.surface,
            urban = s.surface,
            park = s.secondaryContainer,
            water = s.primaryContainer,
            building = if (dark) s.surfaceContainerHigh else s.surfaceContainerHighest,
            buildingOutline = s.outlineVariant,
            road = if (dark) s.surfaceContainerHigh else s.surfaceContainerLowest,
            roadCasing = s.surfaceVariant,
            highway = s.primaryContainer,
            highwayCasing = s.primary,
            label = s.onSurfaceVariant,
            roadLabel = s.onSurfaceVariant,
            labelHalo = s.surface,
        )
    }
}
