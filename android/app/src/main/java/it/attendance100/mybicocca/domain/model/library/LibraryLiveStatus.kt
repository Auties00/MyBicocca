package it.attendance100.mybicocca.domain.model.library

import java.time.LocalTime

/**
 * Real-time status of a library, from Affluences live data. Occupancy is reported either as an
 * exact percentage (the Bicocca libraries have sensors) or, as a fallback, an estimated crowd
 * level. Shown on the Biblioteca landing cards and library detail page.
 *
 * @property isOpen Whether the library is currently open.
 * @property statusText Localized message such as "Chiude alle 22:00" / "Apre alle 08:30"; null
 * when unknown.
 * @property occupancyPercentage Sensor-measured occupancy percentage; null when unavailable.
 * @property crowdLevel Estimated crowd level used as the fallback when no exact percentage is
 * reported; null when unknown.
 * @property todayForecast The day's hourly occupancy forecast curve; empty in the light children
 * listing, populated by the full per-library status.
 */
data class LibraryLiveStatus(
    val isOpen: Boolean,
    val statusText: String?,
    val occupancyPercentage: Int?,
    val crowdLevel: LibraryCrowd?,
    val todayForecast: List<LibraryForecastPoint>,
)

/** Estimated crowd level reported by Affluences when no exact occupancy percentage is available. */
enum class LibraryCrowd { Fluid, Moderate, Dense }

/**
 * One point of the day's occupancy forecast curve.
 *
 * @property time Start of the forecast slot.
 * @property occupancyPercentage Forecast occupancy at that time.
 * @property rising Trend versus the previous point: true rising, false falling, null flat or
 * unknown.
 */
data class LibraryForecastPoint(
    val time: LocalTime,
    val occupancyPercentage: Int,
    val rising: Boolean?,
)
