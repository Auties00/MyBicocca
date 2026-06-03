package it.attendance100.mybicocca.domain.model.search

// Tie-break order between equally-scored results: destinations first, then content.
enum class SearchResultCategory(val priority: Int) {
    Destination(0),
    Course(1),
    CalendarEvent(2),
    Building(3),
    TranscriptEntry(4),
}
