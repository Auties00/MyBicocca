package it.attendance100.mybicocca.domain.model.library

/** Recognized Affluences site service types with dedicated icons and localized labels. */
enum class LibraryServiceType {
    Accessibility,
    Power,
    Wifi,
    AirConditioning,
    StudyRoom,
    Other,
}

/**
 * One facility/service published by Affluences site details (`api.sites.getDetails`).
 *
 * @property type Recognized facility kind, or [LibraryServiceType.Other] for unknown wire types.
 * @property rawType Original wire type key.
 * @property name Service label published by the server; displayed for [LibraryServiceType.Other].
 */
data class LibraryService(
    val type: LibraryServiceType,
    val rawType: String,
    val name: String,
)
