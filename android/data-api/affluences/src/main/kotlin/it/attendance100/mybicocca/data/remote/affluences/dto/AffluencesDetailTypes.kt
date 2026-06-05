package it.attendance100.mybicocca.data.remote.affluences.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Practical information about a site, as returned by the app API v4 (`GET /sites/{site}/details`).
 *
 * The wire response also contains a `guide` field whose shape is server-driven and undocumented;
 * it is intentionally not mapped.
 *
 * @property services The amenities available at the site (wifi, power outlets...).
 * @property infos Free-form information sections (access conditions, additional information...).
 */
@Serializable
data class AffluencesSiteDetails(
    @SerialName("services")
    val services: List<AffluencesSiteService> = emptyList(),
    @SerialName("infos")
    val infos: List<AffluencesSiteInfoSection> = emptyList()
)

/**
 * An amenity available at a site.
 *
 * @property type The machine-readable type of the service. Known values include `accessibility`,
 * `power`, `wifi`, `ac`, `study_room`, `printer`, `cloakroom`.
 * @property name The localized display name of the service.
 */
@Serializable
data class AffluencesSiteService(
    @SerialName("type")
    val type: String,
    @SerialName("name")
    val name: String
)

/**
 * A free-form information section of a site page.
 *
 * @property type The machine-readable type of the section. Known values are `ACCESS`
 * (access conditions) and `INFORMATION` (additional information).
 * @property title The localized title of the section.
 * @property contentHtml The HTML content of the section.
 */
@Serializable
data class AffluencesSiteInfoSection(
    @SerialName("type")
    val type: String? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("contentHtml")
    val contentHtml: String? = null
)
