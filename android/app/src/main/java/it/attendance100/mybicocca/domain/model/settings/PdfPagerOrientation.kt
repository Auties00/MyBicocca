package it.attendance100.mybicocca.domain.model.settings

/**
 * The scroll axis of the in-app PDF viewer: [Vertical] is a continuous column of pages,
 * [Horizontal] flips page by page.
 *
 * App-local preference persisted in the shared settings DataStore and toggled from the PDF
 * viewer's toolbar; it applies to every PDF opened from the e-learning file viewer.
 */
enum class PdfPagerOrientation { Vertical, Horizontal }
