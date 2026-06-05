package it.attendance100.mybicocca.data.remote.affluences.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Real-time data and occupancy forecasts for a site, as returned by the app API v4
 * (`GET /sites/{site}/live-data`).
 *
 * @property status The current open/closed status of the site.
 * @property notices Critical notices the site wants to display prominently.
 * @property liveAttendance The real-time attendance of the site, or null when the site is closed
 * or does not publish live data.
 * @property todayForecasts The occupancy forecasts for the remainder of the current day.
 * @property forecasts The per-day occupancy forecasts, starting from today.
 */
@Serializable
data class AffluencesLiveData(
    @SerialName("status")
    val status: AffluencesSiteStatus,
    @SerialName("notices")
    val notices: List<AffluencesNotice> = emptyList(),
    @SerialName("liveAttendance")
    val liveAttendance: AffluencesLiveAttendance? = null,
    @SerialName("todayForecasts")
    val todayForecasts: List<AffluencesForecastSlot> = emptyList(),
    @SerialName("forecasts")
    val forecasts: List<AffluencesDayForecasts> = emptyList()
)

/**
 * Detailed open/closed status of a site.
 *
 * Date-time fields use the `yyyy-MM-dd HH:mm:ss` format, local to the site time zone.
 * Text fields are localized according to the `Accept-Language` of the request and are only
 * populated for the relevant state (opening fields when closed, closing fields when open).
 *
 * @property isOpen Whether the site is currently open.
 * @property openingText Localized text describing when the site opens (e.g. "Apre alle 08:30").
 * @property openingAt When the site opens next.
 * @property isOpeningSoon Whether the site is about to open.
 * @property closingText Localized text describing when the site closes.
 * @property closingAt When the site closes next.
 * @property isClosingSoon Whether the site is about to close.
 */
@Serializable
data class AffluencesSiteStatus(
    @SerialName("isOpen")
    val isOpen: Boolean,
    @SerialName("openingText")
    val openingText: String? = null,
    @SerialName("openingAt")
    val openingAt: String? = null,
    @SerialName("isOpeningSoon")
    val isOpeningSoon: Boolean? = null,
    @SerialName("closingText")
    val closingText: String? = null,
    @SerialName("closingAt")
    val closingAt: String? = null,
    @SerialName("isClosingSoon")
    val isClosingSoon: Boolean? = null
)

/**
 * A critical notice attached to the live data of a site.
 *
 * @property contentHtml The HTML content of the notice.
 * @property url External URL with more information.
 */
@Serializable
data class AffluencesNotice(
    @SerialName("contentHtml")
    val contentHtml: String? = null,
    @SerialName("url")
    val url: String? = null
)

/**
 * Real-time attendance measurement of a site.
 *
 * Exactly one style of measurement is populated depending on the precision level of the site:
 * sites with exact sensors report [occupancyPercentage] and/or [waitingTimeMinutes], while sites
 * with estimated data report a [flow] crowd level.
 *
 * @property flow The estimated crowd level, for sites with estimated data.
 * @property occupancyPercentage The current occupancy as a percentage (0-100), for sites with
 * exact sensors.
 * @property waitingTimeMinutes The current waiting time in minutes, for sites that measure queues.
 */
@Serializable
data class AffluencesLiveAttendance(
    @SerialName("flow")
    val flow: AffluencesCrowdLevel? = null,
    @SerialName("occupancy")
    val occupancyPercentage: Int? = null,
    @SerialName("waitingTime")
    val waitingTimeMinutes: Int? = null
)

/**
 * An occupancy forecast for a time range within a day.
 *
 * @property hourRange The start and end of the forecast range, as two `yyyy-MM-dd HH:mm:ss`
 * date-time strings local to the site time zone.
 * @property flow The forecast crowd level, for sites with estimated data.
 * @property flowEvolution The trend of the crowd level compared to the previous slot.
 * @property occupancyPercentage The forecast occupancy as a percentage (0-100).
 * @property occupancyEvolution The trend of the occupancy compared to the previous slot.
 * @property waitingTimeMinutes The forecast waiting time in minutes.
 * @property waitingTimeEvolution The trend of the waiting time compared to the previous slot.
 */
@Serializable
data class AffluencesForecastSlot(
    @SerialName("hourRange")
    val hourRange: List<String> = emptyList(),
    @SerialName("flow")
    val flow: AffluencesCrowdLevel? = null,
    @SerialName("flowEvolution")
    val flowEvolution: AffluencesEvolution? = null,
    @SerialName("occupancy")
    val occupancyPercentage: Int? = null,
    @SerialName("occupancyEvolution")
    val occupancyEvolution: AffluencesEvolution? = null,
    @SerialName("waitingTime")
    val waitingTimeMinutes: Int? = null,
    @SerialName("waitingTimeEvolution")
    val waitingTimeEvolution: AffluencesEvolution? = null
)

/**
 * The occupancy forecasts of a site for a single day.
 *
 * @property day The day of the forecasts, as a `yyyy-MM-dd` date string.
 * @property todayOffset The offset of [day] from today, in days (0 = today, 1 = tomorrow...).
 * @property forecasts The forecast slots covering the opening hours of [day].
 */
@Serializable
data class AffluencesDayForecasts(
    @SerialName("day")
    val day: String,
    @SerialName("todayOffset")
    val todayOffset: Int = 0,
    @SerialName("forecasts")
    val forecasts: List<AffluencesForecastSlot> = emptyList()
)
