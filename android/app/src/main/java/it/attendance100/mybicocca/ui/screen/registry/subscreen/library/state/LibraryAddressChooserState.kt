package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state

/**
 * State for the library address chooser bottom sheet.
 *
 * @property address Cleaned street address (without trailing ", Italie" / ", Italia").
 * @property buildingCode Campus map building code ("U06", "U08"), or null to hide the map action.
 */
data class LibraryAddressChooserState(
    val address: String,
    val buildingCode: String?,
)
