package it.attendance100.mybicocca.domain.model.settings

/**
 * The rendering theme of the in-app PDF viewer: [System] follows the app's resolved dark flag,
 * [Light] and [Dark] force the page colors regardless of it.
 *
 * App-local preference persisted in the shared settings DataStore and toggled from the PDF
 * viewer's toolbar; it applies to every PDF opened from the e-learning file viewer.
 */
enum class PdfThemeMode { System, Light, Dark }
