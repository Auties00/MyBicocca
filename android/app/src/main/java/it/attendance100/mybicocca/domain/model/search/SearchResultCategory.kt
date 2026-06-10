package it.attendance100.mybicocca.domain.model.search

/**
 * Coarse grouping of search results, doubling as the tie-break order between
 * equally-scored hits: actions and destinations first (the command-palette core), then
 * content by how directly the user can act on it.
 *
 * @property priority Tie-break rank; lower wins when scores are equal.
 */
enum class SearchResultCategory(val priority: Int) {
    Action(0),
    Destination(1),
    Course(2),
    Assignment(3),
    Quiz(4),
    CalendarEvent(5),
    Place(6),
    TranscriptEntry(7),
}
