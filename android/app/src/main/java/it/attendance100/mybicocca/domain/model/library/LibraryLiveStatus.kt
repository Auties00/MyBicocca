package it.attendance100.mybicocca.domain.model.library

import java.time.LocalDate
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
 * @property todayForecast The day's occupancy forecast ranges; empty in the light children
 * listing, populated by the full per-library status.
 * @property forecastDate Calendar date carried inside the forecast ranges (site zone, Europe/Rome);
 * null when the payload carries no date.
 */
data class LibraryLiveStatus(
    val isOpen: Boolean,
    val statusText: String?,
    val occupancyPercentage: Int?,
    val crowdLevel: LibraryCrowd?,
    val todayForecast: List<LibraryForecastPoint>,
    val forecastDate: LocalDate? = null,
)

/** Estimated crowd level reported by Affluences when no exact occupancy percentage is available. */
enum class LibraryCrowd { Fluid, Moderate, Dense }

/**
 * One range of the day's occupancy forecast curve.
 *
 * @property time Start of the forecast range.
 * @property endTime End of the forecast range; defaults to 30 minutes after [time] when omitted.
 * @property occupancyPercentage Forecast occupancy over that range.
 * @property rising Trend versus the previous point: true rising, false falling, null flat or
 * unknown.
 */
data class LibraryForecastPoint(
    val time: LocalTime,
    val occupancyPercentage: Int,
    val rising: Boolean?,
    val endTime: LocalTime = time.plusMinutes(30),
)

fun LibraryForecastPoint.toCrowdingSlot(): LibraryCrowdingSlot =
    LibraryCrowdingSlot(start = time, end = endTime, occupancyPercentage = occupancyPercentage)
