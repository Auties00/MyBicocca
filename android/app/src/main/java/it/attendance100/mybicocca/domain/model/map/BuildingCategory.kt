package it.attendance100.mybicocca.domain.model.map

/**
 * Coarse classification of a campus building, parsed from the bundled building catalog's
 * category string; drives the building's icon and the category filter on the map tab.
 */
enum class BuildingCategory {
    TEACHING,
    LIBRARY,
    CANTEEN,
    ADMIN,
    SPORT,
    OTHER,
}
