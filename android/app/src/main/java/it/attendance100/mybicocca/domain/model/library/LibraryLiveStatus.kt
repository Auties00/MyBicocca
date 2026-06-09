package it.attendance100.mybicocca.domain.model.library

import java.time.LocalTime

// Real-time status of a library. Occupancy is reported either as an exact percentage (Bicocca
// libraries have sensors) or, as a fallback, an estimated crowd level.
data class LibraryLiveStatus(
    val isOpen: Boolean,
    // Localized "Chiude alle 22:00" / "Apre alle 08:30", when known.
    val statusText: String?,
    val occupancyPercentage: Int?,
    val crowdLevel: LibraryCrowd?,
    val todayForecast: List<LibraryForecastPoint>,
)

enum class LibraryCrowd { Fluid, Moderate, Dense }

// One point of the day's occupancy forecast curve.
data class LibraryForecastPoint(
    val time: LocalTime,
    val occupancyPercentage: Int,
    // Trend versus the previous point: true rising, false falling, null flat/unknown.
    val rising: Boolean?,
)
