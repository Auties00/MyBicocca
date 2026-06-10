package it.attendance100.mybicocca.domain.model.map

/**
 * A campus building pinned on the map tab. Sourced from the static bundled catalog
 * (buildings.json) — EasyStaff exposes no usable coordinates, so the set is hand-curated — and
 * persisted in Room.
 *
 * @property code Building code, also the EasyStaff query key.
 * @property name Display name.
 * @property point Pin coordinates.
 * @property category Coarse classification driving icon and category filter.
 * @property address Street address; null when not catalogued.
 * @property city Municipality; null when not catalogued.
 */
data class MapBuilding(
    val code: BuildingCode,
    val name: String,
    val point: GeoPoint,
    val category: BuildingCategory,
    val address: String?,
    val city: String?,
)
